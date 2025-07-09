package uk.gov.hmcts.cp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.utils.EncodeDecodeUtils;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CaseUrnMapperControllerIT {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOkWhenValidUrnIsProvided() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshTrue() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}?refresh=true", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshOne() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}?refresh=1", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshFalse() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}?refresh=false", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshZero() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}?refresh=0", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshMissing() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}?refresh", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshMissingWithEquality() throws Exception {
        final String caseUrn = "test-case-urn";
        mockMvc.perform(post("/case/{case_urn}?refresh=", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertTrue(caseMapperResponse.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });
    }

    @Test
    void shouldRefreshResponse() throws Exception {
        AtomicReference<CaseMapperResponse> caseMapperResponse = new AtomicReference<>();

        final String caseUrn = EncodeDecodeUtils.encode("<script>ZXCqwe123£$^&*()[]{}.,'|`~<script>");
        assertEquals("%3Cscript%3EZXCqwe123%C2%A3%24%5E%26*%28%29%5B%5D%7B%7D.%2C%27%7C%60%7E%3Cscript%3E", caseUrn);

        // perform mapping search, value should be cached
        mockMvc.perform(post("/case/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    caseMapperResponse.set(objectMapper.readValue(responseBody, CaseMapperResponse.class));

                    CaseMapperResponse response = caseMapperResponse.get();
                    assertEquals(caseUrn, response.getCaseUrn());
                    assertNotNull(response.getCaseId());
                    assertTrue(response.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
                });

        // perform mapping search again, same value should be returned
        mockMvc.perform(post("/case/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });

        // perform mapping search again, same value should be returned
        mockMvc.perform(post("/case/{case_urn}?refresh=false", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });

        // perform mapping search again, same value should be returned
        mockMvc.perform(post("/case/{case_urn}?refresh=0", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });

        // perform mapping search again, a new value should be generated, cached and returned
        mockMvc.perform(post("/case/{case_urn}?refresh=true", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertNotEquals(caseMapperResponse.get(), response);
                    assertEquals(caseMapperResponse.get().getCaseUrn(), response.getCaseUrn());
                    assertNotEquals(caseMapperResponse.get().getCaseId(), response.getCaseId());
                    caseMapperResponse.set(response);
                });

        // perform mapping search again, same value should be returned as previously changed
        mockMvc.perform(post("/case/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });
    }

}