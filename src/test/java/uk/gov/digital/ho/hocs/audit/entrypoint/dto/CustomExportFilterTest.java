package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView.ExportField;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView.Filter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView.FilterType.DateRange;
import static uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews.CustomExportView.FilterType.Value;

class CustomExportFilterTest {

    private CustomExportViews.CustomExportView exampleView(List<ExportField> fields) {
        return new CustomExportViews.CustomExportView(
            "permission",
            "displayName",
            "viewName",
            fields
        );
    }

    @Test
    void validateGeneratesAnEmptyWhereClauseWhenNoFiltersAreProvided() throws CustomExportFilter.FilterValidationException {
        var filter = new CustomExportFilter(null, null, null, null, false);
        var exportView = exampleView(List.of());

        var validated = filter.validate(exportView);

        assertEquals("", validated.whereClause());
    }

    public static Stream<Arguments> missingColumnExamples() {
        return Stream.of(
            Arguments.of(
                new CustomExportFilter(null, LocalDate.of(2025, 1, 1), null, null, false),
                "Column must be provided to filter by a date range"
            ),
            Arguments.of(
                new CustomExportFilter(null, null, LocalDate.of(2025, 1, 1), null, false),
                "Column must be provided to filter by a date range"
            ),
            Arguments.of(
                new CustomExportFilter(null, null, null, "value", false),
                "Column must be provided to filter by a value"
            ),
            Arguments.of(
                new CustomExportFilter(null, null, null, null, true),
                "Column must be provided to filter by empty values"
            )
        );
    }

    @ParameterizedTest(name = "{1} thrown on missing column")
    @MethodSource("missingColumnExamples")
    void validateFailsWhenMissingAColumn(CustomExportFilter filter, String expectedMessage) {
        var exportView = exampleView(List.of());

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void validateFailsWhenFilteringOnAnInvalidColumn() {
        var filter = new CustomExportFilter("columnName", null, null, null, false);
        var exportView = exampleView(List.of());

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("columnName is not a column in export displayName", exception.getMessage());
    }

    @Test
    void validateFailsWhenFilteringOnAColumnWithoutAFilter() {
        var filter = new CustomExportFilter("columnName", null, null, null, false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, null))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("columnName is not filterable", exception.getMessage());
    }

    @Test
    void validateFailsWhenNoFilterParametersAreProvided() {
        var filter = new CustomExportFilter("columnName", null, null, null, false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(DateRange, false, null)))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("A date range, value, and/or includeEmpty is required if column is not null", exception.getMessage());
    }

    @Test
    void validateFailsWhenTooManyParametersAreProvided() {
        var filter = new CustomExportFilter("columnName", LocalDate.parse("2025-01-01"), null, "value", false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(DateRange, false, null)))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("A column can't be filtered both by date and value", exception.getMessage());
    }

    @Test
    void validateFailsDateRangeIsProvidedToValueFiler() {
        var filter = new CustomExportFilter("columnName", LocalDate.parse("2025-01-01"), null, null, false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(Value, false, null)))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("columnName is not a date column", exception.getMessage());
    }

    @Test
    void validateFailsForOutOfOrderDateRange() {
        var filter = new CustomExportFilter(
            "columnName",
            LocalDate.parse("2025-01-01"),
            LocalDate.parse("2024-12-31"),
            null,
            false
        );
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(DateRange, false, null)))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("dateFrom must be before dateTo", exception.getMessage());
    }

    public static Stream<Arguments> dateFiltersExamples() {
        return Stream.of(
            Arguments.of(
                LocalDate.parse("2025-01-01"),
                null,
                "WHERE viewName.\"columnName\" >= '2025-01-01 00:00:00'"
            ),
            Arguments.of(
                null,
                LocalDate.parse("2025-01-01"),
                "WHERE viewName.\"columnName\" <= '2025-01-01 23:59:59'"
            ),
            Arguments.of(
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-01-01"),
                "WHERE viewName.\"columnName\" BETWEEN '2025-01-01 00:00:00' AND '2025-01-01 23:59:59'"
            )
        );
    }

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @MethodSource("dateFiltersExamples")
    void validateGeneratesDateRangeFilters(
        LocalDate dateFrom,
        LocalDate dateTo,
        String expectedFilter
    ) throws CustomExportFilter.FilterValidationException {
        var filter = new CustomExportFilter("columnName", dateFrom, dateTo, null, false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(DateRange, false, null)))
        );

        var validated = filter.validate(exportView);

        assertEquals(expectedFilter, validated.whereClause());
    }

    @Test
    void validateFailsForValueFiltersWithNoOptionsDefined() {
        var filter = new CustomExportFilter("columnName", null, null, "value", false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(Value, false, null)))
        );

        // Config error - should be resolved by developer changing filter specification
        assertThrows(
            NullPointerException.class,
            () -> filter.validate(exportView)
        );
    }

    @Test
    void validateFailsForValueFiltersOnDateColumn() {
        var filter = new CustomExportFilter("columnName", null, null, "value", false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(DateRange, false, null)))
        );

       var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("columnName can not be filtered by value", exception.getMessage());
    }

    @Test
    void validateFailsWhenInvalidValueIsProvided() {
        var filter = new CustomExportFilter("columnName", null, null, "value", false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(Value, false, List.of("anotherValue"))))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("value is not a valid value for columnName", exception.getMessage());
    }

    @Test
    void validateGeneratesValueFilters() throws CustomExportFilter.FilterValidationException {
        var filter = new CustomExportFilter("columnName", null, null, "value", false);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(Value, false, List.of("value"))))
        );

        var validated = filter.validate(exportView);

        assertEquals("WHERE viewName.\"columnName\" = 'value'", validated.whereClause());
    }

    @Test
    void validateFailsWhenIncludeEmptyProvidedToNonNullableColumn() {
        var filter = new CustomExportFilter("columnName", null, null, null, true);
        var exportView = exampleView(
            List.of(new ExportField("columnName", null, new Filter(DateRange, false, null)))
        );

        var exception = assertThrows(
            CustomExportFilter.FilterValidationException.class,
            () -> filter.validate(exportView)
        );

        assertEquals("columnName can not be filtered to empty values", exception.getMessage());
    }

    public static Stream<Arguments> includeEmptyExamples() {
        return Stream.of(
            Arguments.of(
                new CustomExportFilter("dateName", null, null, null, true),
                "WHERE viewName.\"dateName\" IS NULL",
                "Nullable date column is empty"
            ),
            Arguments.of(
                new CustomExportFilter("dateName", LocalDate.parse("2025-01-01"), null, null, true),
                "WHERE viewName.\"dateName\" >= '2025-01-01 00:00:00' OR viewName.\"dateName\" IS NULL",
                "Nullable date column is in range or empty"
            ),
            Arguments.of(
                new CustomExportFilter("valueName", null, null, null, true),
                "WHERE viewName.\"valueName\" IS NULL",
                "Nullable value column is empty"
            ),
            Arguments.of(
                new CustomExportFilter("valueName", null, null, "value", true),
                "WHERE viewName.\"valueName\" = 'value' OR viewName.\"valueName\" IS NULL",
                "Nullable value column is a value or empty"
            )
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("includeEmptyExamples")
    void validateGeneratesFiltersForNullableColumns(
        CustomExportFilter filter,
        String expectedFilter,
        String label
    ) throws CustomExportFilter.FilterValidationException {
        var exportView = exampleView(
            List.of(
                new ExportField("dateName", null, new Filter(DateRange, true, null)),
                new ExportField("valueName", null, new Filter(Value, true, List.of("value")))
            )
        );

        var validated = filter.validate(exportView);

        assertEquals(expectedFilter, validated.whereClause());
    }
}
