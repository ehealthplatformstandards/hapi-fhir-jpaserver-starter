package be.fgov.ehealth.scheduling;

import be.fgov.ehealth.service.ImplementationGuideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IGScheduler {

	private final ImplementationGuideService implementationGuideService;

	public IGScheduler(final ImplementationGuideService implementationGuideService) {
		this.implementationGuideService = implementationGuideService;
	}

	@Scheduled(cron = "0 0 0 * * MON")
	public void process() {
		log.info("Starting scheduled update of implementation guide packages.");
		try {
			implementationGuideService.updatePackages();
			log.info("Successfully updated implementation guide packages.");
		} catch (final Exception e) {
			log.error("Failed to update implementation guide packages.", e);
			throw e;
		}
	}

}
