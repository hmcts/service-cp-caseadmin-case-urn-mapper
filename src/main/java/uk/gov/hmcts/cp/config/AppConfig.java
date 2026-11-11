package uk.gov.hmcts.cp.config;

import java.net.http.HttpClient;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // A bare new RestTemplate() uses HttpURLConnection, which keeps at most 5 idle connections per
    // host. Under concurrent load the rest are closed after each call, and their TIME_WAIT sockets
    // exhaust the ephemeral port range ("Cannot assign requested address"). The JDK HttpClient
    // pools and reuses connections. HTTP/1.1 avoids an h2c upgrade attempt on plain-http backends.
    // Building from RestTemplateBuilder applies spring.http.clients.* (timeouts, redirects) and the
    // tracing customizer, so backend calls get client spans and propagate the trace context.
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        return builder
                .requestFactoryBuilder(ClientHttpRequestFactoryBuilder.jdk()
                        .withHttpClientCustomizer(httpClient -> httpClient.version(HttpClient.Version.HTTP_1_1)))
                .build();
    }
}
