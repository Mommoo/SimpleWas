package com.mommoo.http.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class HttpRequestBuilder implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestBuilder.class);

    private final Socket requestSocket;
    private final HttpRequestParser headerParser = new HttpRequestParser();

    public HttpRequestBuilder(Socket requestSocket) {
        this.requestSocket = requestSocket;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()), BUFFER_SIZE)){

            String requestLine = bufferedReader.readLine();
            boolean isValidateRequestLine = this.headerParser.setRequestLine(requestLine);
            if (!isValidateRequestLine) {
                return;
            }



        } catch (IOException e) {
            logger.warn("[Request 소켓에 담겨온 데이터를 읽지 못했습니다.]", e);
        }
    }
}
