package be.fgov.ehealth.repository;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

import static java.net.http.HttpClient.Version.HTTP_1_1;

@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = AbstractIntegrationTest.RepositoryTestConfig.class)
@ActiveProfiles("ehealth")
@DirtiesContext
public abstract class AbstractIntegrationTest {

	public static final String LOCAL_HOST = "http://localhost";
	public static final String DTE_DATA_URL = "/dte";
	public static final WireMockServer WIRE_MOCK
		= new WireMockServer(WireMockConfiguration.options().dynamicPort());

	@BeforeAll
	static void startWireMock() {
		WIRE_MOCK.start();
	}

	@AfterAll
	static void stopWireMock() {
		WIRE_MOCK.stop();
	}

	@DynamicPropertySource
	protected static void properties(final DynamicPropertyRegistry registry) {
		registry.add("service.dte-backend.endpoint", () -> "%s:%d%s/".formatted(LOCAL_HOST, WIRE_MOCK.port(), DTE_DATA_URL));
	}

	@Configuration
	@ComponentScan(basePackages = "be.fgov.ehealth.repository")
	static class RepositoryTestConfig {

		@Bean
		@Profile("test")
		//The test profile forces HTTP/1.1 because Spring Boot 4 defaults to HTTP/2, which WireMock doesn’t support
		public RestClient dteBackendClient(final RestClient.Builder builder, final DteBackendConfig config) {
			final HttpClient httpClient = HttpClient.newBuilder()
				.version(HTTP_1_1)
				.build();
			return builder
				.baseUrl(config.getEndpoint())
				.requestFactory(new JdkClientHttpRequestFactory(httpClient))
				.build();
		}

	}

}
