package be.fgov.ehealth.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Setter
@ConfigurationProperties(prefix = "proxy")
@Profile("!local")
public class ProxyConfig {

	private static final List<String> DEFAULT_WHITELIST = List.of(
		"services.ehealth.fgov.be",
		"services-acpt.ehealth.fgov.be",
		"packages2.fhir.org",
		"hl7.org",
		"tx.fhir.org",
		"packages.fhir.org"
	);

	private final String host = "proxyapp.ehealth.fgov.be";
	private final int port = 8080;
	private List<String> whitelist;

	@PostConstruct
	public void init() {
		final List<String> fullWhislist = new ArrayList<>(DEFAULT_WHITELIST);
		if (whitelist != null && !whitelist.isEmpty()) {
			fullWhislist.addAll(whitelist);
		}
		final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
		ProxySelector.setDefault(new WhitelistProxySelector(fullWhislist, proxy));
		log.info("Proxy configured for hosts: {}", fullWhislist);
	}

}
