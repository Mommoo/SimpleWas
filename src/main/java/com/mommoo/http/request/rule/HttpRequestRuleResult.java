package com.mommoo.http.request.rule;

/**
 * 검증 결과를 정의한 클래스 입니다.
 * (검증 여부, 검증 메시지)
 *
 * @author mommoo
 */
public class HttpRequestRuleResult {
    public final boolean isValidate;
    public final String message;

    HttpRequestRuleResult(boolean isValidate, String message) {
        this.isValidate = isValidate;
        this.message = message;
    }

    public String toString() {
        return message;
    }
}
