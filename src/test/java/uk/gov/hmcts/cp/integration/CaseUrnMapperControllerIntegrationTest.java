package uk.gov.hmcts.cp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.utils.EncodeDecodeUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "case-urn-mapper.url=https://ENTER_CORRECT_DOMAIN.org.uk",
        "case-urn-mapper.path=/ENTER/CORRECT/PATH",
        "case-urn-mapper.cjscppuid=ENTER-CORRECT-CJSCPPUID"
})
@AutoConfigureMockMvc
class CaseUrnMapperControllerIntegrationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvided() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshTrue() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshOne() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=1", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshFalse() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshZero() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=0", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshMissing() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldReturnOkWhenValidUrnIsProvidedAndRefreshMissingWithEquality() throws Exception {
        final String caseUrn = "CIK2JQKECS";
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse caseMapperResponse = objectMapper.readValue(responseBody, CaseMapperResponse.class);

                    assertEquals(caseUrn, caseMapperResponse.getCaseUrn());
                    assertNotNull(caseMapperResponse.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", caseMapperResponse.getCaseId());
                });
    }

    //    @Test
    void shouldRefreshResponse() throws Exception {
        AtomicReference<CaseMapperResponse> caseMapperResponse = new AtomicReference<>();

        final String caseUrn = "CIK2JQKECS";

        // perform mapping search, value should be cached
        mockMvc.perform(get("/urnmapper/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    caseMapperResponse.set(objectMapper.readValue(responseBody, CaseMapperResponse.class));

                    CaseMapperResponse response = caseMapperResponse.get();
                    assertEquals(caseUrn, response.getCaseUrn());
                    assertNotNull(response.getCaseId());
                    assertEquals("f552dee6-f092-415b-839c-5e5b5f46635e", response.getCaseId());
                });

        // perform mapping search again, same value should be returned
        mockMvc.perform(get("/urnmapper/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });

        // perform mapping search again, same value should be returned
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=false", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });

        // perform mapping search again, same value should be returned
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=0", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });

        // perform mapping search again, a new value should be generated, cached and returned
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                    assertEquals(caseMapperResponse.get().getCaseUrn(), response.getCaseUrn());
                    assertEquals(caseMapperResponse.get().getCaseId(), response.getCaseId());
                    caseMapperResponse.set(response);
                });

        // perform mapping search again, same value should be returned as previously changed
        mockMvc.perform(get("/urnmapper/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    CaseMapperResponse response = objectMapper.readValue(responseBody, CaseMapperResponse.class);
                    assertEquals(caseMapperResponse.get(), response);
                });
    }

    //    @Test
    void shouldReturnNotFound() throws Exception {
        final String caseUrn = EncodeDecodeUtils.encode("<script>ZXCqwe123£$^&*()[]{}.,'|`~<script>");
        assertEquals("%3Cscript%3EZXCqwe123%C2%A3%24%5E%26*%28%29%5B%5D%7B%7D.%2C%27%7C%60%7E%3Cscript%3E", caseUrn);

        mockMvc.perform(get("/urnmapper/{case_urn}", caseUrn).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    Map body = objectMapper.readValue(responseBody, Map.class);
                    assertEquals("404", body.get("error"));
                    assertTrue(((String) body.get("message")).startsWith("Case not found by urn: " + caseUrn));
                });
    }

}