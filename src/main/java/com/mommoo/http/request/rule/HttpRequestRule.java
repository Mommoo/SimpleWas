package com.mommoo.http.request.rule;

import com.mommoo.http.request.HttpRequest;

public interface HttpRequestRule {
    public HttpRequestRuleResult isValidateRequest(HttpRequest httpRequest);
}
