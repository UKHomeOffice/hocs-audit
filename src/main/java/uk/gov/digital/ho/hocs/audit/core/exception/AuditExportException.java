package uk.gov.digital.ho.hocs.audit.core.exception;

public class AuditExportException extends RuntimeException {

    public AuditExportException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
