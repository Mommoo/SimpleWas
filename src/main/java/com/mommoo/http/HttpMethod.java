package com.mommoo.http;

import java.util.Arrays;

/**
 *  간단하게 테스트할 메서드 2개 {@link #GET}, {@link #POST} 만 작성했습니다.
 *  추후 다른 메서드의 처리가 필요하다면, 해당 클래스에 추가해야 합니다.
 */
public enum HttpMethod {
    GET("Get"), POST("Post");

    private final String TEXT;

    private HttpMethod(String TEXT) {
        this.TEXT = TEXT;
    }

    private boolean isLowerCaseMatched(String lowercaseString) {
        return this.TEXT.toLowerCase().equals(lowercaseString);
    }


    public static HttpMethod of(String textMethod) {
        String lowerCaseMethodText = textMethod.toLowerCase();

        return Arrays.stream(values())
                .filter(httpMethod -> httpMethod.isLowerCaseMatched(lowerCaseMethodText))
                .findFirst()
                .orElse(null);
    }
}
