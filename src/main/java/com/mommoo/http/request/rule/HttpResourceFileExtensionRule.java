package com.mommoo.http.request.rule;

import com.mommoo.conf.ServerSpec;
import com.mommoo.contents.ServerContentsFinder;
import com.mommoo.http.request.HttpRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class HttpResourceFileExtensionRule implements HttpRequestRule {
    private Set<String> fileExtensionSet = new HashSet<>();

    HttpResourceFileExtensionRule(String... fileExtension) {
        Collections.addAll(fileExtensionSet, fileExtension);
    }

    @Override
    public HttpRequestRuleResult isValidateRequest(ServerSpec serverSpec, HttpRequest httpRequest) {
        String URI = httpRequest.getURI();

        String lastResource = URI.substring(URI.lastIndexOf("/") + 1);

        int indexOfFileExtension = lastResource.lastIndexOf(".");

        if (ServerContentsFinder.isServletRequest(URI)) {
            return new HttpRequestRuleResult(true, "서블릿 요청 URI 입니다.");
        }

        if (indexOfFileExtension == -1) {
            return new HttpRequestRuleResult(true, "파일 확장자가 없는 URI 입니다.");
        }

        String fileExtension = lastResource.substring(indexOfFileExtension+1);
        boolean isValidExtension = !fileExtensionSet.contains(fileExtension);

        String message = (isValidExtension ? "유효한" : "유효하지 않는") + " 확장자 입니다.(" + fileExtension +")";

        return new HttpRequestRuleResult(isValidExtension, message);
    }
}
