package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateAuditDto {

    @JsonProperty("correlation_id")
    private String correlationID;

    @JsonProperty("raising_service")
    private String raisingService;

    @JsonProperty("audit_payload")
    private String auditPayload;

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("type")
    private String type;

    @JsonProperty("user_id")
    private String userID;

}

/*

aws --endpoint-url=http://localhost:4576 sqs send-message --queue-url http://localstack:4576/queue/reporting-queue --message-body ' { "correlation_id":"corrID", "raising_service":"raising", "audit_payload":"{\"code\":3,\"type\":\"AES\"}", "namespace":"namespace1", "type":"type", "user_id":"usID"}'

*/
