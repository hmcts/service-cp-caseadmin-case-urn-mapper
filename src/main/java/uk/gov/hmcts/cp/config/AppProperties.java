package uk.gov.hmcts.cp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class AppProperties {

    private final String backendUrl;
    private final String backendPath;
    private final String backendCjscppuid;

    public AppProperties(
            @Value("${case-urn-mapper.url}") final String backendUrl,
            @Value("${case-urn-mapper.path}") final String backendPath,
            @Value("${case-urn-mapper.cjscppuid}") final String backendCjscppuid) {
        this.backendUrl = backendUrl;
        this.backendPath = backendPath;
        this.backendCjscppuid = backendCjscppuid;
    }
}
