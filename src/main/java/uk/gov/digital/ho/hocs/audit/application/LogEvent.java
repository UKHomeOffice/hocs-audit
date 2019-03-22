package uk.gov.digital.ho.hocs.audit.application;

public enum LogEvent {

    AUDIT_EVENT_CREATED,
    AUDIT_RECORD_NOT_FOUND,
    AUDIT_EVENTS_RETRIEVED,
    AUDIT_EVENT_RETRIEVED,
    INVALID_AUDIT_PALOAD_STORED, AUDIT_EVENT_CREATION_FAILED, UNCAUGHT_EXCEPTION;

    public static final String EVENT = "event_id";
}
