package uk.gov.hmcts.cp.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void short_case_urn_should_throw_400() throws Exception {
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", "short"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Case urn must be between 10 and 40 alphanumerics"));
    }

    @Test
    void long_case_urn_should_throw_400() throws Exception {
        String longCaseUrn = String.format("%41d", 1);
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", longCaseUrn))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Case urn must be between 10 and 40 alphanumerics"));
    }

    @Test
    void empty_case_urn_should_throw_404() throws Exception {
        mockMvc.perform(get("/urnmapper/{case_urn}?refresh=true", ""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No endpoint GET /urnmapper/."));
    }

    @Test
    void random_urn_should_throw_404() throws Exception {
        mockMvc.perform(get("/something-else", ""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No endpoint GET /something-else."));
    }
}