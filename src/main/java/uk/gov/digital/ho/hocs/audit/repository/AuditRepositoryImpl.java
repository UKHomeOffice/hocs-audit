package uk.gov.digital.ho.hocs.audit.repository;

import lombok.NonNull;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CustomExportFilter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class AuditRepositoryImpl implements AuditRepositoryCustom {

    @Value("#{'${postgresViewAllowList}'.split(',')}")
    List<String> allowedViewNames;

    public void checkViewNameIsAllowed(String viewName) {
        if (!allowedViewNames.contains(viewName.toLowerCase())) {
            throw new SecurityException(viewName + " is not in the list of allowed view names");
        }
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    public Stream<Object[]> getResultsFromView(@NonNull String viewName, CustomExportFilter.ValidatedFilter filter) {
        checkViewNameIsAllowed(viewName);

        // View name limited to specific values, which are safe
        // Where clause validated against specification for view
        // noinspection unchecked,SqlSourceToSinkFlow
        return em.createNativeQuery(
            String.format(
                "SELECT * FROM %s %s",
                viewName,
                filter.whereClause()
            )
        ).unwrap(Query.class).stream();
    }

    @Override
    public LocalDate getViewLastRefreshedDate(@NonNull String viewName) {
        checkViewNameIsAllowed(viewName);

        //noinspection SqlSourceToSinkFlow View name limited to specific values, which are safe
        Timestamp timestamp = (Timestamp) em.createNativeQuery(
            String.format("SELECT last_refresh FROM %s LIMIT 1", viewName)).unwrap(Query.class).getSingleResult();

        return timestamp.toLocalDateTime().toLocalDate();
    }

    @Override
    public void refreshMaterialisedView(@NonNull String viewName) {
        checkViewNameIsAllowed(viewName);

        //noinspection SqlSourceToSinkFlow View name limited to specific values, which are safe
        em.createNativeQuery(String.format("REFRESH MATERIALIZED VIEW CONCURRENTLY %s", viewName)).executeUpdate();
    }

}
