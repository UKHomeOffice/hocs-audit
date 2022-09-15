package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

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
