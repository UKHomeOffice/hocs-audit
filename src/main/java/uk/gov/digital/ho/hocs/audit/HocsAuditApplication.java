package uk.gov.digital.ho.hocs.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HocsAuditApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(HocsAuditApplication.class, args);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
