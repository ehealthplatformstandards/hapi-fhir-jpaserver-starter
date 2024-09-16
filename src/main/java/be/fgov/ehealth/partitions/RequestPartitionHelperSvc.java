package be.fgov.ehealth.partitions;

import ca.uhn.fhir.interceptor.model.ReadPartitionIdRequestDetails;
import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPartitionHelperSvc extends ca.uhn.fhir.jpa.partition.RequestPartitionHelperSvc {
	private static final Logger ourLog = LoggerFactory.getLogger(RequestPartitionHelperSvc.class);

	public RequestPartitionHelperSvc(){
		ourLog.info("Loaded be.fgov.ehealth.partitions.RequestPartitionHelperSvc");
	}

	@Nonnull
	@Override
	public RequestPartitionId determineReadPartitionForRequest(
		@Nullable RequestDetails theRequest, @Nonnull ReadPartitionIdRequestDetails theDetails) {
		RequestPartitionId requestPartitionId;
		try{
			requestPartitionId = super.determineReadPartitionForRequest(theRequest,theDetails);
		} catch(InternalErrorException iee){
			return RequestPartitionId.allPartitions();
		}

		return requestPartitionId;
	}
}
