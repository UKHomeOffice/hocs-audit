package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.stream.Stream;

public class AuditRepositoryImpl implements AuditRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Stream getResultsFromView(String viewName) {
        return em.createNativeQuery(String.format("select * from %s", viewName))
                .unwrap(org.hibernate.query.NativeQuery.class)
                .getResultStream();
    }
}


