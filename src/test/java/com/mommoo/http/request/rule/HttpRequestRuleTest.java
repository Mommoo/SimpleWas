package com.mommoo.http.request.rule;

import com.mommoo.conf.ServerSpec;
import com.mommoo.conf.ServerSpecBuilder;
import com.mommoo.http.request.HttpRequest;
import com.mommoo.http.request.HttpRequestParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

/**
 * 해당 테스트 클래스는, {@link HttpRequestParserTest#createMockHttpRequest(String)} 메서드를 사용하므로,
 * {@link HttpRequestParserTest} 의 검증 테스트 먼저 통과해야 합니다.
 *
 * HttpRequest 검증 Rule은 두가지가 존재합니다.
 *  1. URI의 확장자 검사 Rule. {@link HttpResourceFileExtensionRule}
 *  2. URI의 리소스 요청 경로 검사 Rule. {@link HttpResourceSearchRule}
 *
 * 각 Rule 인스턴스가 올바르게 동작하는지 검증합니다.
 * {@link #httpResourceFileExtensionRuleTest()}
 * {@link #httpResourceSearchRuleTest()}
 *
 * 각 테스트 메서드는 공통 코드 하나로 합칠 수 있지만, 가독성을 위해 어느정도 풀어 작성했습니다.
 */
public class HttpRequestRuleTest {
    private static final String MOCK_HTTP_REQUEST_PATTEN =
            "Get %s HTTP/1.1\n" +
                    "Accept: */*\n" +
                    "Accept-Language: ko\n" +
                    "Accept-Encoding: gzip, deflate\n" +
                    "If-Modified-Since: Fri, 21 Jul 2006 05:31:13 GMT\n" +
                    "If-None-Match: \"734237e186acc61:a1b\"\n" +
                    "User-Agent: Mozilla/4.0(compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)\n" +
                    "Host: localhost\n" +
                    "Connection: Keep-Alive\n" +
                    "\n";

    @Test
    @DisplayName("HttpResourceFileExtensionRule 로직 검증 테스트")
    public void httpResourceFileExtensionRuleTest() {
        ServerSpec mockServerSpec = createMockServerSpec();

        // 걸러야할 확장자를 선언합니다.
        String[] fileExtensions = {"exe", "htm", "jar", "java"};

        // 확장자 검사 인스턴스를 생성합니다.
        HttpResourceFileExtensionRule
                httpResourceFileExtensionRule = new HttpResourceFileExtensionRule(fileExtensions);

        // 유효하지 않은 URI 케이스를 만듭니다.
        String[] invalidateURIs = {
                "/mommoo/folder/game.exe",
                "/mommoo/folder/next/myJava.jar",
                "/mommoo/folder/var/www/index.htm",
                "/mommoo/src/main/java/Main.java"
        };

        // 유효하지 않은 URI 케이스 이므로, 모두 유효하지 않는 결과 값이 나와야 합니다.
        httpRequestForEachByURICase(invalidateURIs, httpRequest -> {
            boolean isInValidate = !httpResourceFileExtensionRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isInValidate);
        });

        // 유효한 URI 케이스를 만듭니다.
        String[] validateURIs = {
                "/mommoo/folder/service.Hello",
                "/mommoo/folder/next/myJava",
                "/mommoo/folder/var/www/index.html",
                "/mommoo/src/main/java/main.ppt"
        };

        // 유효한 URI 케이스 이므로, 모두 유효한 결과 값이 나와야 합니다.
        httpRequestForEachByURICase(validateURIs, httpRequest -> {
            boolean isValidate = httpResourceFileExtensionRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isValidate);
        });
    }

    @Test
    @DisplayName("HttpResourceSearchRule 로직 검증 테스트")
    public void httpResourceSearchRuleTest() {
        ServerSpec mockServerSpec = createMockServerSpec();

        HttpResourceSearchRule httpResourceSearchRule = new HttpResourceSearchRule();

        // 유효하지 않은 URI 케이스를 만듭니다.
        String[] invalidateURIs = {
                "/../../game.exe",
                "/../../../../etc/passwd",
                "/../../company/nhn"
        };

        // 유효하지 않은 URI 케이스 이므로, 모두 유효하지 않는 결과 값이 나와야 합니다.
        httpRequestForEachByURICase(invalidateURIs, httpRequest -> {
            boolean isInvalidate = !httpResourceSearchRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isInvalidate);
        });

        // 유효한 URI 케이스를 만듭니다.
        String[] validateURIs = {
                "/../../var/www/",
                "/home/index.html",
                "/home/../hello.html"
        };

        // 유효한 URI 케이스 이므로, 모두 유효한 결과 값이 나와야 합니다.
        httpRequestForEachByURICase(validateURIs, httpRequest -> {
            boolean isValidate = httpResourceSearchRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isValidate);
        });
    }

    private static String createMockHttpRequestByURI(String URI) {
        return String.format(MOCK_HTTP_REQUEST_PATTEN, URI);
    }

    private static ServerSpec createMockServerSpec() {
        return new ServerSpecBuilder().setDocumentPath("/var/www")
                .setLogPath("log")
                .setIndexPage("index.html")
                .setPortNumber(6766)
                .setServerName("mommoo.com")
                .build();
    }

    private static void httpRequestForEachByURICase(String[] URIs, Consumer<HttpRequest> httpRequestConsumer) {
        for (String invalidateURI : URIs) {
            String httpRequestStringByURI = createMockHttpRequestByURI(invalidateURI);
            HttpRequest mockHttpRequest = HttpRequestParserTest.createMockHttpRequest(httpRequestStringByURI);
            httpRequestConsumer.accept(mockHttpRequest);
        }
    }
}
