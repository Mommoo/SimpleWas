package com.mommoo.conf;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ServerSpec}의 빌더 클래스 입니다.
 *
 * @author mommoo
 */
public class ServerSpecBuilder {
    private static final int DEFAULT_SERVER_PORT = 8080;

    private String serverName;
    private int portNumber;
    private String documentPath;
    private String logPath;
    private String indexPage;
    private Map<Integer, String> errorPage = new HashMap<>();

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
        return new ServerSpec(serverName, portNumber, documentPath, logPath, indexPage, errorPage);
    }
}
