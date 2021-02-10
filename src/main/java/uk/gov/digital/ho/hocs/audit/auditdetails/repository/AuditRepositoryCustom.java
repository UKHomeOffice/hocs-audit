package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface AuditRepositoryCustom {

    Stream<Object[]> getResultsFromView(String viewName);

}


