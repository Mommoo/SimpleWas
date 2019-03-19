package com.mommoo.http.response;

import com.mommoo.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;

/**
 * 소켓에 출력을 담당하는 클래스입니다.
 * {@link HttpResponseHandler}가 구성하는 Response Text 데이터를 이용하여 소켓에 출력합니다.
 *
 * 출력 기능은 다음과 같이 3가지가 존재합니다.
 *
 *  1. 자체 HTML 출력  {@link #sendBasicHTMLPage(HttpStatus, String)}
 *  2. 파일 출력 {@link #sendFile(HttpStatus, Path, String)}
 *  3. HttpResponse 출력 {@link #send(HttpResponse, String)}
 *
 *  @author mommoo
 */
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
