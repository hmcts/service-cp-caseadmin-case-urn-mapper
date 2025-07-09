package uk.gov.hmcts.cp.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {

    public static final String CASE_ID_BY_CASE_URN = "caseIdByCaseUrn";

}
