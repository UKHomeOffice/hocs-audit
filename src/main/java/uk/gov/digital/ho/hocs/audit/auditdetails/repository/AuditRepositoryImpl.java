package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import lombok.NonNull;
import org.hibernate.jpa.QueryHints;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class AuditRepositoryImpl implements AuditRepositoryCustom {
    @Value("#{'${postgresViewAllowList}'.split(',')}")
    List<String> allowedViewNames;

    public void checkViewNameIsAllowed(String viewName) {
        if(!allowedViewNames.contains(viewName.toLowerCase())) {
            throw new SecurityException(viewName + " is not in the list of allowed view names");
        }
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    public Stream<Object[]> getResultsFromView(@NonNull final String viewName) {
        checkViewNameIsAllowed(viewName);

        return em.createNativeQuery(String.format("SELECT * FROM %s", viewName))
                .setHint(QueryHints.HINT_FETCH_SIZE, 20 )
                .unwrap(Query.class)
                .stream();
    }

    @Override
    public LocalDate getViewLastRefreshedDate(@NonNull final String viewName) {
        checkViewNameIsAllowed(viewName);

        Timestamp timestamp = (Timestamp)
                em.createNativeQuery(String.format("SELECT last_refresh FROM %s LIMIT 1", viewName))
                .unwrap(Query.class)
                .getSingleResult();

        return timestamp.toLocalDateTime().toLocalDate();
    }

    @Override
    public int refreshMaterialisedView(@NonNull final String viewName) {
        checkViewNameIsAllowed(viewName);

        return em.createNativeQuery(String.format("REFRESH MATERIALIZED VIEW %s", viewName)).executeUpdate();
    }
}


