package com.mommoo.http.response;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 *  해당 테스트 클래스는 {@link HttpResponseHandler} 가 올바르게 HttpResponse 데이터를 구성하는지 테스트 합니다.
 *  {@link HttpResponseHandler}가 구성하는 순서대로, 하드 코딩한 데이터 값을 비교했습니다.
 */
public class HttpResponseHandlerTest {
    private static String SCHEMA = "HTTP/1.1";
    private static HttpStatus STATUS = HttpStatus.CODE_200;
    private static String SERVER = "MommooSimpleWas";
    private static String CONTENT_TYPE = "text/html";
    private static String BODY_DATA = "<html><title>hello</title><body>Self</body></html>";

    private static String MOCK_HTTP_RESPONSE_PATTERN = "%s %s\n" +
            "Server: %s\n" +
            "Content-Length: %d\n" +
            "Content-Type: %s\n" +
            "\n" +
            "%s";

    private static String MOCK_HTTP_RESPONSE = String.format(MOCK_HTTP_RESPONSE_PATTERN, SCHEMA, STATUS, SERVER, BODY_DATA.getBytes().length, CONTENT_TYPE, BODY_DATA);

    @Test
    @DisplayName("HttpResponse 인스턴스 생성 테스트 및 값 검증 테스트")
    public void testHttpResponseHandler() throws IOException {
        HttpResponseHandler httpResponseHandler = new HttpResponseHandler();
        httpResponseHandler.setSchema(SCHEMA);
        httpResponseHandler.setStatus(STATUS);
        httpResponseHandler.setHeaderData(HttpHeaderType.SERVER, SERVER);
        httpResponseHandler.setHeaderData(HttpHeaderType.CONTENT_TYPE, CONTENT_TYPE);
        httpResponseHandler.setHeaderData(HttpHeaderType.CONTENT_LENGTH, Integer.toString(BODY_DATA.getBytes().length));
        httpResponseHandler.getWriter().append(BODY_DATA);

        boolean isValidHttpResponse = MOCK_HTTP_RESPONSE.equals(httpResponseHandler.toString());
        Assertions.assertTrue(isValidHttpResponse);
    }
}
