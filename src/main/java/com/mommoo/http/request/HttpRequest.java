package com.mommoo.http.request;

import com.mommoo.http.HeaderType;
import com.mommoo.http.Method;

import java.util.Map;

public interface HttpRequest {
    public Method getMethod();
    public String getContextPath();
    public String getURI();
    public String getSchema();
    public String getProtocol();
    public String getVersion();
    public Map<HeaderType, String> getHeaders();
    public String getHeader(HeaderType headerType);
    public Map<String, String> getParameterMap();
    public String getParameterValue(String parameterName);
}
