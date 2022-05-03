package uk.gov.digital.ho.hocs.audit.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.stream.Stream;

@Repository
public interface AuditRepositoryCustom {

    Stream<Object[]> getResultsFromView(String viewName);
    void refreshMaterialisedView(String viewName);
    LocalDate getViewLastRefreshedDate(String viewName);
    void checkViewNameIsAllowed(String viewName);
}
