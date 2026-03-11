package be.fgov.ehealth.packages;

import ca.uhn.fhir.jpa.packages.loader.PackageLoaderSvc;
import org.hl7.fhir.utilities.npm.PackageServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PackageLoaderConfig {

	@Bean
	@Primary
	public PackageLoaderSvc ciBuildPackageLoaderSvc() {
		final PackageLoaderSvc svc = new PackageLoaderSvc();
		svc.getPackageServers().clear();
		svc.getPackageServers().addAll(PackageServer.defaultServers());
		return svc;
	}


}

