package com.mommoo.http.request;

import com.mommoo.http.HeaderType;
import com.mommoo.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class HttpRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParser.class);

    private Method method;
    private String URI;
    private String schema;
    private String protocol;
    private String version;

    private Map<HeaderType, String> headerMap = new HashMap<>();
    private Map<String, String> queryStrings = new HashMap<>();

    boolean setRequestLine(String requestLine) {
        String[] requestLineElement = requestLine.split(" ");

        if (requestLineElement.length != 3) {
            logger.info("Http 프로토콜 스펙에 맞지 않는 헤더 라인 입니다.");
            return false;
        }

        this.method = Method.of(requestLineElement[0]);

        if ( this.method == null ) {
            logger.info("Http 프로토콜에 알맞지 않은 메서드 요청을 시도 했습니다.");
            return false;
        } else if( !requestLineElement[2].toLowerCase().contains("http")) {
            logger.info("Http 프로토콜이 아닙니다.");
            return false;
        }

        this.URI = requestLineElement[1];
        this.schema = requestLineElement[2];
        String[] schemaElement = this.schema.split("/");

        if (schemaElement.length != 2) {
            logger.info("Http 스키마 형식이 아닙니다.");
            return false;
        }

        this.protocol = schemaElement[0];
        this.version = schemaElement[1];

        return true;
    }

    void setHeaderLine(String headerLine) {
        int typeEndIndex = headerLine.indexOf(":");

        if (typeEndIndex == -1) {
            logger.info("Http 프로토콜 스펙에 맞지 않는 데이터 구성 입니다.");
            return;
        }

        String headerTypeData = headerLine.substring(0, typeEndIndex).trim();
        HeaderType headerType = HeaderType.of(headerTypeData);
        if (headerType == null) {
            logger.info("WAS에 등록되지 않은 헤더 타입입니다.(" + headerTypeData +")");
            return;
        }

        headerMap.put(headerType, headerLine.substring(typeEndIndex+1).trim());
    }

    HttpRequest toHttpRequest() {

        return new HttpRequest() {
            @Override
            public Method getMethod() {
                return HttpRequestParser.this.method;
            }

            @Override
            public String getContextPath() {
                return null;
            }

            @Override
            public String getURI() {
                return HttpRequestParser.this.URI;
            }

            @Override
            public Map<HeaderType, String> getHeaders() {
                return new HashMap<>(headerMap);
            }

            @Override
            public String getHeader(HeaderType headerType) {
                return headerMap.get(headerType);
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
            public Map<String, String> getParameterMap() {
                return new HashMap<>(queryStrings);
            }

            @Override
            public String getParameterValue(String parameterName) {
                return queryStrings.get(parameterName);
            }

            @Override
            public String toString() {
                return "Method : " + getMethod()+"\n"+
                        "URI : " + getURI()+"\n"+
                        "Protocol : " + getProtocol()+"\n"+
                        "Version : " +getVersion()+"\n"+
                        "headers : " + headerMap +"\n";
            }
        };
    }
}
