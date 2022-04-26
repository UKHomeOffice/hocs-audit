package uk.gov.digital.ho.hocs.audit.core;

public enum LogEvent {

    AUDIT_EVENT_CREATED,
    AUDIT_RECORD_NOT_FOUND,
    INVALID_AUDIT_PAYLOAD_STORED,
    AUDIT_EVENT_CREATION_FAILED,
    UNCAUGHT_EXCEPTION,
    CSV_EXPORT_START,
    CSV_EXPORT_COMPETE,
    CSV_EXPORT_FAILURE,
    REFRESH_MATERIALISED_VIEW,
    CSV_CUSTOM_CONVERTER_FAILURE,
    REST_CLIENT_EXCEPTION;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
