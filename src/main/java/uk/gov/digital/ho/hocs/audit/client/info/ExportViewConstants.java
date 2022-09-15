package uk.gov.digital.ho.hocs.audit.client.info;

public interface ExportViewConstants {

    // identifies malformed dates with groups for extraction
    String GROUPED_DATE_REGEX = "^0*(\\d{4})\\-0*(0[1-9]|1[0-2])\\-0*(0[1-9]|[12][0-9]|3[01])$";

    String FIELD_ADAPTER_HIDDEN = "hidden";
    String FIELD_ADAPTER_USER_EMAIL = "userEmailAdapter";
    String FIELD_ADAPTER_USERNAME = "usernameAdapter";
    String FIELD_ADAPTER_FIRST_AND_LAST_NAME = "userFirstAndLastName";
    String FIELD_ADAPTER_TEAM_NAME = "teamNameAdapter";
    String FIELD_ADAPTER_UNIT_NAME = "unitNameAdapter";
    String FIELD_ADAPTER_TOPIC_NAME = "topicNameAdapter";
    String FIELD_ADAPTER_DATE = "dateAdapter";

}
