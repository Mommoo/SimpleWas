package com.mommoo.http.request;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class HttpRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParser.class);

    private String mainLogPath;

    private HttpMethod httpMethod;
    private String URI;
    private String schema;
    private String protocol;
    private String version;

    private Map<HttpHeaderType, String> headerMap = new HashMap<>();
    private Map<String, String> queryMap = new HashMap<>();

    private StringBuilder body = new StringBuilder();

    HttpRequestParser(String mainLogPath) {
        this.mainLogPath = mainLogPath;
    }

    void setRequestLine(String requestLine) throws IOException {
        String[] requestLineElement = requestLine.split(" ");

        if (requestLineElement.length != 3) {
            throw new IOException("Http 프로토콜 스펙에 맞지 않는 헤더 라인 입니다.");
        }

        this.httpMethod = HttpMethod.of(requestLineElement[0]);

        if ( this.httpMethod == null ) {
            throw new IOException("Http 프로토콜에 알맞지 않은 메서드 요청을 시도 했습니다.");
        } else if( !requestLineElement[2].toLowerCase().contains("http")) {
            throw new IOException("Http 프로토콜이 아닙니다.");
        }

        parseURIAndQueryString(requestLineElement[1]);

        this.schema = requestLineElement[2];
        String[] schemaElement = this.schema.split("/");

        if (schemaElement.length != 2) {
            throw new IOException("Http 스키마 형식이 아닙니다.");
        }

        this.protocol = schemaElement[0];
        this.version = schemaElement[1];
    }

    void setHeaderLine(String headerLine) {
        int typeEndIndex = headerLine.indexOf(":");

        if (typeEndIndex == -1) {
            changeLogPathToMain();
            logger.info("Http 프로토콜 스펙에 맞지 않는 데이터 구성 입니다. ( ".concat(headerLine).concat(" )"));
            return;
        }

        String headerTypeData = headerLine.substring(0, typeEndIndex).trim();
        HttpHeaderType httpHeaderType = HttpHeaderType.of(headerTypeData);
        if (httpHeaderType == null) {
            changeLogPathToMain();
            logger.info("WAS에 등록되지 않은 헤더 타입입니다.(" + headerTypeData +")");
            return;
        }

        headerMap.put(httpHeaderType, headerLine.substring(typeEndIndex+1).trim());
    }

    void appendBody(String body) {
        this.body.append(body);
    }

    private void changeLogPathToMain() {
        MDC.put("logPath", mainLogPath);
    }

    private void parseURIAndQueryString(String fullURI) {
        int queryStringIndexOf = fullURI.lastIndexOf("?");

        if (queryStringIndexOf != -1) {
            URI = fullURI.substring(0, queryStringIndexOf);
            String queryString = fullURI.substring(queryStringIndexOf + 1);
            parseQueryString(queryString);
            return;
        }

        URI = fullURI;
    }

    private void parseQueryString(String queryString) {
        String[] queries = queryString.split("&");
        for (String query : queries) {
            int keyIndex = query.indexOf("=");
            if (keyIndex == -1) {
                continue;
            }

            String key = query.substring(0, keyIndex);
            String value = query.substring(keyIndex+1);
            changeLogPathToMain();
            logger.info("쿼리 스트링 파싱 [ key : ".concat(key).concat(" , value : ").concat(value).concat(" ]"));
            queryMap.put(key, value);
        }
    }

    private void parseQueryStringAtBody() {
        String contentType = headerMap.get(HttpHeaderType.CONTENT_TYPE);
        if (contentType == null) {
            parseQueryString(body.toString());
        } else if(contentType.contains("/")) {
            String contentDataType = contentType.substring(0, contentType.indexOf("/"));
            if (contentDataType.equals("*") || contentDataType.equals("text")) {
                parseQueryString(body.toString());
            }
        }
    }

    HttpRequest toHttpRequest() {
        parseQueryStringAtBody();

        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpRequestParser.this.httpMethod;
            }

            @Override
            public String getURI() {
                return HttpRequestParser.this.URI;
            }

            @Override
            public Map<HttpHeaderType, String> getHeaders() {
                return new HashMap<>(headerMap);
            }

            @Override
            public String getHeader(HttpHeaderType httpHeaderType) {
                return headerMap.get(httpHeaderType);
            }

            @Override
            public String getSchema() {
                return HttpRequestParser.this.schema;
            }

            @Override
            public String getProtocol() {
                return HttpRequestParser.this.protocol;
            }

            @Override
            public String getVersion() {
                return HttpRequestParser.this.version;
            }

            @Override
            public Map<String, String> getParameters() {
                return new HashMap<>(queryMap);
            }

            @Override
            public String getParameter(String parameterName) {
                return queryMap.get(parameterName);
            }

            @Override
            public String toString() {
                return "HttpMethod : " + getMethod()+"\n"+
                        "URI : " + getURI()+"\n"+
                        "Protocol : " + getProtocol()+"\n"+
                        "Version : " +getVersion()+"\n"+
                        "headers : " + headerMap +"\n"+
                        "queryStrings :" + queryMap+"\n";
            }
        };
    }
}
