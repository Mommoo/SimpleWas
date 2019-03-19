package com.mommoo.http;

import java.util.Arrays;

/**
 * https://developer.mozilla.org/ko/docs/Web/HTTP/Headers 스펙에서 필요한 몇몇 HTTP 스펙을 정의했습니다.
 *
 * 모든 HTTP 헤더 스펙을 구현하진 않았습니다.
 *
 * @author mommoo
 */
public enum HttpHeaderType {
    HOST("Host"),
    ACCEPT("Accept"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_ENCODING("Accept-Encoding"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    USER_AGENT("User-Agent"),
    CONNECTION("Connection"),
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length"),
    SERVER("Server"),
    DATE("Date");

    private final String text;

    private HttpHeaderType(String text) {
        this.text = text;
    }

    private boolean isLowerCaseMatched(String lowercaseString) {
        return this.text.toLowerCase().equals(lowercaseString);
    }

    public String getText(){
        return text;
    }

    public static HttpHeaderType of(String textHeaderType) {
        String lowerCaseText = textHeaderType.toLowerCase();
        return Arrays.stream(values())
                .filter(headerType -> headerType.isLowerCaseMatched(lowerCaseText))
                .findFirst()
                .orElse(null);
    }
}
