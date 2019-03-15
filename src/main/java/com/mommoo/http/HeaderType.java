package com.mommoo.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * https://developer.mozilla.org/ko/docs/Web/HTTP/Headers
 */
public enum HeaderType {
    HOST("Host"),
    ACCEPT("Accept"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_ENCODING("Accept-Encoding"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    USER_AGENT("User-Agent"),
    CONNECTION("Connection"),
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length");

    private final String key;

    private HeaderType(String key) {
        this.key = key;
    }

    private boolean isLowerCaseMatched(String lowercaseString) {
        return this.key.toLowerCase().equals(lowercaseString);
    }

    public static HeaderType of(String textHeaderType) {
        String lowerCaseText = textHeaderType.toLowerCase();
        return Arrays.stream(values())
                .filter(headerType -> headerType.isLowerCaseMatched(lowerCaseText))
                .findFirst()
                .orElse(null);
    }
}
