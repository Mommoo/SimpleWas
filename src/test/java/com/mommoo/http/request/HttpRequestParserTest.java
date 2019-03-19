package com.mommoo.http.request;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.StringTokenizer;

/**
 * 해당 테스트 클래스는 Http Request 요청이 들어왔을 경우에, 데이터를 올바르게 파싱 하는지를 테스트 합니다. {@link HttpRequestParser}
 * {@link HttpRequestParser} 는 {@link HttpRequest} 인스턴스를 생성합니다.
 * Http Request 테스트 데이터는 {@link #MOCK_HTTP_REQUEST_PATTERN} 이며, 다음과 같은 데이터를 패턴으로 채웁니다.
 *
 *  1. HttpMethod
 *  2. URI
 *  3. HttpRequestBody
 *
 * 테스트는 2가지를 진행했습니다. {@link HttpRequestParser}를 통한 {@link HttpRequest} 생성 후 값 비교를 진행합니다.
 * 다음의 테스트 2개 모두 쿼리 스트링 포함 여부도 같이 테스트 합니다. {@link #queryString}
 *
 *  1. GET  요청시, 올바른 {@link HttpRequest}를 생성하는지 여부 테스트.
 *  2. POST 요청시, 올바른 {@link HttpRequest}를 생성하는지 여부 테스트.
 *
 */
public class HttpRequestParserTest {
    private static final String MOCK_HTTP_REQUEST_PATTERN =
            "%s %s HTTP/1.1\n" +
            "Accept: */*\n" +
            "Accept-Language: ko\n" +
            "Accept-Encoding: gzip, deflate\n" +
            "If-Modified-Since: Fri, 21 Jul 2006 05:31:13 GMT\n" +
            "If-None-Match: \"734237e186acc61:a1b\"\n" +
            "User-Agent: Mozilla/4.0(compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)\n" +
            "Host: localhost\n" +
            "Connection: Keep-Alive\n" +
            "%s";

    private static final String queryString = "name=mommoo&age=29";

    @Test()
    @DisplayName("GET Http 요청 파싱 검증 테스트")
    public void testGETRequest() {
        String method = "GET";
        String URI = "/mommoo/nhn/good/test.html";
        String body = "";

        // GET 요청이므로, body는 빈 값을 넣습니다.
        HttpRequest request = createMockHttpRequest(String.format(MOCK_HTTP_REQUEST_PATTERN, method, URI, body));

        // 하드코딩한 데이터 값을 비교합니다.
        assertMockHttpRequest(request, method, URI, false);

        // 쿼리스트링이 포함된 GET 요청을 테스트 합니다.
        String fullURI = URI.concat("?").concat(queryString);
        request = createMockHttpRequest(String.format(MOCK_HTTP_REQUEST_PATTERN, method, fullURI, body));
        assertMockHttpRequest(request, method, URI, true);
    }

    @Test()
    @DisplayName("POST Http 요청 파싱 검증 테스트")
    public void testPostRequest() {
        String URI = "/next/next/next/test.html";
        String method = "POST";
        String body = "\n".concat(queryString).concat("\n");

        // body를 비우면 GET 요청 테스트와 흡사 하므로, body에 쿼리스트링을 채우고 테스트를 진행합니다.
        HttpRequest request = createMockHttpRequest(String.format(MOCK_HTTP_REQUEST_PATTERN, method, URI, body));
        assertMockHttpRequest(request, method, URI, true);
    }

    /** 데이터를 한줄 씩 읽어, Parser에게 전달 합니다. */
    public static HttpRequest createMockHttpRequest(String mockHttpRequestString) {
        String requestLine = mockHttpRequestString.substring(0, mockHttpRequestString.indexOf("\n"));

        HttpRequestParser httpRequestParser = new HttpRequestParser("log");
        try {
            httpRequestParser.setRequestLine(requestLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] restLines = mockHttpRequestString.split("\n");
        // requestLine을 제외한 나머지 부분 부터
        int i = 1;
        for (; i < restLines.length ; i++) {
            String line = restLines[i];
            if (line.equals("")) {
                i++;
                break;
            }
            httpRequestParser.setHeaderLine(line);
        }

        for (; i < restLines.length; i++) {
            String line = restLines[i];
            httpRequestParser.appendBody(line);
        }

        return httpRequestParser.toHttpRequest();
    }

    /** 만들어진 Request 인스턴스를 하드코딩으로 데이터 확인을 진행합니다. */
    private static void assertMockHttpRequest(HttpRequest request, String method, String URI, boolean isQueryString) {
        Assertions.assertEquals(request.getMethod(), HttpMethod.of(method));
        Assertions.assertEquals(request.getProtocol(), "HTTP");
        Assertions.assertEquals(request.getVersion(), "1.1");
        Assertions.assertEquals(request.getURI(), URI);
        Assertions.assertEquals(request.getHeader(HttpHeaderType.ACCEPT), "*/*");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.ACCEPT_LANGUAGE), "ko");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.ACCEPT_ENCODING), "gzip, deflate");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.IF_MODIFIED_SINCE), "Fri, 21 Jul 2006 05:31:13 GMT");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.IF_NONE_MATCH), "\"734237e186acc61:a1b\"");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.USER_AGENT), "Mozilla/4.0(compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.HOST), "localhost");
        Assertions.assertEquals(request.getHeader(HttpHeaderType.CONNECTION), "Keep-Alive");

        if (isQueryString) {
            Assertions.assertEquals(request.getParameter("name"), "mommoo");
            Assertions.assertEquals(request.getParameter("age"), "29");
        }
    }
}
