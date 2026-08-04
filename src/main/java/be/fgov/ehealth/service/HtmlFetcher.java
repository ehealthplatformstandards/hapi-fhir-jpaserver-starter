package be.fgov.ehealth.service;

import be.fgov.ehealth.domain.hapi.PackageInfo;

import java.util.List;

public interface HtmlFetcher {

	List<PackageInfo> fetchPackages();

}
