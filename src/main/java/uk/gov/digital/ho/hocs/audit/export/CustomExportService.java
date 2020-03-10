package uk.gov.digital.ho.hocs.audit.export;

import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class CustomExportService {

    @Autowired
    private EntityManager em;



    public Object getResults(String customViewName){
        List<Object[]> results = em.createNativeQuery(String.format("select * from %s;", customViewName)).unwrap(org.hibernate.query.NativeQuery.class)
                .addScalar("properties", JsonNodeBinaryType.INSTANCE).getResultList();

        return results;
    }
}
