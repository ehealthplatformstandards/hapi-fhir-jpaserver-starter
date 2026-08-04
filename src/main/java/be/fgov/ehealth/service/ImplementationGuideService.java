package be.fgov.ehealth.service;

import be.fgov.ehealth.domain.hapi.PackageInfo;

import java.util.List;

public interface ImplementationGuideService {

	List<PackageInfo> findPackagesToUpdate();

	void updatePackages();
	
}
