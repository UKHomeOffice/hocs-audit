package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView.Filter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CustomExportFilter(
    String column,
    LocalDate dateFrom,
    LocalDate dateTo,
    String value,
    boolean includeEmpty
) {

    public static class FilterValidationException extends Exception {
        public FilterValidationException(String message) {
            super(message);
        }
    }

    public static final class ValidatedFilter {
        private final String whereClause;

        private ValidatedFilter(String whereClause) {this.whereClause = whereClause;}

        public String whereClause() {
            return whereClause;
        }
    }

    public ValidatedFilter validate(CustomExportView customExportView) throws FilterValidationException {
        if (Objects.isNull(column)) {
            return validateNullFilter();
        }

        List<String> whereClauses = validateColumnFilters(customExportView);

        if (whereClauses.isEmpty()) {
            throw new FilterValidationException(
                "A date range, value, and/or includeEmpty is required if column is not null"
            );
        }

        return new ValidatedFilter(
            String.format("WHERE %s", String.join(" OR ", whereClauses))
        );
    }

    private List<String> validateColumnFilters(CustomExportView customExportView) throws FilterValidationException {
        Filter filter = lookupExportFieldFilter(customExportView);
        List<String> whereClauses = new ArrayList<>();

        if (!Objects.isNull(dateFrom) || !Objects.isNull(dateTo)) {
            whereClauses.add(validateDateFilter(filter, customExportView));
        } else if (!Objects.isNull(value)) {
            whereClauses.add(validateValueFilter(filter, customExportView));
        }

        if (includeEmpty) {
            whereClauses.add(validateEmptyFilter(filter, customExportView));
        }

        return whereClauses;
    }

    public String columnRef(CustomExportView customExportView) {
        return String.format("%s.\"%s\"", customExportView.viewName(), column);
    }

    private String validateEmptyFilter(Filter filter, CustomExportView view) throws FilterValidationException {
        if (!filter.nullable()) {
            throw new FilterValidationException(
                String.format("%s can not be filtered to empty values", column)
            );
        }

        return String.format("%s IS NULL", columnRef(view));
    }

    private String validateValueFilter(Filter filter, CustomExportView view) throws FilterValidationException {
        if (filter.filterType() != CustomExportView.FilterType.Value) {
            throw new FilterValidationException(String.format("%s can not be filtered by value", column));
        }

        List<String> filterOptions = Objects.requireNonNull(filter.options());

        if (!filterOptions.contains(value)) {
            throw new FilterValidationException(String.format("%s is not a valid value for %s", value, column));
        }

        return String.format("%s = '%s'", columnRef(view), value);
    }

    private String validateDateFilter(Filter filter, CustomExportView view) throws FilterValidationException {
        if (!Objects.isNull(value)) {
            throw new FilterValidationException("A column can't be filtered both by date and value");
        }

        if (filter.filterType() != CustomExportView.FilterType.DateRange) {
            throw new FilterValidationException(String.format("%s is not a date column", column));
        }

        if (!Objects.isNull(dateFrom)) {
            if (Objects.isNull(dateTo)) {
                return String.format(
                    "%s >= '%s 00:00:00'",
                    columnRef(view),
                    dateFrom.format(DateTimeFormatter.ISO_DATE)
                );
            }

            if (dateTo.isBefore(dateFrom)) {
                throw new FilterValidationException("dateFrom must be before dateTo");
            }

            return String.format(
                "%s BETWEEN '%s 00:00:00' AND '%s 23:59:59'",
                columnRef(view),
                dateFrom.format(DateTimeFormatter.ISO_DATE),
                dateTo.format(DateTimeFormatter.ISO_DATE)
            );
        }

        return String.format(
            "%s <= '%s 23:59:59'",
            columnRef(view),
            dateTo.format(DateTimeFormatter.ISO_DATE)
        );
    }

    private Filter lookupExportFieldFilter(CustomExportView customExportView) throws FilterValidationException {
        Optional<CustomExportView.ExportField> maybeField =
            customExportView.fields().stream()
                            .filter(f -> Objects.equals(f.name(), column))
                            .findAny();

        if (maybeField.isEmpty()) {
            throw new FilterValidationException(
                String.format("%s is not a column in export %s", column, customExportView.displayName())
            );
        }

        CustomExportView.ExportField field = maybeField.get();
        if (field.filter() == null) {
            throw new FilterValidationException(
                String.format("%s is not filterable", column)
            );
        }

        return field.filter();
    }

    private ValidatedFilter validateNullFilter() throws FilterValidationException {
        if (!Objects.isNull(dateFrom) || !Objects.isNull(dateTo)) {
            throw new FilterValidationException("Column must be provided to filter by a date range");
        }

        if (!Objects.isNull(value)) {
            throw new FilterValidationException("Column must be provided to filter by a value");
        }

        if (includeEmpty) {
            throw new FilterValidationException("Column must be provided to filter by empty values");
        }

        return new ValidatedFilter("");
    }
}
