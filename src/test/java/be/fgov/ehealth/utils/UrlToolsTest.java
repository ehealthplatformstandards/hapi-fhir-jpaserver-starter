package be.fgov.ehealth.utils;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class UrlToolsTest {

	private RequestDetails requestDetails;

	@BeforeEach
	void setUp() {
		requestDetails = mock(RequestDetails.class);
	}

	@ParameterizedTest(name = "Input: {0} => Expected: {1}")
	@CsvSource(delimiter = ';', value = {
		"https://api.example.com/endpoint?a=1&api_key=secret&b=2&c=3;https://api.example.com/endpoint?a=1&b=2&c=3",
		"https://api.example.com/endpoint;https://api.example.com/endpoint",
		"https://my-api_key-provider.com/endpoint?param=value;https://my-api_key-provider.com/endpoint?param=value",
		"https://user:pass@api.example.com/endpoint?api_key=secret;https://user:pass@api.example.com/endpoint",
		"https://api.example.com/endpoint?api_key=secret&search=hello%20world;https://api.example.com/endpoint?search=hello%20world",
		"https://api.example.com/endpoint?api_key=&param=value;https://api.example.com/endpoint?param=value",
		"https://api.example.com/v1/users/123/profile?api_key=secret&include=details;https://api.example.com/v1/users/123/profile?include=details",
		"https://api.example.com:8443/endpoint?api_key=secret&param=value;https://api.example.com:8443/endpoint?param=value",
		"https://api.example.com/endpoint?api_key=secret;https://api.example.com/endpoint",
		"https://api.example.com/endpoint?api_key=secret&name=john%20doe&email=test%40example.com;https://api.example.com/endpoint?name=john%20doe&email=test%40example.com",
		"https://api.example.com/endpoint?api_key=secret#section;https://api.example.com/endpoint#section",
		"https://api.example.com/endpoint?api_key=secret1&api_key=secret2&param=value;https://api.example.com/endpoint?param=value",
		"https://api.example.com/endpoint?param1=value1&api_key=secret&param2=value2;https://api.example.com/endpoint?param1=value1&param2=value2",
		"https://api.example.com/endpoint?api_key=secret123;https://api.example.com/endpoint",
		"https://api.example.com/endpoint;https://api.example.com/endpoint",
		"https://api.example.com/endpoint?api_key=secret;https://api.example.com/endpoint",
		"https://api.example.com/endpoint?api_key=secret123&param=value;https://api.example.com/endpoint?param=value",
	})
	void stripApiKey(final String url, final String urlClean) {
		when(requestDetails.getCompleteUrl()).thenReturn(url);

		assertEquals(urlClean, UrlTools.stripApiKey(requestDetails));
	}

	@Test
	@DisplayName("Should throw exception for malformed URL")
	void stripApiKey_MalformedUrl() {
		when(requestDetails.getCompleteUrl()).thenReturn("not a valid url ][");

		assertThrows(IllegalArgumentException.class, () -> UrlTools.stripApiKey(requestDetails));
	}

}