package com.mommoo.http.request.rule;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.request.HttpRequest;

public interface HttpRequestRule {
    public HttpRequestRuleResult isValidateRequest(ServerSpec serverSpec, HttpRequest httpRequest);
}
