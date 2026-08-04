package be.fgov.ehealth.service.impl;

import be.fgov.ehealth.domain.hapi.PackageInfo;
import be.fgov.ehealth.service.HtmlFetcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JsoupHtmlFetcher implements HtmlFetcher {

	@Override
	public List<PackageInfo> fetchPackages() {
		final List<PackageInfo> result = new ArrayList<>();
		try {
			final String url = "https://packages2.fhir.org/packages/catalog?name=hl7.fhir.be&dependson=&pkgcanonical=&canonical=&fhirversion=R4";
			final Document doc = Jsoup.connect(url)
				.header("Accept", "text/html,application/xhtml+xml")
				.header("User-Agent", "Mozilla/5.0 (compatible; FhirCatalogScraper/1.0)")
				.timeout(15000)
				.get();

			final Element table = doc.selectFirst("#segment-content > div > div > div > div > div.table-responsive > table");

			if (table == null) {
				return result;
			}

			final Elements rows = table.select("tbody > tr");
			for (final Element row : rows) {
				final Elements cells = row.select("> td");
				if (cells.size() >= 2) {
					final String name = cells.get(0).text().trim();
					final String rawVersion = cells.get(1).text().trim();
					final String version = rawVersion.split("\\(")[0].trim();
					result.add(new PackageInfo(name, version));
				}
			}

		} catch (final IOException e) {
			log.error(e.getMessage());
		}

		return result;
	}

}
