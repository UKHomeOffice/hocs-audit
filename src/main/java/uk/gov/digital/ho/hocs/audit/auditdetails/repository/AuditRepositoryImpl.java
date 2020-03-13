package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class AuditRepositoryImpl implements AuditRepositoryCustom {

    @PersistenceContext
    private EntityManager em;


    @Override
    public List<Object[]> getResultsFromView(String viewName) {
        return (List<Object[]>) em.createNativeQuery(String.format("select * from %s", viewName)).unwrap(org.hibernate.query.NativeQuery.class)
                .getResultList();

    }
}


