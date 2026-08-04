package be.fgov.ehealth.service.impl;

import be.fgov.ehealth.domain.hapi.PackageInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsoupHtmlHtmlFetcherTest {

	private static final String HTML_WITH_TABLE = """
		<html>
		  <body>
		    <div id="segment-content">
		      <div><div><div><div>
		        <div class="table-responsive">
		          <table class="table table-striped">
		            <thead>
		              <tr><th>Package</th><th>Version</th></tr>
		            </thead>
		            <tbody>
		              <tr>
		                <td><a href="https://packages2.fhir.org/web/hl7.fhir.be.lab-1.0.0.tgz">hl7.fhir.be.lab</a></td>
		                <td>1.0.0 (<a href="/packages/hl7.fhir.be.lab">all</a>)</td>
		              </tr>
		              <tr>
		                <td><a href="https://packages2.fhir.org/web/hl7.fhir.be.core-2.1.2.tgz">hl7.fhir.be.core</a></td>
		                <td>2.1.2 (<a href="/packages/hl7.fhir.be.core">all</a>)</td>
		              </tr>
		            </tbody>
		          </table>
		        </div>
		      </div></div></div></div>
		    </div>
		  </body>
		</html>
		""";

	private static final String HTML_WITHOUT_TABLE = """
		<html>
		  <body>
		    <div id="segment-content">
		      <p>Aucun résultat</p>
		    </div>
		  </body>
		</html>
		""";

	@InjectMocks
	private JsoupHtmlFetcher jsoupHtmlFetcher;
	@Mock
	private Connection connectionMock;

	@BeforeEach
	void setUp() {
		when(connectionMock.timeout(anyInt())).thenReturn(connectionMock);
	}

	private MockedStatic<Jsoup> mockJsoupConnect(final Answer<Document> answer) throws IOException {
		when(connectionMock.get()).thenAnswer(answer);

		final MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class);
		mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(connectionMock);

		return mockedJsoup;
	}

	@Test
	void fetchPackages_shouldReturnNameAndVersion_whenTablePresent() throws IOException {
		final Document parsedDoc = Jsoup.parse(HTML_WITH_TABLE);

		try (final MockedStatic<Jsoup> ignored = mockJsoupConnect(invocation -> parsedDoc)) {
			final List<PackageInfo> result = jsoupHtmlFetcher.fetchPackages();

			assertEquals(2, result.size());

			assertEquals("hl7.fhir.be.lab", result.get(0).getName());
			assertEquals("1.0.0", result.get(0).getVersion());

			assertEquals("hl7.fhir.be.core", result.get(1).getName());
			assertEquals("2.1.2", result.get(1).getVersion());
		}
	}

	@Test
	void fetchPackages_shouldReturnEmptyList_whenTableNotFound() throws IOException {
		final Document parsedDoc = Jsoup.parse(HTML_WITHOUT_TABLE);
		try (final MockedStatic<Jsoup> ignored = mockJsoupConnect(invocation -> parsedDoc)) {
			final List<PackageInfo> result = jsoupHtmlFetcher.fetchPackages();

			assertNotNull(result);
			assertTrue(result.isEmpty());
		}
	}

	@Test
	void fetchPackages_shouldReturnEmptyList_whenIOExceptionOccurs() throws IOException {
		try (final MockedStatic<Jsoup> ignored = mockJsoupConnect(invocation -> {
			throw new IOException("boom");
		})) {
			final List<PackageInfo> result = jsoupHtmlFetcher.fetchPackages();
			assertNotNull(result);
			assertTrue(result.isEmpty());
		}
	}

	@Test
	void fetchPackages_shouldStripAllSuffix_fromVersionColumn() throws IOException {
		final Document parsedDoc = Jsoup.parse(HTML_WITH_TABLE);
		try (final MockedStatic<Jsoup> ignored = mockJsoupConnect(invocation -> parsedDoc)) {
			final List<PackageInfo> result = jsoupHtmlFetcher.fetchPackages();

			result.forEach(pkg -> assertFalse(pkg.getVersion().contains("(")));
		}

	}

}