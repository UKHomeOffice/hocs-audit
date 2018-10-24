package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;

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

    @Column(name = "before")
    @Getter
    private String before;

    @Column(name = "after")
    @Getter
    private String after;

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

    public AuditData(String correlationID, String raisingService, String before, String after, String namespace, String type, String userID) {
        if (correlationID == null || raisingService == null || namespace == null ||type == null ) {
            throw new EntityCreationException("Cannot create Audit(%s,%s,%s,%s,%s,`%s,%s).", correlationID, raisingService,before, after, namespace, type, userID);
        }
        this.uuid = UUID.randomUUID();
        this.correlationID = correlationID;
        this.raisingService = raisingService;
        this.before = before;
        this.after = after;
        this.namespace = namespace;
        this.auditTimestamp = LocalDateTime.now();
        this.type = type;
        this.userID = userID;
    }
}