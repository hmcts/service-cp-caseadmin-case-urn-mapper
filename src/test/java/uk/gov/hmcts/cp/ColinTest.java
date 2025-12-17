package uk.gov.hmcts.cp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ObjectUtils.isEmpty;

public class ColinTest {

    @Test
    void checkjsEmpty() {
        assertThat(isEmpty(null)).isTrue();
        assertThat(isEmpty("")).isTrue();
        assertThat(isEmpty("a")).isFalse();
    }
}
