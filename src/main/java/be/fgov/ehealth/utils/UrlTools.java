package be.fgov.ehealth.utils;

import ca.uhn.fhir.rest.api.server.RequestDetails;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlTools {

	public static final String API_KEY = "api_key";

	private UrlTools() {
	}

	public static String stripApiKey(final RequestDetails requestDetails) {
		final String url = requestDetails.getCompleteUrl();
		if (url == null) return null;
		try {
			new URI(url);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		return url
			.replaceAll(API_KEY + "=[^&#]*&", "")
			.replaceAll("&" + API_KEY + "=[^&#]*", "")
			.replaceAll("\\?" + API_KEY + "=[^&#]*", "");

	}

}
