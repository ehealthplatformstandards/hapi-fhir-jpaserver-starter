package be.fgov.ehealth.controller;

import ca.uhn.fhir.jpa.entity.PartitionEntity;
import ca.uhn.fhir.jpa.partition.IPartitionLookupSvc;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.rest.annotation.Operation;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

@Conditional({OnR4Condition.class})
@Service
public class FhirJpaController {

	private final IPartitionLookupSvc iPartitionLookupSvc;

	public FhirJpaController(final IPartitionLookupSvc iPartitionLookupSvc) {
		this.iPartitionLookupSvc = iPartitionLookupSvc;
	}

	@Operation(name = "$findAllPartition")
	public Parameters findAllPartition() {
		final List<PartitionEntity> partitions = iPartitionLookupSvc.listPartitions();

		final Parameters parameters = new Parameters();

		for (final PartitionEntity partition : partitions) {
			final Parameters.ParametersParameterComponent partParam = parameters.addParameter().setName("partition");
			partParam.addPart()
				.setName("id")
				.setValue(new IntegerType(partition.getId()));

			partParam.addPart()
				.setName("name")
				.setValue(new StringType(partition.getName()));

			partParam.addPart()
				.setName("description")
				.setValue(new StringType(partition.getDescription()));
		}

		return parameters;
	}

}
