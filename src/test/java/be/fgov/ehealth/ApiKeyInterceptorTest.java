package be.fgov.ehealth;

import be.fgov.ehealth.domain.TenantView;
import be.fgov.ehealth.repository.TenantRepository;
import ca.uhn.fhir.jpa.entity.PartitionEntity;
import ca.uhn.fhir.jpa.partition.IPartitionLookupSvc;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyInterceptor Tests")
class ApiKeyInterceptorTest {

	private static final String API_KEY = "api_key";
	private static final String VALID_API_KEY = "valid_key_123";
	private static final int TENANT_ID = 1;
	private static final String BASE_URL = "http://localhost:8080/fhir";
	@Mock
	private TenantRepository tenantRepository;
	@Mock
	private IPartitionLookupSvc partitionLookupSvc;
	@Mock
	private HttpServletRequest httpServletRequest;
	@Mock
	private HttpServletResponse httpServletResponse;
	@Mock
	private RequestDetails requestDetails;
	@Mock
	private ServletRequestDetails servletRequestDetails;
	@Mock
	private TenantView tenantView;
	@InjectMocks
	private ApiKeyInterceptor apiKeyInterceptor;

	@BeforeEach
	void setUp() {
		// Configuration par défaut des mocks
		lenient().when(tenantView.getId()).thenReturn(TENANT_ID);
		lenient().when(tenantView.getTenantLabel()).thenReturn("Test Tenant");
	}

	// =============== Tests pour updatePartitionWithApiKey ===============

	@Test
	@DisplayName("Should throw AuthenticationException when no api_key is provided")
	void testUpdatePartitionWithApiKey_NoApiKey_ThrowsException() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		when(requestDetails.getParameters()).thenReturn(params);

		// Act & Assert
		final AuthenticationException exception = assertThrows(
			AuthenticationException.class,
			() -> apiKeyInterceptor.handleMethod(requestDetails)
		);

		assertEquals("Please provide api_key!", exception.getMessage());
		verify(tenantRepository, never()).findByTenantApiKey(anyString());
	}

	@Test
	@DisplayName("Should throw AuthenticationException when api_key is null")
	void testUpdatePartitionWithApiKey_NullApiKey_ThrowsException() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, null);
		when(requestDetails.getParameters()).thenReturn(params);

		// Act & Assert
		final AuthenticationException exception = assertThrows(
			AuthenticationException.class,
			() -> apiKeyInterceptor.handleMethod(requestDetails)
		);

		assertEquals("Please provide api_key!", exception.getMessage());
	}

	@Test
	@DisplayName("Should throw AuthenticationException when api_key is empty string")
	void testUpdatePartitionWithApiKey_EmptyApiKey_ThrowsException() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{""});
		when(requestDetails.getParameters()).thenReturn(params);

		// Act & Assert
		final AuthenticationException exception = assertThrows(
			AuthenticationException.class,
			() -> apiKeyInterceptor.handleMethod(requestDetails)
		);

		assertEquals("Please provide api_key!", exception.getMessage());
		verify(tenantRepository, never()).findByTenantApiKey(anyString());
	}

	@Test
	@DisplayName("Should throw AuthenticationException when api_key is not found in repository")
	void testUpdatePartitionWithApiKey_InvalidApiKey_ThrowsException() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		when(requestDetails.getParameters()).thenReturn(params);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.empty());

		// Act & Assert
		final AuthenticationException exception = assertThrows(
			AuthenticationException.class,
			() -> apiKeyInterceptor.handleMethod(requestDetails)
		);

		assertEquals("No such api_key!", exception.getMessage());
		verify(tenantRepository).findByTenantApiKey(VALID_API_KEY);
	}

	@Test
	@DisplayName("Should get partition when tenant is found and partition exists")
	void testUpdatePartitionWithApiKey_ValidApiKey_PartitionExists() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		when(requestDetails.getParameters()).thenReturn(params);
		when(requestDetails.getCompleteUrl()).thenReturn(BASE_URL);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
		verify(tenantRepository).findByTenantApiKey(VALID_API_KEY);
		verify(partitionLookupSvc).getPartitionByName(String.valueOf(TENANT_ID));
		verify(partitionLookupSvc, never()).createPartition(any(), any());
	}

	@Test
	@DisplayName("Should create partition when tenant is found but partition does not exist")
	void testUpdatePartitionWithApiKey_ValidApiKey_PartitionNotExists() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		when(requestDetails.getParameters()).thenReturn(params);
		when(requestDetails.getCompleteUrl()).thenReturn(BASE_URL);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID)))
			.thenThrow(new ResourceNotFoundException("Partition not found"));

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
		verify(tenantRepository).findByTenantApiKey(VALID_API_KEY);
		verify(partitionLookupSvc).getPartitionByName(String.valueOf(TENANT_ID));

		// Verify partition was created with correct values
		final ArgumentCaptor<PartitionEntity> partitionCaptor = ArgumentCaptor.forClass(PartitionEntity.class);
		verify(partitionLookupSvc).createPartition(partitionCaptor.capture(), eq(requestDetails));

		final PartitionEntity createdPartition = partitionCaptor.getValue();
		assertEquals(TENANT_ID, createdPartition.getId());
		assertEquals(String.valueOf(TENANT_ID), createdPartition.getName());
		assertEquals("Test Tenant", createdPartition.getDescription());
	}

	@Test
	@DisplayName("Should handle multiple api_key values and use first valid one")
	void testUpdatePartitionWithApiKey_MultipleApiKeys_UseFirstValid() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY, "other_key"});
		when(requestDetails.getParameters()).thenReturn(params);
		when(requestDetails.getCompleteUrl()).thenReturn(BASE_URL);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
		verify(tenantRepository).findByTenantApiKey(VALID_API_KEY);
		verify(tenantRepository, never()).findByTenantApiKey("other_key");
	}

	// =============== Tests pour handleMethod - URL manipulation ===============

	@Test
	@DisplayName("Should remove api_key parameter from request details")
	void testHandleMethod_RemoveApiKeyParameter() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		params.put("other_param", new String[]{"value"});
		when(requestDetails.getParameters()).thenReturn(params);
		when(requestDetails.getCompleteUrl()).thenReturn(BASE_URL + "?api_key=" + VALID_API_KEY);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
	}

	@Test
	@DisplayName("Should rebuild URL without api_key parameter")
	void testHandleMethod_RebuildURLWithoutApiKey() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		params.put("param1", new String[]{"value1"});
		when(requestDetails.getParameters()).thenReturn(params);
		final String originalUrl = BASE_URL + "?api_key=" + VALID_API_KEY + "&param1=value1";
		when(requestDetails.getCompleteUrl()).thenReturn(originalUrl);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
		final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(requestDetails).setCompleteUrl(urlCaptor.capture());

		final String newUrl = urlCaptor.getValue();
		assertFalse(newUrl.contains(API_KEY), "URL should not contain api_key parameter");
		assertTrue(newUrl.contains("param1=value1"), "URL should contain other parameters");
	}

	@Test
	@DisplayName("Should handle URL with multiple parameters")
	void testHandleMethod_URLWithMultipleParameters() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		params.put("param1", new String[]{"value1", "value2"});
		params.put("param2", new String[]{"value3"});
		when(requestDetails.getParameters()).thenReturn(params);
		final String originalUrl = BASE_URL + "?api_key=" + VALID_API_KEY + "&param1=value1&param1=value2&param2=value3";
		when(requestDetails.getCompleteUrl()).thenReturn(originalUrl);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
		final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(requestDetails).setCompleteUrl(urlCaptor.capture());

		final String newUrl = urlCaptor.getValue();
		assertFalse(newUrl.contains("api_key"));
		assertTrue(newUrl.contains("param1=value1"));
		assertTrue(newUrl.contains("param2=value3"));
	}

	@Test
	@DisplayName("Should handle URL with no query parameters except api_key")
	void testHandleMethod_URLWithOnlyApiKey() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		when(requestDetails.getParameters()).thenReturn(params);
		final String originalUrl = BASE_URL + "?api_key=" + VALID_API_KEY;
		when(requestDetails.getCompleteUrl()).thenReturn(originalUrl);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
		final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(requestDetails).setCompleteUrl(urlCaptor.capture());

		final String newUrl = urlCaptor.getValue();
		assertFalse(newUrl.contains("api_key"));
		assertFalse(newUrl.contains("?"), "URL should not contain query string");
	}

	@Test
	@DisplayName("Should return true on successful processing")
	void testHandleMethod_ReturnsTrue() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		when(requestDetails.getParameters()).thenReturn(params);
		when(requestDetails.getCompleteUrl()).thenReturn(BASE_URL);
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act
		final boolean result = apiKeyInterceptor.handleMethod(requestDetails);

		// Assert
		assertTrue(result);
	}

	@Test
	@DisplayName("Should handle whitespace-only api_key as empty")
	void testHandleMethod_WhitespaceOnlyApiKey() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{"   "});
		when(requestDetails.getParameters()).thenReturn(params);

		// Act & Assert
		final AuthenticationException exception = assertThrows(
			AuthenticationException.class,
			() -> apiKeyInterceptor.handleMethod(requestDetails)
		);

		assertEquals("Please provide api_key!", exception.getMessage());
	}

	@Test
	@DisplayName("Should handle InvalidSyntaxException for malformed URL")
	void testHandleMethod_MalformedURL_ThrowsRuntimeException() {
		// Arrange
		final Map<String, String[]> params = new HashMap<>();
		params.put(API_KEY, new String[]{VALID_API_KEY});
		when(requestDetails.getParameters()).thenReturn(params);
		when(requestDetails.getCompleteUrl()).thenReturn("ht!tp://invalid url");
		when(tenantRepository.findByTenantApiKey(VALID_API_KEY)).thenReturn(Optional.of(tenantView));
		when(partitionLookupSvc.getPartitionByName(String.valueOf(TENANT_ID))).thenReturn(new PartitionEntity());

		// Act & Assert
		final RuntimeException exception = assertThrows(
			RuntimeException.class,
			() -> apiKeyInterceptor.handleMethod(requestDetails)
		);

		assertNotNull(exception);
	}

}