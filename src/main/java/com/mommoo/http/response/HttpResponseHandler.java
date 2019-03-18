package com.mommoo.http.response;

import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpStatus;
import com.mommoo.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseHandler implements HttpResponse {
    private String schema = "HTTP/1.1";
    private HttpStatus status = HttpStatus.CODE_200;
    private final Map<HttpHeaderType, String> headerDataMap = new HashMap<>();
    private final StringBuilder bodyBuilder = new StringBuilder();
    private final Writer writer;

    public HttpResponseHandler() {
        this.writer = new Writer() {
            private boolean isClosed = false;

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                if (isClosed) {
                    throw new IOException("The writer is already closed");
                }

                bodyBuilder.append(cbuf, off, len);
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() {
                this.isClosed = true;
            }
        };

        fillBasicHeaderData();
    }

    private void fillBasicHeaderData() {
        setHeaderData(HttpHeaderType.SERVER, "MommooSimpleWas");
        setHeaderData(HttpHeaderType.CONTENT_TYPE, "text/html");
    }

    @Override
    public void setHeaderData(HttpHeaderType httpHeaderType, String data) {
        headerDataMap.put(httpHeaderType, data);
    }

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    @Override
    public Writer getWriter() {
        return writer;
    }

    @Override
    public HttpStatus getStatus() {
        return null;
    }

    @Override
    public String toString() {
        setHeaderData(HttpHeaderType.CONTENT_LENGTH, Integer.toString(bodyBuilder.toString().getBytes().length));

        StringBuilder builder = new StringBuilder();
        String headerLine = schema + " " + status;
        builder.append(headerLine).append("\n");

        for (HttpHeaderType headerType : headerDataMap.keySet()) {
            builder.append(headerType.getText()).append(": ").append(headerDataMap.get(headerType)).append("\n");
        }

        builder.append("\n")
                .append(bodyBuilder.toString());

        return builder.toString();
    }

    public void writeBasicHTMLPage() throws IOException {
        getWriter()
                .append("<html>")
                .append("<head><title>").append(this.status.toString()).append("</title></head>")
                .append("<body>")
                .append("<center>")
                .append("<h1>").append(this.status.toString()).append("</h1>")
                .append("</center>")
                .append("<hr>")
                .append("<center>MommooSimpleWas-1.0</center>");
    }

    public void writeFile(Path path) throws IOException {
        FileUtils.copyFileToWriter(path, getWriter());
    }
}
