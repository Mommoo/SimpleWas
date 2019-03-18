package com.mommoo.http.request;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpMethod;

import java.util.Map;

public interface HttpRequest {
    public HttpMethod getMethod();
    public String getURI();
    public String getSchema();
    public String getProtocol();
    public String getVersion();
    public Map<HttpHeaderType, String> getHeaders();
    public String getHeader(HttpHeaderType httpHeaderType);
    public Map<String, String> getParameters();
    public String getParameter(String parameterName);
}
