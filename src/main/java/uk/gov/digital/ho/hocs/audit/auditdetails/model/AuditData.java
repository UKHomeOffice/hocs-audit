package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "stage_uuid")
    @Getter
    private UUID stageUUID;

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

    @Column(name = "case_type")
    @Getter
    private String caseType;

    @Column(name = "deleted")
    @Getter
    @Setter
    private Boolean deleted;

    public AuditData( String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        this.uuid = UUID.randomUUID();
        this.correlationID = correlationID;
        this.raisingService = raisingService;
        this.auditPayload = auditPayload;
        this.namespace = namespace;
        this.auditTimestamp = auditTimestamp;
        this.type = type;
        this.userID = userID;
        if(caseUUID != null) {
            this.caseType = caseUUID.toString().substring(34);
        }
        this.deleted = false;
    }

    public AuditData(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        this(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        if(caseUUID != null) {
            this.caseType = caseUUID.toString().substring(34);
        }
    }

}