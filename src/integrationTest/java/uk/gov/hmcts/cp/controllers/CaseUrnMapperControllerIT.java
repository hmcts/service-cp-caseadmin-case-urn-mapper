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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                    assertEquals(caseUrn + "-THIS-IS-CASE-ID", caseMapperResponse.getCaseId());
                });
    }
}