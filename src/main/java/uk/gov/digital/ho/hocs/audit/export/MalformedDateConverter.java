package uk.gov.digital.ho.hocs.audit.export;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.export.adapter.DateAdapter;

@Service
public class MalformedDateConverter {

    private DateAdapter dateAdapter = new DateAdapter();

    public String[] correctDateFields(String[] auditData) {
        if (auditData == null) {
            return null;
        }
        for (int i = 0; i < auditData.length; i++){
            auditData[i] = dateAdapter.convert(auditData[i]);
        }
        return auditData;
    }

}
