package uk.gov.digital.ho.hocs.audit.export.infoclient;

public interface ExportViewConstants {

        enum DATE_SECTION {DAY, MONTH, YEAR};
        int YEAR_DIGITS = 4;
        int MONTH_DIGITS = 2;
        int DAY_DIGITS = 2;

        String MALFORMED_DATE_REGEX = "^\\d{4,5}\\-0*(?:[1-9]|1[0-2])\\-0*(?:[1-9]|[12][0-9]|3[01])$";
        String EXTRACT_YEAR_REGEX = "^\\d+?(?=\\-)";
        String EXTRACT_MONTH_REGEX = "(?<=\\-)\\d+(?=\\-)";
        String EXTRACT_DAY_REGEX = "(?:[^\\-]*\\-){2}(\\d+)";

        String FIELD_ADAPTER_HIDDEN = "hidden";
        String FIELD_ADAPTER_USER_EMAIL = "userEmailAdapter";
        String FIELD_ADAPTER_USERNAME = "usernameAdapter";
        String FIELD_ADAPTER_FIRST_AND_LAST_NAME = "userFirstAndLastName";
        String FIELD_ADAPTER_TEAM_NAME = "teamNameAdapter";
        String FIELD_ADAPTER_UNIT_NAME = "unitNameAdapter";
        String FIELD_ADAPTER_TOPIC_NAME = "topicNameAdapter";
        String FIELD_ADAPTER_DATE = "dateAdapter";
}
