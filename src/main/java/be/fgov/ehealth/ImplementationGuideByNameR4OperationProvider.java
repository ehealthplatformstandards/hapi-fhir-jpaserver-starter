package be.fgov.ehealth;

import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.ig.IImplementationGuideOperationProvider;
import ca.uhn.fhir.jpa.starter.ig.IgConfigCondition;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;

	@Conditional({OnR4Condition.class, IgConfigCondition.class})
	@Service
	public class ImplementationGuideByNameR4OperationProvider {

		IPackageInstallerSvc packageInstallerSvc;

		public ImplementationGuideByNameR4OperationProvider(IPackageInstallerSvc packageInstallerSvc) {
			this.packageInstallerSvc = packageInstallerSvc;
		}

		@Operation(name = "$installByName", typeName = "ImplementationGuide")
		public Parameters installByName(@OperationParam(name = "name", min = 1, max = 1) String name, @OperationParam(name = "version", min = 1, max = 1) String version, @OperationParam(name = "url", min = 1, max = 1) String url) {


				packageInstallerSvc.install(new PackageInstallationSpec().setName(name).setVersion(version).setFetchDependencies(true).setPackageUrl(url).addDependencyExclude("hl7\\.fhir\\.r4\\.core"));

			return new Parameters();
		}


	}

