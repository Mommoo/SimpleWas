package com.mommoo.http.request;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class HttpRequestBuilder {
    private final HttpRequestParser headerParser;

    public HttpRequestBuilder(String mainLogPath, Socket socket) throws IOException {
        headerParser = new HttpRequestParser(mainLogPath);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = bufferedReader.readLine();

        this.headerParser.setRequestLine(requestLine);

        String headerLine;

        while (!(headerLine = bufferedReader.readLine()).equals("")) {
            this.headerParser.setHeaderLine(headerLine);
        }

        char[] buffer = new char[2014];
        while (bufferedReader.ready()) {
            int len = bufferedReader.read(buffer);
            this.headerParser.appendBody(new String(buffer, 0, len));
        }
    }

    public HttpRequest build() {
        return this.headerParser.toHttpRequest();
    }
}
