package uk.gov.digital.ho.hocs.audit.auditdetails.exception;

public class EntityCreationException extends RuntimeException {

    public EntityCreationException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}