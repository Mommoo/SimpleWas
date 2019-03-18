package com.mommoo.http.request.rule;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.request.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class HttpRequestRuleInspector implements HttpRequestRule{
    private static final String[] INVALID_FILE_EXTENSIONS = {"exe"};

    private static final HttpRequestRuleInspector INSTANCE = new HttpRequestRuleInspector();

    private List<HttpRequestRule> requestRuleList = new ArrayList<>();

    private HttpRequestRuleInspector() {
        requestRuleList.add(new HttpResourceFileExtensionRule(INVALID_FILE_EXTENSIONS));
        requestRuleList.add(new HttpResourceSearchRule());
    }

    public static HttpRequestRuleInspector getInstance() {
        return INSTANCE;
    }

    @Override
    public HttpRequestRuleResult isValidateRequest(ServerSpec serverSpec, HttpRequest httpRequest) {
        StringBuilder builder = new StringBuilder();
        boolean isValidate = true;

        for (HttpRequestRule httpRequestRule : requestRuleList) {
            HttpRequestRuleResult result = httpRequestRule.isValidateRequest(serverSpec, httpRequest);
            String className = httpRequestRule.getClass().getSimpleName();
            builder.append(className).append(" : ").append(result).append("\n");
            if (!result.isValidate) {
                isValidate = false;
                break;
            }
        }
        builder.insert(0, "## HttpRequestRule 검증 결과 (".concat(Boolean.toString(isValidate)).concat(")\n"));

        return new HttpRequestRuleResult(isValidate, builder.toString());
    }
}
