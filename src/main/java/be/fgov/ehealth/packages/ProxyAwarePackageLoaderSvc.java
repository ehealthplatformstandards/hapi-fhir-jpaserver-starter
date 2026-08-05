package be.fgov.ehealth.packages;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.packages.loader.PackageLoaderSvc;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.util.ClasspathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ProxyAwarePackageLoaderSvc extends PackageLoaderSvc {

	@Override
	public byte[] loadPackageUrlContents(final String thePackageUrl) {
		log.info("Loading ProxyAwarePackageLoaderSvc");
		if (thePackageUrl.startsWith("classpath:")) {
			return ClasspathUtil.loadResourceAsByteArray(thePackageUrl.substring("classpath:".length()));
		} else if (thePackageUrl.startsWith("file:")) {
			try {
				return Files.readAllBytes(Paths.get(new URI(thePackageUrl)));
			} catch (final IOException | URISyntaxException e) {
				throw new InternalErrorException(
					Msg.code(2031) + "Error loading \"" + thePackageUrl + "\": " + e.getMessage());
			}
		} else {
			final HttpClientConnectionManager connManager = new BasicHttpClientConnectionManager();
			try (final CloseableHttpResponse request = HttpClientBuilder.create()
				.setConnectionManager(connManager)
				.useSystemProperties()
				.build()
				.execute(new HttpGet(thePackageUrl))) {
				if (request.getStatusLine().getStatusCode() != 200) {
					throw new ResourceNotFoundException(Msg.code(1303) + "Received HTTP "
						+ request.getStatusLine().getStatusCode() + " from URL: " + thePackageUrl);
				}
				return IOUtils.toByteArray(request.getEntity().getContent());
			} catch (final IOException e) {
				throw new InternalErrorException(
					Msg.code(1304) + "Error loading \"" + thePackageUrl + "\": " + e.getMessage());
			}
		}
	}

}
