package be.fgov.ehealth;


import be.fgov.ehealth.entities.Tenants;
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
import net.sourceforge.plantuml.url.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.internal.util.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Component
@Interceptor
public class ApiKeyInterceptor {

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private IPartitionLookupSvc partitionLookupSvc;

	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
	public boolean handleMethod(HttpServletRequest rqf, HttpServletResponse rpf, RequestDetails requestDetails, ServletRequestDetails srqd){
		Map<String, String[]> params = requestDetails.getParameters();
		List<String> keyList = params.get("api_key")==null?new ArrayList<>():Arrays.stream(params.get("api_key")).toList();


		keyList.stream().filter(x->!Strings.isNullOrEmpty(x)).findFirst().ifPresentOrElse(s -> {

			Tenants tenant = tenantRepository.getTenantByApiKey(s);
			if (tenant == null){
				throw new AuthenticationException("No such api_key!");
			} else {
				try {
					partitionLookupSvc.getPartitionByName(tenant.getId_tenant().toString());
				} catch(ResourceNotFoundException rnfe) {
					PartitionEntity pe = new PartitionEntity();
					pe.setId(tenant.getId_tenant());
					pe.setName(tenant.getId_tenant().toString());
					pe.setDescription(tenant.getTenant_label());
					partitionLookupSvc.createPartition(pe,requestDetails);
				}

			}

		}, () -> {
			throw new AuthenticationException("Please provide api_key!");
		});

		requestDetails.removeParameter("api_key");
		URI parsedCompleteUrl = null;
		try {
			parsedCompleteUrl = new URI(requestDetails.getCompleteUrl());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		List<Map.Entry<String,String[]>> parsedParams = UrlUtil.parseQueryString(parsedCompleteUrl.getQuery()).entrySet().stream().filter(e -> !"api_key".equals(e.getKey())).toList();
		String newParameterString = null;
		for (Map.Entry<String,String[]> e : parsedParams){
			if (StringUtils.isEmpty(newParameterString)){
				newParameterString="?";
			}else{
				newParameterString+="&";
			}
			for (String instance: e.getValue()){
				newParameterString+=e.getKey();
				newParameterString+="=";
				newParameterString+=instance;
			}
		}
		URI newCompleteUrl = null;
		try {
			newCompleteUrl = new URI(parsedCompleteUrl.getScheme(),parsedCompleteUrl.getAuthority(),parsedCompleteUrl.getPath(),newParameterString,parsedCompleteUrl.getFragment());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		requestDetails.setCompleteUrl(newCompleteUrl.toString());

		return true;
	}
}
