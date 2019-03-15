package com.mommoo.http.request;

import com.mommoo.http.HeaderType;
import com.mommoo.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.StringTokenizer;

public class HttpRequestParserTest {
    private static final String MOCK_GET_HTTP_REQUEST =
            "Get /serverConfigurationCreateTest/serverConfigurationCreateTest.htm HTTP/1.1\n" +
            "Accept: */*\n" +
            "Accept-Language: ko\n" +
            "Accept-Encoding: gzip, deflate\n" +
            "If-Modified-Since: Fri, 21 Jul 2006 05:31:13 GMT\n" +
            "If-None-Match: \"734237e186acc61:a1b\"\n" +
            "User-Agent: Mozilla/4.0(compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)\n" +
            "Host: localhost\n" +
            "Connection: Keep-Alive\n" +
            "\n";

    @Test()
    public void testGETRequest() {
        String requestLine = MOCK_GET_HTTP_REQUEST.substring(0, MOCK_GET_HTTP_REQUEST.indexOf("\n"));

        HttpRequestParser httpRequestParser = new HttpRequestParser();
        httpRequestParser.setRequestLine(requestLine);

        StringTokenizer tokenizer = new StringTokenizer(MOCK_GET_HTTP_REQUEST, "\n");
        tokenizer.nextToken();

        while (tokenizer.hasMoreTokens()) {
            String nextLine = tokenizer.nextToken().trim();
            httpRequestParser.setHeaderLine(nextLine);
        }

        HttpRequest request = httpRequestParser.toHttpRequest();
        Assertions.assertEquals(request.getMethod(), Method.GET);
        Assertions.assertEquals(request.getProtocol(), "HTTP");
        Assertions.assertEquals(request.getVersion(), "1.1");
        Assertions.assertEquals(request.getURI(), "/serverConfigurationCreateTest/serverConfigurationCreateTest.htm");
        Assertions.assertEquals(request.getHeader(HeaderType.ACCEPT), "*/*");
        Assertions.assertEquals(request.getHeader(HeaderType.ACCEPT_LANGUAGE), "ko");
        Assertions.assertEquals(request.getHeader(HeaderType.ACCEPT_ENCODING), "gzip, deflate");
        Assertions.assertEquals(request.getHeader(HeaderType.IF_MODIFIED_SINCE), "Fri, 21 Jul 2006 05:31:13 GMT");
        Assertions.assertEquals(request.getHeader(HeaderType.IF_NONE_MATCH), "\"734237e186acc61:a1b\"");
        Assertions.assertEquals(request.getHeader(HeaderType.USER_AGENT), "Mozilla/4.0(compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)");
        Assertions.assertEquals(request.getHeader(HeaderType.HOST), "localhost");
        Assertions.assertEquals(request.getHeader(HeaderType.CONNECTION), "Keep-Alive");
    }
}
