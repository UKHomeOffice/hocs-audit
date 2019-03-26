package uk.gov.digital.ho.hocs.audit.application;

public enum LogEvent {

    AUDIT_EVENT_CREATED,
    AUDIT_RECORD_NOT_FOUND,
    AUDIT_EVENT_RETRIEVED,
    AUDIT_STARTUP_FAILURE,
    INVALID_AUDIT_PALOAD_STORED,
    AUDIT_EVENT_CREATION_FAILED,
    UNCAUGHT_EXCEPTION;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
