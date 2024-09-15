package be.fgov.ehealth.packages;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.packages.loader.PackageResourceParsingSvc;
import org.hl7.fhir.utilities.npm.PackageServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PackageLoaderConfig {

	@Bean
	@Primary
	public ca.uhn.fhir.jpa.packages.loader.PackageLoaderSvc ciBuildPackageLoaderSvc() {
		PackageLoaderSvc svc = new PackageLoaderSvc();
		svc.getPackageServers().clear();
		svc.getPackageServers().add(PackageServer.primaryServer());
		svc.getPackageServers().add(PackageServer.secondaryServer());
		return svc;
	}


}

