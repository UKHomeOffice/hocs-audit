package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.UUID;

@Repository
public interface AuditRepository extends CrudRepository<AuditData, String> {

    AuditData findByUuid(UUID uuid);

}

