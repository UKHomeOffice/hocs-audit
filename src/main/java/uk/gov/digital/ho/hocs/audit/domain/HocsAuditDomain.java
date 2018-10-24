package uk.gov.digital.ho.hocs.audit.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HocsAuditDomain {

    private final HocsAuditContext hocsAuditContext;

    @Autowired
    public HocsAuditDomain(HocsAuditContext hocsAuditContext) {
        this.hocsAuditContext = hocsAuditContext;
    }

    public void executeCommand(Command command) {
        log.debug("Process command: {}", command);
        command.execute(hocsAuditContext);
        log.debug("Processed command: {}", command);
    }

}
