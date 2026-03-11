package be.fgov.ehealth.repository;

import be.fgov.ehealth.domain.TenantView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TenantRepositoryTest extends AbstractIntegrationTest {

	@Autowired
	private TenantRepository tenantRepository;

	@Test
	void findByTenantApiKey() {
		WIRE_MOCK.stubFor(
			get(urlEqualTo(DTE_DATA_URL + "/api/tenant/api-key?api_key=value"))
				.willReturn(aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "application/json")
					.withBody("""
						{
										"id":1,
										"tenantLabel":"CHU de Bruxelles",
										"tenantDescription":"Environnement de test pour les échanges médicaux électroniques",
										"tenantEhboxSenderId":"87021518344",
										"tenantApiKey":"value"
										}
						""")
				)
		);

		final Optional<TenantView> tenant = tenantRepository.findByTenantApiKey("value");

		assertNotNull(tenant);
		assertThat(tenant)
			.isPresent()
			.hasValueSatisfying(view -> {
				assertThat(view.getId()).isEqualTo(1);
				assertThat(view.getTenantLabel()).isEqualTo("CHU de Bruxelles");
				assertThat(view.getTenantEhboxSenderId()).isEqualTo("87021518344");
			});
	}

}