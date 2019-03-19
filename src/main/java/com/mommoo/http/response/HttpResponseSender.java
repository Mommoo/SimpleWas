package com.mommoo.http.response;

import com.mommoo.http.HttpStatus;
import com.mommoo.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;

public class HttpResponseSender {
    private static Logger logger = LoggerFactory.getLogger(HttpResponseSender.class);
    private final static String SEND_ERROR_MSG = "HttpResponse를 클라이언트에게 보내지 못했습니다.";

    private final Socket socket;

    public HttpResponseSender(Socket socket) {
        this.socket = socket;
    }

    public void sendBasicHTMLPage(HttpStatus httpStatus, String logPath) {
        HttpResponseHandler httpResponseHandler = new HttpResponseHandler();
        httpResponseHandler.setStatus(httpStatus);
        try {
            httpResponseHandler.writeBasicHTMLPage();
            send(httpResponseHandler, logPath);
        } catch (IOException e) {
            printErrorLog(logPath, e);
        }
    }

    public void sendFile(HttpStatus httpStatus, Path filePath, String logPath) {
        HttpResponseHandler httpResponseHandler = new HttpResponseHandler();
        httpResponseHandler.setStatus(httpStatus);
        try {
            httpResponseHandler.writeFile(filePath);
            send(httpResponseHandler, logPath);
        } catch (IOException io) {
            printErrorLog(logPath, io);
        }
    }

    public void send(HttpResponse httpResponse, String logPath) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(httpResponse.toString().getBytes());
            outputStream.flush();
        } catch (IOException e) {
            printErrorLog(logPath, e);
        }
    }

    private static void printErrorLog(String logPath, Throwable throwable) {
        MDC.put("logPath", logPath);
        logger.error(SEND_ERROR_MSG, throwable);
    }
}
