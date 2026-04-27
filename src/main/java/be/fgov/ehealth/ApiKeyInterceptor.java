package be.fgov.ehealth;


import be.fgov.ehealth.domain.TenantView;
import be.fgov.ehealth.repository.TenantRepository;
import be.fgov.ehealth.utils.UrlTools;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.entity.PartitionEntity;
import ca.uhn.fhir.jpa.partition.IPartitionLookupSvc;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static be.fgov.ehealth.utils.UrlTools.API_KEY;

@Component
@Interceptor
public class ApiKeyInterceptor {

	private final TenantRepository tenantRepository;
	private final IPartitionLookupSvc partitionLookupSvc;

	public ApiKeyInterceptor(final TenantRepository tenantRepository, final IPartitionLookupSvc partitionLookupSvc) {
		this.tenantRepository = tenantRepository;
		this.partitionLookupSvc = partitionLookupSvc;
	}

	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
	public boolean handleMethod(final RequestDetails requestDetails) {
		updatePartitionWithApiKey(requestDetails);
		requestDetails.removeParameter("api_key");
		requestDetails.setCompleteUrl(UrlTools.stripApiKey(requestDetails));
		return true;
	}

	private void updatePartitionWithApiKey(final RequestDetails requestDetails) {
		final String apiKey = Optional.ofNullable(requestDetails.getParameters().get(API_KEY))
			.filter(arr -> arr.length > 0)
			.map(arr -> arr[0])
			.filter(StringUtils::isNotBlank)
			.orElseThrow(() -> new AuthenticationException("Please provide api_key!"));

		final TenantView tenant = tenantRepository.findByTenantApiKey(apiKey)
			.orElseThrow(() -> new AuthenticationException("No such api_key!"));

		ensurePartitionExists(tenant, requestDetails);
	}

	private void ensurePartitionExists(final TenantView tenant, final RequestDetails requestDetails) {
		try {
			partitionLookupSvc.getPartitionByName(tenant.getId().toString());
		} catch (final ResourceNotFoundException e) {
			final PartitionEntity pe = new PartitionEntity();
			pe.setId(tenant.getId());
			pe.setName(tenant.getId().toString());
			pe.setDescription(tenant.getTenantLabel());
			partitionLookupSvc.createPartition(pe, requestDetails);
		}
	}
}
