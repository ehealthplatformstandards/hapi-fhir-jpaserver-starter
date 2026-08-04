package be.fgov.ehealth.controller;

import be.fgov.ehealth.domain.hapi.PackageInfo;
import be.fgov.ehealth.service.ImplementationGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "eHealth Management", description = "Endpoints de gestion des Implementation Guides")
@RequestMapping(value = "/api/admin")
@Slf4j
public class EHealthController {

	private final ImplementationGuideService implementationGuideService;

	public EHealthController(final ImplementationGuideService implementationGuideService) {
		this.implementationGuideService = implementationGuideService;
	}

	@GetMapping("/findPackagesToUpdate")
	@Operation(summary = "Trouver les packages à mettre à jour")
	public List<PackageInfo> findPackagesToUpdate() {
		return implementationGuideService.findPackagesToUpdate();
	}

	@PostMapping("/updatePackages")
	public void updatePackages() {
		implementationGuideService.updatePackages();
	}

}
