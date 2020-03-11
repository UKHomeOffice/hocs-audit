package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepositoryCustom {

    List<Object[]> getResultsFromView(String viewName);


}


