package com.mommoo.conf;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerSpec {
    private final int threadCount;
    private final String serverName;
    private final int portNumber;
    private final String documentPath;
    private final String logPath;
    private final String indexPage;
    private final Map<Integer, String> errorPage;

    ServerSpec(int threadCount, String serverName, int portNumber, String documentPath, String logPath, String indexPage, Map<Integer, String> errorPage) {
        this.threadCount = threadCount;
        this.serverName = serverName;
        this.portNumber = portNumber;
        this.documentPath = documentPath;
        this.logPath = logPath;
        this.indexPage = indexPage;
        this.errorPage = errorPage;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public String getServerName() {
        return serverName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getIndexPage() {
        return indexPage;
    }

    public Map<Integer, String> getErrorPage() {
        return Collections.unmodifiableMap(errorPage);
    }
}
