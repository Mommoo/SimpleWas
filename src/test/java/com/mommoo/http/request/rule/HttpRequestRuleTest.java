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
 * 본 테스트는,
 * {@link HttpRequestParserTest#createMockHttpRequest(String)} 메서드를 사용하므로,
 * {@link HttpRequestParserTest} 의 검증 테스트 먼저 통과해야 한다.
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

    @Test
    @DisplayName("HttpResourceFileExtensionRule 로직 검증 테스트")
    public void httpResourceFileExtensionRuleTest() {
        ServerSpec mockServerSpec = createMockServerSpec();

        String[] fileExtensions = {"exe", "htm", "jar", "java"};

        HttpResourceFileExtensionRule
                httpResourceFileExtensionRule = new HttpResourceFileExtensionRule(fileExtensions);

        String[] invalidateURIs = {
                "/mommoo/folder/game.exe",
                "/mommoo/folder/next/myJava.jar",
                "/mommoo/folder/var/www/index.htm",
                "/mommoo/src/main/java/Main.java"
        };

        httpRequestForEachByURICase(invalidateURIs, httpRequest -> {
            boolean isInValidate = !httpResourceFileExtensionRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isInValidate);
        });

        String[] validateURIs = {
                "/mommoo/folder/service.Hello",
                "/mommoo/folder/next/myJava",
                "/mommoo/folder/var/www/index.html",
                "/mommoo/src/main/java/main.ppt"
        };

        httpRequestForEachByURICase(validateURIs, httpRequest -> {
            boolean isValidate = httpResourceFileExtensionRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isValidate);
        });
    }

    @Test
    @DisplayName("HttpResourceSearchRule 로직 검증 테스트")
    public void httpResourceSearchRuleTest() {
        ServerSpec mockServerSpec = createMockServerSpec();

        String[] invalidateURIs = {
                "/../../game.exe",
                "/../../../../etc/passwd",
                "/../../company/nhn"
        };

        String[] validateURIs = {
                "/../../var/www/",
                "/home/index.html",
                "/home/../hello.html"
        };

        HttpResourceSearchRule httpResourceSearchRule = new HttpResourceSearchRule();

        httpRequestForEachByURICase(invalidateURIs, httpRequest -> {
            boolean isInvalidate = !httpResourceSearchRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isInvalidate);
        });

        httpRequestForEachByURICase(validateURIs, httpRequest -> {
            boolean isValidate = httpResourceSearchRule.isValidateRequest(mockServerSpec, httpRequest).isValidate;
            Assertions.assertTrue(isValidate);
        });
    }
}
