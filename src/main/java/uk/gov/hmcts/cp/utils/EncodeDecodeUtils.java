package uk.gov.hmcts.cp.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class EncodeDecodeUtils {

    @SneakyThrows
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }



}
