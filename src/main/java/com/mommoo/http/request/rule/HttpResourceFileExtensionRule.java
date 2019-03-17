package com.mommoo.http.request.rule;

import com.mommoo.http.request.HttpRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HttpResourceFileExtensionRule implements HttpRequestRule{
    private Set<String> fileExtensionSet = new HashSet<>();

    public HttpResourceFileExtensionRule(String... fileExtension) {
        Collections.addAll(fileExtensionSet, fileExtension);
    }

    @Override
    public HttpRequestRuleResult isValidateRequest(HttpRequest httpRequest) {
        String URI = httpRequest.getURI();
        String[] directories = URI.split("/");
        String lastDirectory = directories[directories.length -1];

        int indexOfFileExtension = lastDirectory.lastIndexOf(".");

        if (indexOfFileExtension == -1) {
            return new HttpRequestRuleResult(true, "파일 확장자가 없는 URI 입니다.");
        }

        String fileExtension = lastDirectory.substring(indexOfFileExtension+1);
        boolean isValidExtension = !fileExtensionSet.contains(fileExtension);

        String message = (isValidExtension ? "유효한" : "유효하지 않은") + "확장자 입니다.(" + fileExtension +")";

        return new HttpRequestRuleResult(isValidExtension, message);
    }
}
