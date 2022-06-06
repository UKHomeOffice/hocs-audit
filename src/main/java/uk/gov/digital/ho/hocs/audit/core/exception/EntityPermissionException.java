package uk.gov.digital.ho.hocs.audit.core.exception;


import uk.gov.digital.ho.hocs.audit.core.LogEvent;

public class EntityPermissionException extends RuntimeException {

    private final LogEvent event;

    public EntityPermissionException(LogEvent event, String msg, Object... args) {
        super(String.format(msg, args));
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }

}
