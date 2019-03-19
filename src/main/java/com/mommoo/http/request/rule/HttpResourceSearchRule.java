package com.mommoo.http.request.rule;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.request.HttpRequest;

import java.util.Collections;
import java.util.Stack;

/**
 * 유효하지 않은 파일 경로 검색을 검증 하는 클래스 입니다.
 * 스택 자료구조를 이용하여, 사용자가 요구하는 최종경로가 올바른지 검사 했습니다.
 *
 * @author mommoo
 */
class HttpResourceSearchRule implements HttpRequestRule {

    @Override
    public HttpRequestRuleResult isValidateRequest(ServerSpec serverSpec, HttpRequest httpRequest) {
        String documentPath = serverSpec.getDocumentPath();

        String[] documentDirectories = documentPath.split("/");
        Stack<String> directoryStack = new Stack<>();
        Collections.addAll(directoryStack, documentDirectories);

        String[] directories = httpRequest
                .getURI()
                .substring(1)
                .split("/");

        for (String directory : directories) {
            if (directoryStack.empty()) {
                return new HttpRequestRuleResult(false, "루트 디렉토리의 하위 디렉토리를 참조하려 시도했습니다.");
            }

            if (directory.equals("..")) {
                directoryStack.pop();
            } else {
                directoryStack.add(directory);
            }
        }

        String resultPath = String.join("/", directoryStack);

        boolean isValidPath = resultPath.startsWith(documentPath);
        String message = isValidPath ? "공개한 리소스 경로 입니다." : "공개하지 않은 리소스 경로 입니다.";

        return new HttpRequestRuleResult(isValidPath, message);
    }
}
