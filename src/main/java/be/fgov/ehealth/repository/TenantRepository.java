package be.fgov.ehealth.repository;

import be.fgov.ehealth.domain.TenantView;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;


@Repository
public class TenantRepository {

	private final RestClient dteBackendClient;

	public TenantRepository(final RestClient dteBackendClient) {
		this.dteBackendClient = dteBackendClient;
	}

	public Optional<TenantView> findByTenantApiKey(final String apiKey) {
		try {
			return Optional.ofNullable(dteBackendClient.get()
				.uri("/api/tenant/api-key?api_key=" + apiKey)
				.retrieve()
				.body(TenantView.class));
		} catch (final HttpClientErrorException.NotFound e) {
			return Optional.empty();
		}
	}

}
