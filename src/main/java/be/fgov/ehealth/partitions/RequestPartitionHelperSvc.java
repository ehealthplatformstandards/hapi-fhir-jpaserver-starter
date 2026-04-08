package be.fgov.ehealth.partitions;

import org.hl7.fhir.r4.model.codesystems.ResourceTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPartitionHelperSvc extends ca.uhn.fhir.jpa.partition.RequestPartitionHelperSvc {

	private static final Logger ourLog = LoggerFactory.getLogger(RequestPartitionHelperSvc.class);

	public RequestPartitionHelperSvc() {
		super();
		ourLog.info("Loaded be.fgov.ehealth.partitions.RequestPartitionHelperSvc");
	}

	@Override
	public boolean isResourcePartitionable(final String theResourceType) {
		if (ResourceTypes.QUESTIONNAIRE.toCode().equals(theResourceType)) {
			return true;
		}

		return super.isResourcePartitionable(theResourceType);
	}

}
