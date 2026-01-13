package uk.gov.hmcts.cp.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UrnMapperResponse {
    private String mappingId;
    private String sourceId;
    private String sourceType;
    private String targetId;
    private String targetType;
    private Instant createdAt;
}
