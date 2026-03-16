package be.fgov.ehealth.configuration;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@Slf4j
public class WhitelistProxySelector extends ProxySelector {

	private final List<String> whitelist;
	private final Proxy proxy;

	public WhitelistProxySelector(final List<String> whitelist, final Proxy proxy) {
		this.whitelist = whitelist;
		this.proxy = proxy;
	}

	@Override
	public List<Proxy> select(final URI uri) {
		final String host = uri.getHost();
		if (host != null && whitelist.stream().anyMatch(allowed -> host.equals(allowed) || host.endsWith("." + allowed))) {
			return Collections.singletonList(proxy);
		}
		return Collections.singletonList(Proxy.NO_PROXY);
	}

	@Override
	public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
		log.error("Proxy connection failed for {}: {}", uri, ioe.getMessage());
	}

}
