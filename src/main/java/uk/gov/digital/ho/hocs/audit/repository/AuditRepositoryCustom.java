package uk.gov.digital.ho.hocs.audit.repository;

import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CustomExportFilter;

import java.time.LocalDate;
import java.util.stream.Stream;

@Repository
public interface AuditRepositoryCustom {

    Stream<Object[]> getResultsFromView(String viewName, CustomExportFilter.ValidatedFilter filter);

    void refreshMaterialisedView(String viewName);

    LocalDate getViewLastRefreshedDate(String viewName);

    void checkViewNameIsAllowed(String viewName);

}
