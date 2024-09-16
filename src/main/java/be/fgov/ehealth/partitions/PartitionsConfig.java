package be.fgov.ehealth.partitions;

import ca.uhn.fhir.jpa.partition.IRequestPartitionHelperSvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class PartitionsConfig {
	@Bean
	@Primary
	public IRequestPartitionHelperSvc myRequestPartitionHelperService() {
		return new RequestPartitionHelperSvc();
	}
}
