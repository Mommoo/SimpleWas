package com.mommoo.http.request.rule;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.request.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 검증 해야 할, 모든 HttpRequest 검증 규칙을 한곳에 모은 클래스 입니다.
 * 필터 방식으로, 모든 규칙을 통과해야 최종 검증을 완료 합니다.
 *
 * @author mommoo
 */
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
