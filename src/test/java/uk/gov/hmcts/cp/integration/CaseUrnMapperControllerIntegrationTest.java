package uk.gov.hmcts.cp.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.client.UrnMapperResponse;
import uk.gov.hmcts.cp.config.AppProperties;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.client.CaseUrnMapperClient.CJSCPPUID_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
class CaseUrnMapperControllerIntegrationTest {

    @Autowired
    AppProperties appProperties;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    RestTemplate restTemplate;

    private String caseUrn = "CIK2JQKECS";
    private String caseId = "f552dee6-f092-415b-839c-5e5b5f46635e";

    @Test
    void refresh_false_should_return_ok() throws Exception {
        UrnMapperResponse response = UrnMapperResponse.builder().sourceId(caseUrn).targetId(caseId).build();
        mockRestResponse(HttpStatus.OK, response);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));
    }

    @Test
    void refresh_true_should_return_ok() throws Exception {
        UrnMapperResponse response = UrnMapperResponse.builder().sourceId(caseUrn).targetId(caseId).build();
        mockRestResponse(HttpStatus.OK, response);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));
    }

    @Test
    void refresh_false_should_return_cached_value() throws Exception {
        UrnMapperResponse response1 = UrnMapperResponse.builder().sourceId(caseUrn).targetId(caseId).build();
        mockRestResponse(HttpStatus.OK, response1);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));

        UrnMapperResponse response2 = UrnMapperResponse.builder().sourceId(caseUrn).targetId("ANOTHER").build();
        mockRestResponse(HttpStatus.OK, response2);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value(caseUrn))
                .andExpect(jsonPath("$.caseId").value(caseId));
    }

    @Test
    void refresh_true_should_return_new_value() throws Exception {
        UrnMapperResponse response1 = UrnMapperResponse.builder().sourceId("DAAA123123").targetId("ORIG").build();
        mockRestResponse(HttpStatus.OK, response1);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", "DAAA123123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value("DAAA123123"))
                .andExpect(jsonPath("$.caseId").value("ORIG"));

        UrnMapperResponse response2 = UrnMapperResponse.builder().sourceId("DAAA123123").targetId("CHANGED").build();
        mockRestResponse(HttpStatus.OK, response2);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", "DAAA123123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseUrn").value("DAAA123123"))
                .andExpect(jsonPath("$.caseId").value("CHANGED"));
    }

    @Test
    void not_exist_should_throw_404() throws Exception {
        UrnMapperResponse response = UrnMapperResponse.builder().sourceId(caseUrn).build();
        mockRestResponse(HttpStatus.NOT_FOUND, response);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("404 NOT_FOUND"));
    }

    @Test
    void certificate_error_should_return_500() throws Exception {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(expectedRequest()),
                eq(UrnMapperResponse.class)
        )).thenThrow(new RuntimeException("SSL certificate problem: unable to get local issuer certificate"));
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("SSL certificate problem: unable to get local issuer certificate"));
    }

    private void mockRestResponse(HttpStatus httpStatus, UrnMapperResponse urnMapperResponse) {
        String expectedUrl = String.format("%s%s?sourceId=%s&targetType=CASE_FILE_ID", appProperties.getBackendUrl(), appProperties.getBackendPath(), urnMapperResponse.getSourceId());
        log.info("Mocking {}", expectedUrl);
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedRequest()),
                eq(UrnMapperResponse.class)
        )).thenReturn(new ResponseEntity<>(urnMapperResponse, httpStatus));
    }

    private HttpEntity expectedRequest() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.systemid.mapping+json");
        headers.add(CJSCPPUID_HEADER, appProperties.getBackendCjscppuid());
        return new HttpEntity<>(headers);
    }
}