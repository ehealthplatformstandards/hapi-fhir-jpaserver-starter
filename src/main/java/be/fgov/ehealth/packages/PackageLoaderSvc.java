package be.fgov.ehealth.packages;

/*-
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2024 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.packages.loader.NpmPackageData;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.util.ClasspathUtil;
import jakarta.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.SimpleHTTPClient;
import org.hl7.fhir.utilities.json.JsonUtilities;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.json.parser.JsonParser;
import org.hl7.fhir.utilities.npm.BasePackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PackageLoaderSvc extends ca.uhn.fhir.jpa.packages.loader.PackageLoaderSvc {

	private static final Logger ourLog = LoggerFactory.getLogger(PackageLoaderSvc.class);

	public PackageLoaderSvc(){
		ourLog.info("Loaded be.fgov.ehealth.packages.PackageLoaderSvc");
	}


	/**
	 * Loads the package, but won't save it anywhere.
	 * Returns the data to the caller
	 *
	 * @return - a POJO containing information about the NpmPackage, as well as it's contents
	 * 			as fetched from the server
	 * @throws IOException
	 */
	public NpmPackageData fetchPackageFromPackageSpec(String thePackageId, String thePackageVersion)
		throws FHIRException, IOException {
		if (StringUtils.isNotBlank(thePackageVersion) && thePackageVersion.startsWith("current")){
			 JsonObject list = JsonParser.parseObjectFromUrl("https://raw.githubusercontent.com/FHIR/ig-registry/master/fhir-ig-list.json");
			 JsonObject guide = getGuide(list, thePackageId);
			 if (guide == null){
				 throw new IOException(String.format("Could not find IG %s in fhir-ig-list.json",thePackageId));
			 }
			 String ciBuild = guide.asString("ci-build");
			 if (ciBuild == null){
				 throw new IOException(String.format("Could not find ci-build for IG %s in fhir-ig-list.json",thePackageId));
			 }

			 String source = ciBuild + (thePackageVersion.endsWith("current")?"":("/branches/"+thePackageVersion.substring(8))) + "/package.tgz";

			SimpleHTTPClient fetcher = new SimpleHTTPClient();
			fetcher.addHeader("Accept", "application/octet-stream");
			SimpleHTTPClient.HTTPResult res = fetcher.get(source+"?nocache=" + System.currentTimeMillis());
			res.checkThrowException();
			NpmPackageData temp =  createNpmPackageDataFromData(
				thePackageId, thePackageVersion, source, new ByteArrayInputStream(res.getContent()));
			String version = temp.getPackage().version();
			return  createNpmPackageDataFromData(
				thePackageId, version, source, new ByteArrayInputStream(res.getContent()));



		}
		return fetchPackageFromServerInternal(thePackageId, thePackageVersion);
	}
	private JsonObject getGuide(JsonObject json, String pid) {
		for (JsonObject o : json.getJsonObjects("guides")) {
			if (pid.equals(o.asString("npm-name"))) {
				return o;
			}
		}
		return null;
	}

	private NpmPackageData fetchPackageFromServerInternal(String thePackageId, String thePackageVersion)
		throws IOException {
		BasePackageCacheManager.InputStreamWithSrc pkg = this.loadFromPackageServer(thePackageId, thePackageVersion);

		if (pkg == null) {
			throw new ResourceNotFoundException(
				Msg.code(1301) + "Unable to locate package " + thePackageId + "#" + thePackageVersion);
		}

		NpmPackageData npmPackage = createNpmPackageDataFromData(
			thePackageId, thePackageVersion == null ? pkg.version : thePackageVersion, pkg.url, pkg.stream);

		return npmPackage;
	}


}

