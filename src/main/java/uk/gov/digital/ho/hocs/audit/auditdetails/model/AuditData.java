package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.json.JSONObject;
import org.json.JSONException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;


import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Base64;


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

    @Getter
    @Column(name = "audit_payload")
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

    public String getAuditPayload() {
        try {
            JSONObject jsonObject = new JSONObject(auditPayload);
            if (jsonObject.has("invalid_json")) {
                String encodedPayload = jsonObject.getString("invalid_json");
                byte[] decodedPayload = Base64.getDecoder().decode(encodedPayload);
                return new String(decodedPayload);
            }
        } catch (JSONException e) { // Do nothing
             }
            return auditPayload;
        }

    public static AuditData fromDto(CreateAuditDto createAuditDto){
        validateNotNull(createAuditDto);
        String auditPayload = validatePayload(createAuditDto);
        return new AuditData(createAuditDto.getCorrelationID(),
                createAuditDto.getRaisingService(),
                auditPayload,
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

    private static String validatePayload(CreateAuditDto createAuditDto) {
         String auditPayload = createAuditDto.getAuditPayload();
        if (auditPayload != null) {
            try {
                com.google.gson.JsonParser parser = new JsonParser();
                parser.parse(auditPayload);
            } catch (JsonSyntaxException e) {
                log.info("Created audit with invalid json in payload - Correlation ID: {}, Raised by: {}, Invalid json payload: {}, Namespace: {}, Timestamp: {}, EventType: {}, User: {}\")",
                        createAuditDto.getCorrelationID(),
                        createAuditDto.getRaisingService(),
                        auditPayload,
                        createAuditDto.getNamespace(),
                        createAuditDto.getAuditTimestamp(),
                        createAuditDto.getType(),
                        createAuditDto.getUserID());
                // Encode invalid json to base 64, otherwise it can be seen as nested invalid json
                byte[] encodedPayload = Base64.getEncoder().encode(auditPayload.getBytes());
                return "{\"invalid_json\":\"" + new String(encodedPayload) + "\"}";
            }
        }
        return auditPayload;
    }
}