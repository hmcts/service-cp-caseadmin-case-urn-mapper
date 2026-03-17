package uk.gov.hmcts.cp.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cp.client.UrnMapperResponse;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class CaseUrnMapperControllerIntegrationTest {

    @Autowired
    AppPropertiesBackend appProperties;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    private String caseUrn = "CIK2JQKECS";
    private String caseId = "f552dee6-f092-415b-839c-5e5b5f46635e";

    protected WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void afterEach() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void refresh_false_should_return_ok() throws Exception {
        UrnMapperResponse response = UrnMapperResponse.builder().sourceId(caseUrn).targetId(caseId).build();
        stubMappingResponse(response);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));
    }

    @Test
    void refresh_true_should_return_ok() throws Exception {
        UrnMapperResponse response = UrnMapperResponse.builder().sourceId(caseUrn).targetId(caseId).build();
        stubMappingResponse(response);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));
    }

    @Test
    void refresh_false_should_return_cached_value() throws Exception {
        UrnMapperResponse response1 = UrnMapperResponse.builder().sourceId(caseUrn).targetId(caseId).build();
        stubMappingResponse(response1);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));

        UrnMapperResponse response2 = UrnMapperResponse.builder().sourceId(caseUrn).targetId("ANOTHER").build();
        stubMappingResponse(response2);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));
    }

    @Test
    void refresh_true_should_return_new_value() throws Exception {
        UrnMapperResponse response1 = UrnMapperResponse.builder().sourceId("DAAA123123").targetId("ORIG").build();
        stubMappingResponse(response1);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", "DAAA123123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value("DAAA123123"))
                .andExpect(jsonPath("$.caseId").value("ORIG"));

        UrnMapperResponse response2 = UrnMapperResponse.builder().sourceId("DAAA123123").targetId("CHANGED").build();
        stubMappingResponse(response2);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", "DAAA123123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value("DAAA123123"))
                .andExpect(jsonPath("$.caseId").value("CHANGED"));
    }

    @Test
    void not_exist_should_throw_404() throws Exception {
        String expectedUrl = String.format("%s?sourceId=%s&targetType=CASE_FILE_ID", appProperties.getBackendPath(), caseUrn);
        log.info("Mocking {} error", expectedUrl);
        ResponseDefinitionBuilder mockResponse = aResponse()
                .withStatus(HTTP_NOT_FOUND)
                .withHeader("Content-Type", "application/json");
        log.info("Stubbing mapping url:{}", expectedUrl);
        stubFor(WireMock.get(urlEqualTo(expectedUrl)).willReturn(mockResponse));

        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void empty_caseUrn_should_throw_404() throws Exception {
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", Strings.EMPTY))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void certificate_error_should_return_500() throws Exception {
        String expectedUrl = String.format("%s?sourceId=%s&targetType=CASE_FILE_ID", appProperties.getBackendPath(), caseUrn);
        log.info("Mocking {} error", expectedUrl);
        ResponseDefinitionBuilder mockResponse = aResponse()
                .withStatus(HTTP_INTERNAL_ERROR)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"SSL certificate error\"}");
        log.info("Stubbing mapping url:{}", expectedUrl);
        stubFor(WireMock.get(urlEqualTo(expectedUrl)).willReturn(mockResponse));

        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }

    private void stubMappingResponse(UrnMapperResponse urnMapperResponse) throws JsonProcessingException {
        String expectedUrl = String.format("%s?sourceId=%s&targetType=CASE_FILE_ID", appProperties.getBackendPath(), urnMapperResponse.getSourceId());
        ResponseDefinitionBuilder mockResponse = aResponse()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(MAPPER.writeValueAsString(urnMapperResponse));
        log.info("Stubbing mapping url:{}", expectedUrl);
        stubFor(WireMock.get(urlEqualTo(expectedUrl)).willReturn(mockResponse));
    }
}