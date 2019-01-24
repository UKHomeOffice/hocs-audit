package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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

    public AuditData( String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        this.uuid = UUID.randomUUID();
        this.correlationID = correlationID;
        this.raisingService = raisingService;
        this.auditPayload = auditPayload;
        this.namespace = namespace;
        this.auditTimestamp = auditTimestamp;
        this.type = type;
        this.userID = userID;
    }

    public AuditData(UUID caseUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        this(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        this.caseUUID = caseUUID;

    }

}