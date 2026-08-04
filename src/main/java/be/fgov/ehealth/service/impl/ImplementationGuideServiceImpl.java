package be.fgov.ehealth.service.impl;

import be.fgov.ehealth.domain.hapi.PackageInfo;
import be.fgov.ehealth.service.ImplementationGuideService;
import ca.uhn.fhir.jpa.dao.data.INpmPackageDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageEntity;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImplementationGuideServiceImpl implements ImplementationGuideService {

	private final INpmPackageDao inpmPackageDao;
	private final JsoupHtmlFetcher jsoupHtmlFetcher;
	private final IPackageInstallerSvc iPackageInstallerSvc;

	public ImplementationGuideServiceImpl(final INpmPackageDao inpmPackageDao, final JsoupHtmlFetcher jsoupHtmlFetcher, final IPackageInstallerSvc iPackageInstallerSvc) {
		this.inpmPackageDao = inpmPackageDao;
		this.jsoupHtmlFetcher = jsoupHtmlFetcher;
		this.iPackageInstallerSvc = iPackageInstallerSvc;
	}

	@Override
	public List<PackageInfo> findPackagesToUpdate() {
		final List<NpmPackageEntity> existingPackages = inpmPackageDao.findAll();
		final List<PackageInfo> availablePackages = jsoupHtmlFetcher.fetchPackages();

		final Map<String, String> existingPackageVersions = toVersionMap(existingPackages);

		final List<PackageInfo> list = new ArrayList<>();
		for (final PackageInfo availablePackage : availablePackages) {
			final String currentVersion = existingPackageVersions.get(availablePackage.getName());
			if (currentVersion == null || isNewerVersion(availablePackage.getVersion(), currentVersion)) {
				list.add(availablePackage);
			}
		}
		return list;
	}

	@Override
	public void updatePackages() {
		final List<NpmPackageEntity> existingPackages = inpmPackageDao.findAll();
		final Map<String, String> existingPackageVersions = toVersionMap(existingPackages);
		final List<PackageInfo> availablePackages = jsoupHtmlFetcher.fetchPackages();

		for (final PackageInfo pkg : availablePackages) {
			final String currentVersion = existingPackageVersions.get(pkg.getName());
			if (currentVersion == null) {
				log.info("New package detected: {} ({})", pkg.getName(), pkg.getVersion());
				install(pkg.getName(), pkg.getVersion());
			} else if (isNewerVersion(pkg.getVersion(), currentVersion)) {
				log.info("Package update available for {} ({} -> {})", pkg.getName(), currentVersion, pkg.getVersion());
				update(pkg, currentVersion);
			}
		}
	}

	private Map<String, String> toVersionMap(final List<NpmPackageEntity> packages) {
		return packages.stream().collect(Collectors.toMap(NpmPackageEntity::getPackageId, NpmPackageEntity::getCurrentVersionId));
	}

	private boolean isNewerVersion(final String availableVersion, final String currentVersion) {
		final String[] availableParts = splitVersion(availableVersion);
		final String[] currentParts = splitVersion(currentVersion);
		final int maxLength = Math.max(availableParts.length, currentParts.length);
		for (int i = 0; i < maxLength; i++) {
			final int availablePart = getVersionPart(availableParts, i);
			final int currentPart = getVersionPart(currentParts, i);
			if (availablePart > currentPart) {
				return true;
			}
		}

		return false;
	}

	private void update(final PackageInfo newPackageInfo, final String currentVersion) {
		uninstall(newPackageInfo.getName(), currentVersion);
		install(newPackageInfo.getName(), newPackageInfo.getVersion());
	}

	private void uninstall(final String name, final String version) {
		log.info("Uninstalling package {} version {}", name, version);
		try {
			iPackageInstallerSvc.uninstall(new PackageInstallationSpec().setName(name).setVersion(version));
			log.info("Successfully uninstalled package {} version {}", name, version);
		} catch (final Exception e) {
			log.error("Failed to uninstall package {} version {}", name, version, e);
			throw e;
		}
	}

	private void install(final String name, final String version) {
		log.info("Installing package {} version {}", name, version);
		try {
			final PackageInstallationSpec packageInstallationSpec = new PackageInstallationSpec()
				.setName(name)
				.setVersion(version)
				.setFetchDependencies(true)
				.setPackageUrl("https://packages.fhir.org/" + name + "/" + version)
				.addDependencyExclude("hl7.fhir.r4.core");

			iPackageInstallerSvc.install(packageInstallationSpec);
			log.info("Successfully installed package {} version {}", name, version);
		} catch (final Exception e) {
			log.error("Failed to install package {} version {}", name, version, e);
			throw e;
		}
	}

	private String[] splitVersion(final String version) {
		return version.split("\\.");
	}

	private int getVersionPart(final String[] parts, final int index) {
		return index < parts.length ? Integer.parseInt(parts[index]) : 0;
	}

}
