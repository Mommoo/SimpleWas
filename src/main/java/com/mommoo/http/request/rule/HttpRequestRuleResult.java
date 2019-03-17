package com.mommoo.http.request.rule;

public class HttpRequestRuleResult {
    public final boolean isValidate;
    public final String message;

    public HttpRequestRuleResult(boolean isValidate, String message) {
        this.isValidate = isValidate;
        this.message = message;
    }
}
