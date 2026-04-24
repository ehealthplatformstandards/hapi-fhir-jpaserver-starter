package be.fgov.ehealth.partitions;

import org.hl7.fhir.r4.model.codesystems.ResourceTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RequestPartitionHelperSvc extends ca.uhn.fhir.jpa.partition.RequestPartitionHelperSvc {

	private static final Logger ourLog = LoggerFactory.getLogger(RequestPartitionHelperSvc.class);

	private static final Set<String> ALLOWED_RESOURCES = Set.of(
		ResourceTypes.QUESTIONNAIRE.toCode(),
		ResourceTypes.STRUCTUREDEFINITION.toCode()
	);

	public RequestPartitionHelperSvc() {
		super();
		ourLog.info("Loaded be.fgov.ehealth.partitions.RequestPartitionHelperSvc");
	}

	@Override
	public boolean isResourcePartitionable(final String theResourceType) {
		return ALLOWED_RESOURCES.contains(theResourceType) || super.isResourcePartitionable(theResourceType);
	}

}
