package uk.gov.hmcts.cp;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class UrnMapperApiTest {

    private final String baseUrl = System.getProperty("app.baseUrl", "http://localhost:4550");
    private final RestTemplate http = new RestTemplate();

    @Test
    void urn_mapper_should_return_caseId() {
        final ResponseEntity<String> res = http.exchange(
                baseUrl + "/urnmapper/CT98KRYCAP", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"caseId\":\"6c198796-08bb-4803-b456-fa0c29ca6021\"");
        assertThat(res.getBody()).contains("\"caseUrn\":\"CT98KRYCAP\"");
    }
}
