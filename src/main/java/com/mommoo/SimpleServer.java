package com.mommoo;

import com.mommoo.conf.ServerSpec;
import com.mommoo.contents.ServerContentsFinder;
import com.mommoo.http.HttpHeaderType;
import com.mommoo.http.HttpStatus;
import com.mommoo.http.request.HttpRequest;
import com.mommoo.http.request.HttpRequestBuilder;
import com.mommoo.http.request.rule.HttpRequestRuleInspector;
import com.mommoo.http.request.rule.HttpRequestRuleResult;
import com.mommoo.http.response.HttpResponse;
import com.mommoo.http.response.HttpResponseHandler;
import com.mommoo.http.response.HttpResponseSender;
import com.mommoo.servlet.SimpleServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SimpleServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);

    private final List<ServerSpec> serverSpecList = new ArrayList<>();
    private final String mainLogPath;
    private final int threadCount;
    private int portNumber = -1;
    private ServerSpec anyMatchServerSpec;

    public SimpleServer(String mainLogPath, int threadCount) {
        this.mainLogPath = mainLogPath;
        this.threadCount = threadCount;
    }

    public void addServerSpecs(List<ServerSpec> serverSpecs) {
        for (ServerSpec serverSpec : serverSpecs) {
            addServerSpec(serverSpec);
        }
    }

    private void addServerSpec(ServerSpec serverSpec) {
        if (portNumber == -1) {
            portNumber = serverSpec.getPortNumber();
        }

        if (portNumber != serverSpec.getPortNumber()) {
            setMainLogPath();
            logger.error("서로 다른 포트 번호를 가진 서버 스펙을 동시에 보유할 수 없습니다." + portNumber + " , " + serverSpec.getPortNumber(), new InvalidParameterException());
            return;
        }

        if (serverSpec.getServerName().equals("*")) {
            anyMatchServerSpec = serverSpec;
        } else {
            serverSpecList.add(serverSpec);
        }
    }

    public void start() throws IOException {
        printLogOfServerInfo();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        ServerSocket serverSocket = new ServerSocket(portNumber);

        while (true) {
            logger.info(this + "의 Connection Listen 시작");

            Socket socket = serverSocket.accept();

            printLogOfClientInfo(socket);

            executorService.submit(() -> {
                HttpRequestBuilder requestBuilder = null;

                try {
                    requestBuilder = new HttpRequestBuilder(mainLogPath, socket);
                } catch (IOException io) {
                    setMainLogPath();
                    logger.error("HttpRequest 파싱에 실패했습니다.", io);
                    HttpResponseSender.sendBasicHTMLPage(HttpStatus.CODE_500, socket, mainLogPath);
                    return;
                }

                HttpRequest httpRequest = requestBuilder.build();

                String host = httpRequest.getHeader(HttpHeaderType.HOST);
                ServerSpec serverSpec = findServerSpecByHostOrNull(host);

                if (serverSpec == null) {
                    printLogOfInvalidateHostAccess(host);
                    HttpResponseSender.sendBasicHTMLPage(HttpStatus.CODE_412, socket, mainLogPath);
                    return;
                }

                HttpRequestRuleResult ruleResult
                        = HttpRequestRuleInspector
                        .getInstance()
                        .isValidateRequest(serverSpec, httpRequest);


                printLogOfHttpRequest(serverSpec, httpRequest, ruleResult);

                HttpStatus httpStatus = ruleResult.isValidate ? HttpStatus.CODE_200 : HttpStatus.CODE_403;

                ServerContentsFinder contentsFinder = new ServerContentsFinder(serverSpec, httpRequest.getURI(), httpStatus);

                String serverSpecLogPath = serverSpec.getLogPath();

                switch (contentsFinder.getServerContents()) {
                    case NONE:
                        //403 -> 올때가 있꼬
                        //200 -> 올때가 있네;
                        HttpStatus targetStatus = httpStatus == HttpStatus.CODE_200 ? HttpStatus.CODE_404 : httpStatus;
                        HttpResponseSender.sendBasicHTMLPage(targetStatus, socket, serverSpecLogPath);
                        System.out.println("NONE!!");
                        break;
                    case SERVLET:
                        HttpResponse httpResponse = new HttpResponseHandler();
                        SimpleServlet simpleServlet = contentsFinder.getServlet(httpRequest.getURI());
                        simpleServlet.service(httpRequest, httpResponse);
                        HttpResponseSender.send(httpResponse, socket, serverSpecLogPath);
                        break;
                    case FILE:
                        Path filePath = contentsFinder.getFilePath();
                        HttpResponseSender.sendFile(httpStatus, filePath, socket, serverSpecLogPath);
                        break;
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    private ServerSpec findServerSpecByHostOrNull(String host) {
        boolean isPortNumberContain = host.contains(":");
        for (ServerSpec serverSpec : serverSpecList) {
            String serverSpecHost;
            if (isPortNumberContain) {
                serverSpecHost = serverSpec.getServerName() + ":" + serverSpec.getPortNumber();
            } else {
                serverSpecHost = serverSpec.getServerName();
            }
            if (serverSpecHost.equals(host)) {
                return serverSpec;
            }
        }

        return anyMatchServerSpec;
    }

    private static void setServerSpecLogPath(ServerSpec serverSpec) {
        MDC.put("logPath", serverSpec.getLogPath());
    }

    private void setMainLogPath() {
        MDC.put("logPath", mainLogPath);
    }

    private void printLogOfServerInfo() {
        setMainLogPath();
        logger.info(this.toString().concat("를 구동했습니다.").concat("\n")
                .concat("## 총 스레드 개수 : ").concat(Integer.toString(threadCount)).concat("\n")
                .concat("## 개방 포트 : ").concat(Integer.toString(portNumber)).concat("\n")
                .concat("## Server Spec 개수 : ").concat(Integer.toString(serverSpecList.size())).concat("\n"));
    }

    private void printLogOfClientInfo(Socket socket) {
        setMainLogPath();
        String clientIP = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString();
        logger.info(this.toString().concat("의 Connection Binding").concat("\n")
                .concat("## Client IP : ").concat(clientIP).concat("\n"));
    }

    private static void printLogOfHttpRequest(ServerSpec serverSpec, HttpRequest httpRequest, HttpRequestRuleResult ruleResult) {
        setServerSpecLogPath(serverSpec);
        logger.info("Accept HttpRequest\n"
                .concat(httpRequest.toString()).concat("\n")
                .concat(ruleResult.toString()).concat("\n")
        );
    }

    private void printLogOfInvalidateHostAccess(String host) {
        setMainLogPath();
        String hostListInfo = serverSpecList.stream()
                .map(ServerSpec::getServerName)
                .collect(Collectors.toList())
                .toString();

        String onlyHostName = host.contains(":") ? host.substring(0, host.indexOf(":")) : host;

        logger.warn(
                "비정상적인 HOST 접근 입니다.\n"
                        .concat("## 접근 된 HOST : ").concat(onlyHostName).concat("\n")
                        .concat("## 등록 된 HOST 리스트 : ").concat(hostListInfo)
        );
    }

    @Override
    public String toString() {
        return String.format("SimpleServer[%-6d]", portNumber);
    }
}
