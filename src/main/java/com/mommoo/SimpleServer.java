package com.mommoo;

import com.mommoo.conf.ServerSpec;
import com.mommoo.contents.ServerContents;
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 제가 정의한 WAS 프로세스를 수행하는 클래스 입니다.
 *
 * 해당 클래스는 같은 포트의 {@link ServerSpec}들로 구성되어 실행됩니다.
 *
 * @author mommoo
 */
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

    public void addServerSpecs(List<ServerSpec> serverSpecs) throws InvalidParameterException {
        for (ServerSpec serverSpec : serverSpecs) {
            addServerSpec(serverSpec);
        }
    }

    private void addServerSpec(ServerSpec serverSpec) throws InvalidParameterException {
        // 처음으로 등록되는 ServerSpec의 포트번호를 SimpleServer가 구동할 포트번호로 구성합니다.
        if (portNumber == -1) {
            portNumber = serverSpec.getPortNumber();
        }

        // 만약 포트번호가 다른 ServerSpec이라면, InvalidParameterException을 던집니다.
        if (portNumber != serverSpec.getPortNumber()) {
            String errorMsg = "서로 다른 포트 번호를 가진 서버 스펙을 동시에 보유할 수 없습니다." + portNumber + " , " + serverSpec.getPortNumber();
            setLogPath(mainLogPath);
            logger.error(errorMsg, new InvalidParameterException());
            throw new InvalidParameterException(errorMsg);
        }

        // 모든 경우를 매칭하는 ServerSpec을 구분합니다.
        if (serverSpec.getServerName().equals("*")) {
            anyMatchServerSpec = serverSpec;
        } else {
            serverSpecList.add(serverSpec);
        }
    }

    public void start() {
        // 등록된 ServerSpec이 없다면 실행하지 않습니다.
        if (portNumber == -1) {
            setLogPath(mainLogPath);
            logger.error("ServerSpec이 적어도 한개 이상 존재해야 합니다.");
            return;
        }

        // SimpleServer의 정보를 로깅합니다.
        printLogOfServerInfo();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            logger.error("서버 소켓을 열지 못했습니다.", e);
            return;
        }

        // 요청 처리를 병렬로 하기 위한 스레드 서비스 인스턴스를 생성합니다.
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        while (true) {
            logger.info(this + "의 Connection Listen 시작");

            Socket tempSocket = null;
            try {
                tempSocket = serverSocket.accept();
            } catch (IOException e) {
                logger.error("소켓을 받지 못했습니다.", e);
                continue;
            }

            // 클라이언트의 소켓 정보를 로깅합니다.
            printLogOfClientInfo(tempSocket);

            // 자바 익명클래스는 final 변수로 받아야 하므로, 어쩔 수 없이 추가적인 코드를 넣었습니다.
            final Socket socket = tempSocket;

            executorService.submit(() -> {
                // 사용자에게 HttpResponse 응답을 보낼 수 있는 인스턴스를 생성합니다.
                HttpResponseSender responseSender = new HttpResponseSender(socket);

                HttpRequest httpRequest = null;
                ServerSpec serverSpec = null;

                // ServerSpec 이 구성되기 전, 서버 에러를 검증하기 위한 try-catch 입니다.
                // 여기서 발생하는 서버 에러는 ServerSpec을 구성하기 전에 발생하므로, 사용자가 정의한 HTML 파일을 찾을 수 없습니다.
                // 따라서, 자체적으로 500 Error HTML를 구성하여 처리합니다.
                try {
                    //HttpRequest 인스턴스를 구성합니다.
                    HttpRequestBuilder requestBuilder = new HttpRequestBuilder(mainLogPath, socket);
                    httpRequest = requestBuilder.build();

                    // Host 정보를 토대로 매칭되는 ServerSpec을 검사합니다.
                    String host = httpRequest.getHeader(HttpHeaderType.HOST);
                    serverSpec = findServerSpecByHostOrNull(host);

                    // 매칭되는 ServerSpec이 존재하지 않습니다.
                    // 구성된 ServerSpec이 없으므로, 사용자가 정의한 HTML 파일을 찾을 수 없습니다.
                    // 따라서, 자체적으로 412 Error HTML를 구성하여 처리합니다.
                    if (serverSpec == null) {
                        // 비정상적인 HOST 접근이므로, HOST 매핑 에러 정보를 출력합니다.
                        printLogOfInvalidateHostAccess(host);
                        // ServerSpec이 정의되지 않았으므로, 자체적으로 412 HTML을 구성하여 처리합니다.
                        responseSender.sendBasicHTMLPage(HttpStatus.CODE_412, mainLogPath);
                        return;
                    }
                } catch (Exception e) {
                    setLogPath(mainLogPath);
                    logger.error("서버 에러가 발생했습니다.", e);
                    responseSender.sendBasicHTMLPage(HttpStatus.CODE_500, mainLogPath);
                    return;
                }

                // ServerSpec이 올바르게 구성 된 후 ServerSpec이 정의한 로깅이 동작합니다.
                String serverSpecLog = serverSpec.getLogPath();

                // ServerSpec 이 구성된 후, 서버 에러를 검증하기 위한 try-catch 입니다.
                // 여기서 발생하는 서버 에러는 ServerSpec이 기술한 500에러 파일 매핑을 처리합니다.
                try {
                    // HttpRequest 검증을 시도합니다.
                    HttpRequestRuleResult ruleResult
                            = HttpRequestRuleInspector
                            .getInstance()
                            .isValidateRequest(serverSpec, httpRequest);

                    // HttpRequest 인스턴스 정보와 검증 결과를 로깅합니다.
                    printLogOfHttpRequest(serverSpecLog, httpRequest, ruleResult);

                    // 검증을 통과하지 못했다면 403 에러로 설정합니다.
                    HttpStatus httpStatus = ruleResult.isValidate ? HttpStatus.CODE_200 : HttpStatus.CODE_403;

                    // 환경에 알맞은 컨텐츠를 찾기 위해 해당 인스턴스를 구성합니다.
                    ServerContentsFinder contentsFinder = new ServerContentsFinder(serverSpec, httpRequest.getURI(), httpStatus);

                    // 클라이언트에게 알맞은 컨텐츠를 보냅니다.
                    sendResponseWithContents(httpRequest, responseSender, contentsFinder, serverSpecLog);

                } catch(Exception e) {
                    // ServerSpec이 구성됬으므로, 메인 로그 와 서버 로그 둘다 로깅합니다.
                    eachPrintErrorLog(serverSpec, "서버 에러가 발생했습니다.", e);

                    // 500에러를 처리하기 위한 컨텐츠를 찾기 위해 해당 인스턴스를 구성합니다.
                    ServerContentsFinder contentsFinder = new ServerContentsFinder(serverSpec, "", HttpStatus.CODE_500);
                    sendResponseWithContents(httpRequest, responseSender, contentsFinder, serverSpecLog);
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    // ServerSpec이 구성됬으므로, 메인 로그 와 서버 로그 둘다 로깅합니다.
                    eachPrintErrorLog(serverSpec, "socket을 닫지 못했습니다.", e);
                }
            });
        }
    }

    private ServerSpec findServerSpecByHostOrNull(String host) {
        // host가 포트번호 까지 포함되어 있는지 확인합니다.
        boolean isPortNumberContain = host.contains(":");
        for (ServerSpec serverSpec : serverSpecList) {
            // host 포트번호 유무에 따라, ServerSpec의 정보도 똑같이 구성합니다.
            String serverSpecHost = serverSpec.getServerName() + (isPortNumberContain ? ":" + serverSpec.getPortNumber() : "");
            if (serverSpecHost.equals(host)) {
                // 찾은 ServerSpec를 리턴합니다.
                return serverSpec;
            }
        }

        // 만약 모든 경우를 매칭하는 ServerSpec이 존재한다면 리턴합니다.
        // 모든 경우를 매칭하는 ServerSpec이 없는 경우도 있습니다. (null 리턴)
        return anyMatchServerSpec;
    }

    private void eachPrintErrorLog(ServerSpec serverSpec, String errorMsg, Throwable t) {
        // 메인 로그는 어떤 ServerSpec에서 발생하였는지 정보 까지 출력합니다.
        setLogPath(mainLogPath);
        logger.error(serverSpec.toString().concat(":: ").concat(errorMsg), t);

        setLogPath(serverSpec.getLogPath());
        logger.error("서버 에러가 발생했습니다.", t);
    }

    private void setLogPath(String logPath) {
        MDC.put("logPath", logPath);
    }

    private void printLogOfServerInfo() {
        setLogPath(mainLogPath);
        logger.info(this.toString().concat("를 구동했습니다.").concat("\n")
                .concat("## 총 스레드 개수 : ").concat(Integer.toString(threadCount)).concat("\n")
                .concat("## 개방 포트 : ").concat(Integer.toString(portNumber)).concat("\n")
                .concat("## Server Spec 개수 : ").concat(Integer.toString(serverSpecList.size())).concat("\n"));
    }

    private void printLogOfClientInfo(Socket socket) {
        setLogPath(mainLogPath);
        String clientIP = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString();
        logger.info(this.toString().concat("의 Connection Binding").concat("\n")
                .concat("## Client IP : ").concat(clientIP).concat("\n"));
    }

    private static void printLogOfHttpRequest(String serverSpecLogPath, HttpRequest httpRequest, HttpRequestRuleResult ruleResult) {
        MDC.put("logPath", serverSpecLogPath);
        logger.info("Accept HttpRequest\n"
                .concat(httpRequest.toString()).concat("\n")
                .concat(ruleResult.toString()).concat("\n")
        );
    }

    private void printLogOfInvalidateHostAccess(String host) {
        setLogPath(mainLogPath);
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

    private void sendResponseWithContents(HttpRequest httpRequest, HttpResponseSender responseSender, ServerContentsFinder contentsFinder, String serverSpecLogPath) {
        ServerContents serverContents = contentsFinder.getContents();
        switch (serverContents.getType()) {
            case NONE:
                // 매핑되는 컨텐츠가 없다면, 자체적으로 HTML를 구성하여 처리합니다.
                responseSender.sendBasicHTMLPage(serverContents.getHttpStatus(), serverSpecLogPath);
                break;
            case SERVLET:
                // 서블릿이 매칭된다면, 서블릿에게 컨텐츠의 구성 기회를 넘깁니다.
                HttpResponse httpResponse = new HttpResponseHandler();
                httpResponse.setStatus(serverContents.getHttpStatus());

                SimpleServlet simpleServlet = (SimpleServlet)serverContents.get();
                simpleServlet.service(httpRequest, httpResponse);

                // 서블릿이 구성한 컨텐츠를 처리합니다.
                responseSender.send(httpResponse, serverSpecLogPath);
                break;
            case FILE:
                // 파일이 매칭된다면, 파일 내용을 읽어 처리합니다.
                Path filePath = (Path)serverContents.get();
                responseSender.sendFile(serverContents.getHttpStatus(), filePath, serverSpecLogPath);
                break;
        }
    }

    @Override
    public String toString() {
        return String.format("SimpleServer[%-6d]", portNumber);
    }
}
