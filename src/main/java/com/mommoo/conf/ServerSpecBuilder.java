package com.mommoo.conf;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServerSpecBuilder {
    private static final int MIN_THREAD_COUNT = 1;
    private static final int MAX_THREAD_COUNT = 20;
    private static final int DEFAULT_SERVER_PORT = 8080;

    private int threadCount;
    private String serverName;
    private int portNumber;
    private String documentPath;
    private String logPath;
    private String indexPage;
    private Map<Integer, String> errorPage = new HashMap<>();

    public ServerSpecBuilder setThreadCount(long nThread) {
        this.threadCount = (int)Math.max(MIN_THREAD_COUNT, Math.min(nThread, MAX_THREAD_COUNT));
        return this;
    }

    public ServerSpecBuilder setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public ServerSpecBuilder setPortNumber(long portNumber) {
        if (0 < portNumber  && portNumber <= 65535) {
            this.portNumber = (int)portNumber;
        } else {
            this.portNumber = DEFAULT_SERVER_PORT;
        }

        return this;
    }

    public ServerSpecBuilder setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
        return this;
    }

    public ServerSpecBuilder setLogPath(String logPath) {
        this.logPath = logPath;
        return this;
    }

    public ServerSpecBuilder setIndexPage(String indexPage) {
        this.indexPage = indexPage;
        return this;
    }

    public ServerSpecBuilder addErrorPage(long errorCode, String errorPage) {
        this.errorPage.put((int)errorCode, errorPage);
        return this;
    }

    public ServerSpec build() {
        return new ServerSpec(threadCount, serverName, portNumber, documentPath, logPath, indexPage, errorPage);
    }

    public static ServerSpec buildDefault() {
        return new ServerSpecBuilder()
                .setThreadCount(MAX_THREAD_COUNT/2)
                .setServerName("localHost")
                .setPortNumber(DEFAULT_SERVER_PORT)
                .setDocumentPath("home")
                .setLogPath("log")
                .setIndexPage("index.html")
                .addErrorPage(403, "error403.html")
                .addErrorPage(404, "error404.html")
                .addErrorPage(500, "error500.html")
                .build();
    }
}
