package uk.gov.digital.ho.hocs.audit.repository.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Map;

public class CustomExportViews {

    @JsonValue
    private final Map<String, CustomExportView> customExportViews;

    @JsonCreator
    public CustomExportViews(Map<String, CustomExportView> customExportViews) {
        this.customExportViews = customExportViews;
    }

    public CustomExportView getCustomViewByName(String viewName) {
        return customExportViews.get(viewName);
    }

    public record CustomExportView(
        String requiredPermission,
        String displayName,
        String viewName,
        List<ExportField> fields
    ) {
        @JsonCreator
        public CustomExportView(
            @JsonProperty("requiredPermission") String requiredPermission,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("viewName") String viewName,
            @JsonProperty("fields") List<ExportField> fields
        ) {
            this.requiredPermission = requiredPermission;
            this.displayName = displayName;
            this.viewName = viewName;
            this.fields = fields;
        }

        public enum FilterType {
            DateRange,
            Value
        }

        public record Filter(
            @JsonProperty(value = "type", required = true) FilterType filterType,
            @JsonProperty(value = "nullable", defaultValue = "false") boolean nullable,
            @JsonProperty("options") List<String> options) {}

        public record ExportField(String name, String adapter, Filter filter) {
            @JsonCreator
            public ExportField(
                @JsonProperty("name") String name,
                @JsonProperty("adapter") String adapter,
                @JsonProperty("filter") Filter filter
            ) {
                this.name = name;
                this.adapter = adapter;
                this.filter = filter;
            }
        }

    }

}
