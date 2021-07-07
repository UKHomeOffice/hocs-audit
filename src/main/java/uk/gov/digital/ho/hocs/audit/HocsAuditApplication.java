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

		try {
			SpringApplication.run(HocsAuditApplication.class, args);
		}
		catch(Exception e) {
			//Log error message to avoid silent camel startup failure
			log.error("Error starting application {}", e.getMessage(), value(EVENT, AUDIT_STARTUP_FAILURE));
			throw e;
		}
	}
	
	@PreDestroy
	public void stop() {
		log.info("Stopping gracefully");
	}

}
