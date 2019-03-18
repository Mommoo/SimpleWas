package com.mommoo.servlet;

import com.mommoo.http.request.HttpRequest;
import com.mommoo.http.response.HttpResponse;

public interface SimpleServlet {
    public void service(HttpRequest httpRequest, HttpResponse httpResponse);
}
