package be.fgov.ehealth.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.entity.PartitionEntity;
import ca.uhn.fhir.jpa.partition.IPartitionLookupSvc;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {Application.class},
	properties = {
		"hapi.fhir.custom-bean-packages=be.fgov.ehealth.controller",
		"hapi.fhir.custom-provider-classes=be.fgov.ehealth.controller.FhirJpaController",
	})
class FhirJpaControllerTest {

	@MockitoBean
	private IPartitionLookupSvc iPartitionLookupSvc;

	@LocalServerPort
	private int port;

	private IGenericClient client;

	@BeforeEach
	void setUp() {
		final FhirContext ctx = FhirContext.forR4();
		client = ctx.newRestfulGenericClient("http://localhost:" + port + "/fhir/");
	}

	@Test
	void testCustomOperations() {
		final PartitionEntity partition = new PartitionEntity();
		partition.setId(1);
		partition.setName("eHealth-Partition");
		partition.setDescription("Ma description de test");

		final PartitionEntity partition2 = new PartitionEntity();
		partition2.setId(2);
		partition2.setName("Partition2");
		partition2.setDescription("description2");

		when(iPartitionLookupSvc.listPartitions()).thenReturn(List.of(partition, partition2));

		final Parameters outParams = client.operation()
			.onServer()
			.named("$findAllPartition")
			.withNoParameters(Parameters.class)
			.execute();


		assertNotNull(outParams, "La réponse ne devrait pas être nulle");
		final ParametersParameterComponent param = outParams.getParameterFirstRep();
		assertEquals("partition", param.getName());

		final List<ParametersParameterComponent> firstPart = outParams.getParameter().get(0).getPart();
		assertEquals("Ma description de test", findDescription(firstPart));
		assertEquals("eHealth-Partition", findName(firstPart));
		assertEquals("1", findId(firstPart));

		final List<ParametersParameterComponent> secondPart = outParams.getParameter().get(1).getPart();
		assertEquals("description2", findDescription(secondPart));
		assertEquals("Partition2", findName(secondPart));
		assertEquals("2", findId(secondPart));

		verify(iPartitionLookupSvc, times(1)).listPartitions();
	}

	private String findDescription(final List<ParametersParameterComponent> part) {
		return findByParameter(part, "description");
	}

	private String findName(final List<ParametersParameterComponent> part) {
		return findByParameter(part, "name");
	}

	private String findId(final List<ParametersParameterComponent> part) {
		return findByParameter(part, "id");
	}

	private String findByParameter(final List<ParametersParameterComponent> part, final String name) {
		return part.stream()
			.filter(p -> name.equals(p.getName()))
			.map(p -> p.getValue().primitiveValue())
			.findFirst()
			.orElse(null);
	}

}