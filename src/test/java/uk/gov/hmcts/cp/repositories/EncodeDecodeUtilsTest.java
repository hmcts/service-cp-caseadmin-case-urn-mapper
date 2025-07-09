package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.utils.EncodeDecodeUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncodeDecodeUtilsTest {

    @Test
    void shouldEncodeDecode() {
        final String value = "<script>ZXCqwe123£$^&*()[]{}.,'|`~<script>";
        final String encoded = EncodeDecodeUtils.encode(value);
        assertEquals("%3Cscript%3EZXCqwe123%C2%A3%24%5E%26*%28%29%5B%5D%7B%7D.%2C%27%7C%60%7E%3Cscript%3E", encoded);

        final String decoded = EncodeDecodeUtils.decode(encoded);
        assertEquals(value, decoded);
    }

}