package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.json.simple.JsonObject;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import com.google.gson.JsonParser;


import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;

@Slf4j
@Entity
@Table(name = "audit_data")
@NoArgsConstructor
public class AuditData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "correlation_id")
    @Getter
    private String correlationID;

    @Column(name = "raising_service")
    @Getter
    private String raisingService;

    @Column(name = "audit_payload")
    @Getter
    private String auditPayload;

    @Column(name = "namespace")
    @Getter
    private String namespace;

    @Column(name = "auditTimestamp")
    @Getter
    private LocalDateTime auditTimestamp;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "user_id")
    @Getter
    private String userID;

    public AuditData(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        this.uuid = UUID.randomUUID();
        this.correlationID = correlationID;
        this.raisingService = raisingService;
        this.auditPayload = auditPayload;
        this.namespace = namespace;
        this.auditTimestamp = auditTimestamp;
        this.type = type;
        this.userID = userID;
    }

    public static AuditData fromDto(CreateAuditDto createAuditDto) throws EntityCreationException {
        validateNotNull(createAuditDto);
        validatePayload(createAuditDto);
        return new AuditData(createAuditDto.getCorrelationID(),
                createAuditDto.getRaisingService(),
                createAuditDto.getAuditPayload(),
                createAuditDto.getNamespace(),
                createAuditDto.getAuditTimestamp(),
                createAuditDto.getType(),
                createAuditDto.getUserID());
    }

    private static void validateNotNull(CreateAuditDto createAuditDto) {
        String correlationID = createAuditDto.getCorrelationID();
        String raisingService = createAuditDto.getRaisingService();
        String namespace = createAuditDto.getNamespace();
        LocalDateTime auditTimestamp = createAuditDto.getAuditTimestamp();
        String type = createAuditDto.getType();
        String userID = createAuditDto.getUserID();

        if (correlationID == null || raisingService == null || namespace == null || auditTimestamp == null || type == null || userID == null) {
            throw new EntityCreationException("Cannot create Audit - null input(%s, %s, %s, %s, %s, %s, %s)",
                    correlationID,
                    raisingService,
                    createAuditDto.getAuditPayload(),
                    namespace,
                    auditTimestamp,
                    type,
                    userID);
        }
    }

    public static void validatePayload(CreateAuditDto createAuditDto) throws EntityCreationException{
        String auditPayload = createAuditDto.getAuditPayload();
        if (createAuditDto.getAuditPayload() != null) {
            try {
                JsonParser parser = new JsonParser();
                parser.parse(auditPayload);
            } catch (JsonSyntaxException e) {
                throw new EntityCreationException("Cannot create Audit - invalid Json (%s, %s, %s, %s, %s, %s, %s)",
                        createAuditDto.getCorrelationID(),
                        createAuditDto.getRaisingService(),
                        auditPayload,
                        createAuditDto.getNamespace(),
                        createAuditDto.getAuditTimestamp(),
                        createAuditDto.getType(),
                        createAuditDto.getUserID());
            }
        }
    }



//    }public static String validatePayload(String auditPayload) {
//        if (auditPayload != null) {
//            try {
//                JsonParser parser = new JsonParser();
//                parser.parse(auditPayload);
//            } catch (JsonSyntaxException e) {
//
//
//
// //               return "{\"invalid_json\": " + "\"name1\":\"value1\",\"name2\"\"value2\"" + "}\"" ;
//                return "{\"invalid_json\": " + auditPayload + "}\"" ;
//
////                JsonObject test = new JsonObject();
////                test.put("invalid_json", auditPayload);
////                return test.toString();
//
//
//            }
//        }
//        return auditPayload;
//    }
}