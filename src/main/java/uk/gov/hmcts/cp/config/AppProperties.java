package uk.gov.hmcts.cp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class AppProperties {

    private String backendUrl;
    private String backendPath;
    private String backendCjscppuid;

    public AppProperties(
            @Value("${case-urn-mapper.url}") String backendUrl,
            @Value("${case-urn-mapper.path}") String backendPath,
            @Value("${case-urn-mapper.cjscppuid}") String backendCjscppuid) {
        this.backendUrl = backendUrl;
        this.backendPath = backendPath;
        this.backendCjscppuid = backendCjscppuid;
    }
}
