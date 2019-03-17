package com.mommoo;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.HeaderType;
import com.mommoo.http.request.HttpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SimpleServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);

    private final List<ServerSpec> serverSpecList = new ArrayList<>();
    private final String mainLogPath;
    private final int threadCount;
    private int portNumber = -1;

    public SimpleServer(String mainLogPath, int threadCount) {
        this.mainLogPath = mainLogPath;
        this.threadCount = threadCount;
    }

    public void addServerSpec(ServerSpec serverSpec) {
        if (portNumber == -1) {
            portNumber = serverSpec.getPortNumber();
        }

        if (portNumber != serverSpec.getPortNumber()) {
            MDC.put("logPath", mainLogPath);
            logger.error("서로 다른 포트 번호를 가진 서버 스펙을 동시에 보유할 수 없습니다." + portNumber + " , " + serverSpec.getPortNumber(), new InvalidParameterException());
            return;
        }

        serverSpecList.add(serverSpec);
    }

    public void start() {
        MDC.put("logPath", mainLogPath);
        logger.info(new StringBuilder()
                .append(this).append("를 구동했습니다.").append("\n")
                .append("## 총 스레드 개수 : ").append(threadCount).append("\n")
                .append("## 개방 포트 : ").append(portNumber).append("\n")
                .append("## Server Spec 개수 : ").append(serverSpecList.size()).append("\n")
                .toString());

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                MDC.put("logPath", mainLogPath);
                logger.info(this+"의 Connection Listen 시작");
                Socket socket = serverSocket.accept();

                logger.info(new StringBuilder()
                        .append(this).append("의 Connection Bind")
                        .append("## Client IP : ").append(((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress()).append("\n")
                        .toString());

                HttpRequestBuilder requestBuilder = new HttpRequestBuilder(socket);
                requestBuilder.setOnBuildCompleteListener(httpRequest -> {
                    httpRequest.getHeader(HeaderType.HOST);
                });

                executorService.submit(requestBuilder);

            }
        } catch (IOException io) {
            MDC.put("logPath", mainLogPath);
            logger.error("소켓을 열지 못하여 "+this+"가 종료되었습니다.", io);
        }
    }

    private ServerSpec findServerSpecByHost(String host) {
        //TODO host에서 서버 이름만 파싱해야함.
        for (ServerSpec serverSpec : serverSpecList) {
            if (serverSpec.getServerName().equals(host)) {
                return serverSpec;
            }
        }

        // TODO null의 의미가 무엇일지 생각해보기.
        return null;
    }

    @Override
    public String toString() {
        return String.format("SimpleServer[%-6d]", portNumber);
    }
}
