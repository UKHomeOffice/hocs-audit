package uk.gov.digital.ho.hocs.audit.repository.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Getter
    public static class CustomExportView {

        private final String requiredPermission;

        private final String displayName;

        @JsonValue
        private final List<ExportField> fields;

        @JsonCreator
        public CustomExportView(@JsonProperty("requiredPermission") String requiredPermission,
                              @JsonProperty("displayName") String displayName,
                              @JsonProperty("fields") List<ExportField> fields) {
            this.requiredPermission = requiredPermission;
            this.displayName = displayName;
            this.fields = fields;
        }

        @Getter
        public static class ExportField {

            private final String name;

            private final String adapter;

            @JsonCreator
            public ExportField(@JsonProperty("name") String name,
                                @JsonProperty("adapter") String adapter)
            {
                this.name = name;
                this.adapter = adapter;
            }

        }

    }

}
