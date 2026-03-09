package be.fgov.ehealth.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class DteBackendConnector {

	private final DteBackendConfig dteBackendConfig;

	public DteBackendConnector(final DteBackendConfig dteBackendConfig) {
		this.dteBackendConfig = dteBackendConfig;
	}

	@Bean
	public RestClient dteBackendClient(final RestClient.Builder builder) {
		final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();
		final JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofMinutes(30));
		return builder
			.baseUrl(dteBackendConfig.getEndpoint())
			.requestFactory(factory)
			.defaultHeaders(headers ->
				headers.setBasicAuth(dteBackendConfig.getUsername(), dteBackendConfig.getPassword()))
			.build();
	}

	@Bean
	private RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

}
