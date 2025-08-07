package uk.gov.hmcts.cp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseUrnMapperService {

    private static final Logger LOG = LoggerFactory.getLogger(CaseUrnMapperService.class);

    private final CaseUrnMapperRepository caseUrnMapperRepository;

    public CaseMapperResponse getCaseIdByCaseUrn(final String caseUrn, final Boolean refresh) throws ResponseStatusException {
        if (StringUtils.isEmpty(caseUrn)) {
            log.warn("No case urn provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "caseUrn is required");
        }
        final CaseMapperResponse caseMapperResponse = caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn, refresh);
        log.debug("Case Mapper response: {}", caseMapperResponse);
        return caseMapperResponse;
    }

}
