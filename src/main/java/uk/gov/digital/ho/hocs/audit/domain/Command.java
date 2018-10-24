package uk.gov.digital.ho.hocs.audit.domain;

public interface Command {

    void execute(HocsAuditContext hocsAuditContext);
}
