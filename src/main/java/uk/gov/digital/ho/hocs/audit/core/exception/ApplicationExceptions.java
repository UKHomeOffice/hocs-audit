package uk.gov.digital.ho.hocs.audit.core.exception;


import uk.gov.digital.ho.hocs.audit.core.LogEvent;

public interface ApplicationExceptions {

    class AuditExportException extends RuntimeException {

        private final LogEvent event;

        public AuditExportException(Throwable throwable, LogEvent event, String msg, Object... args) {
            super(String.format(msg, args), throwable);
            this.event = event;
        }

        public AuditExportException(LogEvent event, String msg, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }

    }

    class EntityPermissionException extends RuntimeException {

        private final LogEvent event;

        public EntityPermissionException(LogEvent event, String msg, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }

    }

    class InvalidExportTypeException extends RuntimeException {

        private final LogEvent event;

        public InvalidExportTypeException(LogEvent event, String msg, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }


    }

}
