package com.mommoo.http.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class HttpRequestBuilder implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestBuilder.class);

    private boolean isBuildComplete = false;
    private final Socket requestSocket;
    private final HttpRequestParser headerParser = new HttpRequestParser();
    private Consumer<HttpRequest> onBuildCompleteListener = httpRequest -> {};

    public HttpRequestBuilder(Socket requestSocket) {
        this.requestSocket = requestSocket;
    }

    public void setOnBuildCompleteListener(Consumer<HttpRequest> onBuildCompleteListener) {
        this.onBuildCompleteListener = onBuildCompleteListener;
        if (this.isBuildComplete) {
            this.onBuildCompleteListener.accept(this.headerParser.toHttpRequest());
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()), BUFFER_SIZE);
            String requestLine = bufferedReader.readLine();
            boolean isValidateRequestLine = this.headerParser.setRequestLine(requestLine);
            if (!isValidateRequestLine) {
                return ;
            }

            String headerLine;

            while (!(headerLine = bufferedReader.readLine()).equals("")) {
                this.headerParser.setHeaderLine(headerLine);
            }

        } catch (IOException e) {
            logger.warn("[Request 소켓에 담겨온 데이터를 읽지 못했습니다.]", e);
        }

        this.isBuildComplete = true;
        this.onBuildCompleteListener.accept(headerParser.toHttpRequest());
    }
}
