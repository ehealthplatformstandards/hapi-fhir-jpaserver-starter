package be.fgov.ehealth.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class WhitelistProxySelector extends ProxySelector {

	private static final Logger logger = LoggerFactory.getLogger(WhitelistProxySelector.class);

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
		logger.error("Proxy connection failed for {}: {}", uri, ioe.getMessage());
	}

}
