package be.fgov.ehealth;


import be.fgov.ehealth.domain.TenantView;
import be.fgov.ehealth.repository.TenantRepository;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.entity.PartitionEntity;
import ca.uhn.fhir.jpa.partition.IPartitionLookupSvc;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.util.UrlUtil;
import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Component
@Interceptor
public class ApiKeyInterceptor {

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private IPartitionLookupSvc partitionLookupSvc;

	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
	public boolean handleMethod(final HttpServletRequest rqf, final HttpServletResponse rpf, final RequestDetails requestDetails, final ServletRequestDetails srqd) {
		final Map<String, String[]> params = requestDetails.getParameters();
		final List<String> keyList = params.get("api_key") == null ? new ArrayList<>() : Arrays.stream(params.get("api_key")).toList();


		keyList.stream().filter(x -> !Strings.isNullOrEmpty(x)).findFirst().ifPresentOrElse(s -> {

			final Optional<TenantView> tenant = tenantRepository.findByTenantApiKey(s);
			if (tenant.isEmpty()) {
				throw new AuthenticationException("No such api_key!");
			} else {
				try {
					partitionLookupSvc.getPartitionByName(tenant.orElseThrow().getId().toString());
				} catch (final ResourceNotFoundException rnfe) {
					final PartitionEntity pe = new PartitionEntity();
					pe.setId(tenant.orElseThrow().getId());
					pe.setName(tenant.orElseThrow().getId().toString());
					pe.setDescription(tenant.orElseThrow().getTenantLabel());
					partitionLookupSvc.createPartition(pe, requestDetails);
				}

			}

		}, () -> {
			throw new AuthenticationException("Please provide api_key!");
		});

		requestDetails.removeParameter("api_key");
		URI parsedCompleteUrl = null;
		try {
			parsedCompleteUrl = new URI(requestDetails.getCompleteUrl());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
		final List<Map.Entry<String, String[]>> parsedParams = UrlUtil.parseQueryString(parsedCompleteUrl.getQuery()).entrySet().stream().filter(e -> !"api_key".equals(e.getKey())).toList();
		String newParameterString = null;
		for (final Map.Entry<String, String[]> e : parsedParams) {
			for (final String instance : e.getValue()) {
				if (StringUtils.isEmpty(newParameterString)) {
					newParameterString = "?";
				} else {
					newParameterString += "&";
				}
				newParameterString += e.getKey();
				newParameterString += "=";
				newParameterString += instance;
			}
		}
		URI newCompleteUrl = null;
		try {
			newCompleteUrl = new URI(parsedCompleteUrl.getScheme(), parsedCompleteUrl.getAuthority(), parsedCompleteUrl.getPath(), newParameterString, parsedCompleteUrl.getFragment());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
		requestDetails.setCompleteUrl(newCompleteUrl.toString());

		return true;
	}
}
