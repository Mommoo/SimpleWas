package com.mommoo.http.response;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpStatus;

import java.io.Writer;

public interface HttpResponse {
    public void setHeaderData(HttpHeaderType httpHeaderType, String data);
    public void setSchema(String schema);
    public void setStatus(HttpStatus status);
    public HttpStatus getStatus();
    public Writer getWriter();
}
