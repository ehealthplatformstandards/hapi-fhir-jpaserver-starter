package be.fgov.ehealth.service.impl;

import be.fgov.ehealth.domain.hapi.PackageInfo;
import ca.uhn.fhir.jpa.dao.data.INpmPackageDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageEntity;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImplementationGuideServiceImplTest {

	@Mock
	private INpmPackageDao inpmPackageDao;
	@Mock
	private JsoupHtmlFetcher jsoupHtmlFetcher;
	@Mock
	private IPackageInstallerSvc iPackageInstallerSvc;
	@InjectMocks
	private ImplementationGuideServiceImpl implementationGuideService;

	@Test
	void findPackagesToUpdate_shouldReturnPackagesNotInDatabase() {
		final NpmPackageEntity npmPackageEntity = new NpmPackageEntity();
		npmPackageEntity.setPackageId("hl7.fhir.be.core");
		npmPackageEntity.setCurrentVersionId("2.1.2");


		final List<NpmPackageEntity> existingPackages = List.of(npmPackageEntity);

		final List<PackageInfo> availablePackages = List.of(
			new PackageInfo("hl7.fhir.be.core", "2.1.2"),
			new PackageInfo("hl7.fhir.be.lab", "1.0.0")
		);

		when(inpmPackageDao.findAll()).thenReturn(existingPackages);
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(availablePackages);

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertEquals(1, result.size());
		assertEquals("hl7.fhir.be.lab", result.get(0).getName());
		assertEquals("1.0.0", result.get(0).getVersion());
	}

	@Test
	void findPackagesToUpdate_shouldReturnPackagesWithNewVersion() {
		final NpmPackageEntity npmPackageEntity = new NpmPackageEntity();
		npmPackageEntity.setPackageId("hl7.fhir.be.core");
		npmPackageEntity.setCurrentVersionId("2.1.1");

		final List<NpmPackageEntity> existingPackages = List.of(npmPackageEntity);

		final List<PackageInfo> availablePackages = List.of(
			new PackageInfo("hl7.fhir.be.core", "2.1.2")
		);

		when(inpmPackageDao.findAll()).thenReturn(existingPackages);
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(availablePackages);

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertEquals(1, result.size());
		assertEquals("hl7.fhir.be.core", result.get(0).getName());
		assertEquals("2.1.2", result.get(0).getVersion());
	}


	@Test
	void findPackagesToUpdate_shouldNotReturnPackagesAlreadyUpToDate() {

		final NpmPackageEntity npmPackageEntity = new NpmPackageEntity();
		npmPackageEntity.setPackageId("hl7.fhir.be.core");
		npmPackageEntity.setCurrentVersionId("2.1.2");

		final NpmPackageEntity npmPackageEntity2 = new NpmPackageEntity();
		npmPackageEntity2.setPackageId("hl7.fhir.uv.extensions.r4");
		npmPackageEntity2.setCurrentVersionId("5.1.0");

		final List<NpmPackageEntity> existingPackages = List.of(npmPackageEntity, npmPackageEntity2);

		final List<PackageInfo> availablePackages = List.of(
			new PackageInfo("hl7.fhir.be.core", "2.1.2"),
			new PackageInfo("hl7.fhir.uv.extensions.r4", "5.1.0")
		);

		when(inpmPackageDao.findAll()).thenReturn(existingPackages);
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(availablePackages);

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertTrue(result.isEmpty());
	}

	@Test
	void findPackagesToUpdate_shouldReturnAllPackages_whenDatabaseIsEmpty() {
		final List<PackageInfo> availablePackages = List.of(
			new PackageInfo("hl7.fhir.be.core", "2.1.2"),
			new PackageInfo("hl7.fhir.be.lab", "1.0.0")
		);

		when(inpmPackageDao.findAll()).thenReturn(List.of());
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(availablePackages);

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertEquals(2, result.size());
		assertEquals(
			List.of("hl7.fhir.be.core", "hl7.fhir.be.lab"),
			result.stream()
				.map(PackageInfo::getName)
				.toList()
		);
	}

	@Test
	void findPackagesToUpdate_shouldReturnEmptyList_whenNoAvailablePackages() {
		when(inpmPackageDao.findAll()).thenReturn(List.of());
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of());

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void findPackagesToUpdate_shouldReturnPackage_whenAvailableVersionIsNewerThanDatabaseVersion() {
		final NpmPackageEntity npmPackageEntity = new NpmPackageEntity();
		npmPackageEntity.setPackageId("hl7.fhir.be.core");
		npmPackageEntity.setCurrentVersionId("2.1.2");

		final List<NpmPackageEntity> existingPackages = List.of(npmPackageEntity);

		final List<PackageInfo> availablePackages = List.of(
			new PackageInfo("hl7.fhir.be.core", "2.1.3")
		);

		when(inpmPackageDao.findAll()).thenReturn(existingPackages);
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(availablePackages);

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertEquals(1, result.size());

		assertEquals("hl7.fhir.be.core", result.get(0).getName());
		assertEquals("2.1.3", result.get(0).getVersion());
	}

	@Test
	void findPackagesToUpdate_shouldReturnOnlyNewerVersions() {
		final NpmPackageEntity entity = new NpmPackageEntity();
		entity.setPackageId("hl7.fhir.be.core");
		entity.setCurrentVersionId("2.1.2");

		when(inpmPackageDao.findAll()).thenReturn(List.of(entity));

		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of(
			new PackageInfo("hl7.fhir.be.core", "2.1.1"),
			new PackageInfo("hl7.fhir.be.core", "10.10.3")
		));

		final List<PackageInfo> result = implementationGuideService.findPackagesToUpdate();

		assertEquals(1, result.size());
		assertEquals("10.10.3", result.get(0).getVersion());
	}

	@Test
	void updatePackages_shouldInstallNewPackage_whenNoExistingVersion() {
		when(inpmPackageDao.findAll()).thenReturn(List.of());

		final PackageInfo availablePackage = new PackageInfo("hl7.fhir.be.core", "1.2.0");
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of(availablePackage));

		implementationGuideService.updatePackages();

		verify(iPackageInstallerSvc, never()).uninstall(any());
		final ArgumentCaptor<PackageInstallationSpec> installCaptor = ArgumentCaptor.forClass(PackageInstallationSpec.class);
		verify(iPackageInstallerSvc, times(1)).install(installCaptor.capture());
		final PackageInstallationSpec installedSpec = installCaptor.getValue();
		assertThat(installedSpec.getName()).isEqualTo("hl7.fhir.be.core");
		assertThat(installedSpec.getVersion()).isEqualTo("1.2.0");
		assertThat(installedSpec.getPackageUrl()).isEqualTo("https://packages.fhir.org/hl7.fhir.be.core/1.2.0");
		assertThat(installedSpec.isFetchDependencies()).isTrue();
		assertThat(installedSpec.getDependencyExcludes()).contains("hl7.fhir.r4.core");
	}

	@Test
	void updatePackages_shouldUninstallOldVersionThenInstallNewVersion_whenNewerVersionAvailable() {
		final NpmPackageEntity existingEntity = npmPackageEntity("hl7.fhir.be.core", "1.1.0");
		when(inpmPackageDao.findAll()).thenReturn(List.of(existingEntity));

		final PackageInfo availablePackage = new PackageInfo("hl7.fhir.be.core", "1.2.0");
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of(availablePackage));

		final ArgumentCaptor<PackageInstallationSpec> uninstallCaptor = ArgumentCaptor.forClass(PackageInstallationSpec.class);
		final ArgumentCaptor<PackageInstallationSpec> installCaptor = ArgumentCaptor.forClass(PackageInstallationSpec.class);

		implementationGuideService.updatePackages();

		final InOrder inOrder = inOrder(iPackageInstallerSvc);
		inOrder.verify(iPackageInstallerSvc).uninstall(uninstallCaptor.capture());
		inOrder.verify(iPackageInstallerSvc).install(installCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final PackageInstallationSpec uninstalledSpec = uninstallCaptor.getValue();
		assertThat(uninstalledSpec.getName()).isEqualTo("hl7.fhir.be.core");
		assertThat(uninstalledSpec.getVersion()).isEqualTo("1.1.0");

		final PackageInstallationSpec installedSpec = installCaptor.getValue();
		assertThat(installedSpec.getName()).isEqualTo("hl7.fhir.be.core");
		assertThat(installedSpec.getVersion()).isEqualTo("1.2.0");
	}

	@Test
	void updatePackages_shouldNotUninstallNorInstall_whenVersionIsAlreadyUpToDate() {
		final NpmPackageEntity existingEntity = npmPackageEntity("hl7.fhir.be.core", "1.2.0");
		when(inpmPackageDao.findAll()).thenReturn(List.of(existingEntity));

		final PackageInfo availablePackage = new PackageInfo("hl7.fhir.be.core", "1.2.0");
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of(availablePackage));

		implementationGuideService.updatePackages();

		verify(iPackageInstallerSvc, never()).uninstall(any());
		verify(iPackageInstallerSvc, never()).install(any());
	}

	@Test
	void updatePackages_shouldNotUninstallNorInstall_whenAvailableVersionIsOlder() {
		final NpmPackageEntity existingEntity = npmPackageEntity("hl7.fhir.be.core", "1.3.0");
		when(inpmPackageDao.findAll()).thenReturn(List.of(existingEntity));

		final PackageInfo availablePackage = new PackageInfo("hl7.fhir.be.core", "1.2.0");
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of(availablePackage));

		implementationGuideService.updatePackages();

		verify(iPackageInstallerSvc, never()).uninstall(any());
		verify(iPackageInstallerSvc, never()).install(any());
	}

	@Test
	void updatePackages_shouldHandleMultiplePackages_independently() {
		final NpmPackageEntity coreEntity = npmPackageEntity("hl7.fhir.be.core", "1.1.0");
		final NpmPackageEntity vaccinEntity = npmPackageEntity("hl7.fhir.be.vaccination", "2.0.0");
		when(inpmPackageDao.findAll()).thenReturn(List.of(coreEntity, vaccinEntity));

		final PackageInfo coreUpdate = new PackageInfo("hl7.fhir.be.core", "1.2.0");
		final PackageInfo vaccinUpToDate = new PackageInfo("hl7.fhir.be.vaccination", "2.0.0");
		final PackageInfo newPackage = new PackageInfo("hl7.fhir.be.laboratory", "1.0.0");
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of(coreUpdate, vaccinUpToDate, newPackage));

		implementationGuideService.updatePackages();

		verify(iPackageInstallerSvc, times(1)).uninstall(any());
		verify(iPackageInstallerSvc, times(2)).install(any());

		final ArgumentCaptor<PackageInstallationSpec> installCaptor = ArgumentCaptor.forClass(PackageInstallationSpec.class);
		verify(iPackageInstallerSvc, times(2)).install(installCaptor.capture());
		final List<String> installedNames = installCaptor.getAllValues().stream()
			.map(PackageInstallationSpec::getName)
			.toList();
		assertThat(installedNames).containsExactlyInAnyOrder("hl7.fhir.be.core", "hl7.fhir.be.laboratory");
	}

	@Test
	void updatePackages_shouldDoNothing_whenNoPackagesAvailable() {
		when(inpmPackageDao.findAll()).thenReturn(List.of());
		when(jsoupHtmlFetcher.fetchPackages()).thenReturn(List.of());

		implementationGuideService.updatePackages();

		verifyNoInteractions(iPackageInstallerSvc);
	}

	private NpmPackageEntity npmPackageEntity(final String packageId, final String currentVersionId) {
		final NpmPackageEntity entity = new NpmPackageEntity();
		entity.setPackageId(packageId);
		entity.setCurrentVersionId(currentVersionId);
		return entity;
	}

}