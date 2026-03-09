package be.fgov.ehealth.repository;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("service.dte-backend")
public class DteBackendConfig {

	private String endpoint;
	private String username;
	private String password;

}
