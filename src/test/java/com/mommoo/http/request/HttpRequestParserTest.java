package com.mommoo.http.request;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.StringTokenizer;

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

    private static void assertMockHttpRequest(HttpRequest request, String method, String URI) {
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
        Assertions.assertEquals(request.getParameter("name"), "mommoo");
        Assertions.assertEquals(request.getParameter("age"), "29");
    }

    @Test()
    @DisplayName("GET Http 요청 파싱 검증 테스트 ( URI에 쿼리스트링 삽입 )")
    public void testGETRequest() {
        String URI = "/mommoo/nhn/good/test.html";

        String method = "GET";
        String fullURI = URI.concat("?").concat(queryString);
        String body = "";

        HttpRequest request = createMockHttpRequest(String.format(MOCK_HTTP_REQUEST_PATTERN, method, fullURI, body));
        assertMockHttpRequest(request, method, URI);
    }

    @Test()
    @DisplayName("POST Http 요청 파싱 검증 테스트 ( Body에 쿼리스트링 삽입 )")
    public void testPostRequest() {
        String URI = "/next/next/next/test.html";

        String method = "POST";
        String body = "\n".concat(queryString).concat("\n");

        HttpRequest request = createMockHttpRequest(String.format(MOCK_HTTP_REQUEST_PATTERN, method, URI, body));

        assertMockHttpRequest(request, method, URI);
    }
}
