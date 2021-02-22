package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import lombok.NonNull;
import org.hibernate.jpa.QueryHints;
import org.hibernate.query.Query;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.stream.Stream;

public class AuditRepositoryImpl implements AuditRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Stream<Object[]> getResultsFromView(@NonNull final String viewName) {
        return em.createNativeQuery(String.format("SELECT * FROM %s", viewName))
                .setHint(QueryHints.HINT_FETCH_SIZE, 50 )
                .unwrap(Query.class)
                .stream();
    }
}


