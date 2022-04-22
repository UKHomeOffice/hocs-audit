package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.AUDIT_STARTUP_FAILURE;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.EVENT;

@Slf4j
@SpringBootApplication
public class HocsAuditApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsAuditApplication.class, args);
	}
	
	@PreDestroy
	public void stop() {
		log.info("Stopping gracefully");
	}

}
