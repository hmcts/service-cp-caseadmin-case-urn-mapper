package uk.gov.hmcts.cp.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.cp.client.CaseUrnMapperClient;
import uk.gov.hmcts.cp.client.UrnMapperResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real RestTemplate wiring against WireMock: headers, redirects, timeouts and
 * trace propagation. The unit tests mock RestTemplate, so they cannot catch changes here.
 */
@SpringBootTest
class CaseUrnMapperClientIntegrationTest {

    private static final String MAPPINGS_PATH = "/system-id-mapper-api/rest/systemid/mappings";
    private static final String MAPPING_BODY = """
            {"sourceId":"CIK2JQKECS","targetId":"f552dee6-f092-415b-839c-5e5b5f46635e"}""";

    @Autowired
    private CaseUrnMapperClient client;

    private WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
    }

    private static void stubMapping() {
        stubFor(WireMock.get(urlPathEqualTo(MAPPINGS_PATH))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(MAPPING_BODY)));
    }

    @Test
    void sends_accept_cjscppuid_and_traceparent_headers() {
        stubMapping();

        final UrnMapperResponse response = client.getCaseFileByCaseUrn("CIK2JQKECS").getBody();

        assertThat(response.getTargetId()).isEqualTo("f552dee6-f092-415b-839c-5e5b5f46635e");
        verify(getRequestedFor(urlPathEqualTo(MAPPINGS_PATH))
                .withHeader("Accept", equalTo("application/vnd.systemid.mapping+json"))
                .withHeader(CaseUrnMapperClient.CJSCPPUID_HEADER, equalTo("00000000-0000-0000-0000-000000000000"))
                .withHeader("traceparent", matching("00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}")));
    }

    @Test
    void follows_redirects() {
        stubFor(WireMock.get(urlPathEqualTo(MAPPINGS_PATH))
                .willReturn(aResponse().withStatus(302).withHeader("Location", "/moved")));
        stubFor(WireMock.get(urlPathEqualTo("/moved"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(MAPPING_BODY)));

        final UrnMapperResponse response = client.getCaseFileByCaseUrn("CIK2JQKECS").getBody();

        assertThat(response.getSourceId()).isEqualTo("CIK2JQKECS");
    }

    @Nested
    @TestPropertySource(properties = {
        "spring.http.clients.read-timeout=300ms",
        "case-urn-mapper.cjscppuid="
    })
    class WithShortTimeoutAndBlankCjscppuid {

        @Test
        void times_out_when_backend_does_not_answer() {
            stubFor(WireMock.get(urlPathEqualTo(MAPPINGS_PATH))
                    .willReturn(aResponse().withFixedDelay(3000).withBody(MAPPING_BODY)));

            assertThatThrownBy(() -> client.getCaseFileByCaseUrn("CIK2JQKECS"))
                    .isInstanceOf(ResourceAccessException.class);
        }

        @Test
        void sends_empty_cjscppuid_when_not_configured() {
            stubMapping();

            client.getCaseFileByCaseUrn("CIK2JQKECS");

            verify(getRequestedFor(urlPathEqualTo(MAPPINGS_PATH))
                    .withHeader(CaseUrnMapperClient.CJSCPPUID_HEADER, equalTo("")));
        }
    }
}
