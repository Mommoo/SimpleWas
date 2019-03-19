package com.mommoo.conf;

import com.mommoo.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * 서버 설정 파일의 'serverSpec'에 해당하는 데이터 값을 정의한 클래스 입니다.
 *
 * @author mommoo
 */
public class ServerSpec {
    private final String serverName;
    private final int portNumber;
    private final String documentPath;
    private final String logPath;
    private final String indexPage;
    private final Map<Integer, String> errorPage;

    ServerSpec(String serverName, int portNumber, String documentPath, String logPath, String indexPage, Map<Integer, String> errorPage) {
        this.serverName = serverName;
        this.portNumber = portNumber;
        this.documentPath = documentPath;
        this.logPath = logPath;
        this.indexPage = indexPage;
        this.errorPage = errorPage;
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

    public String getErrorIndexPageOrNull(HttpStatus httpStatus) {
        return errorPage.get(httpStatus.getCodeNum());
    }

    public Map<Integer, String> getErrorPage() {
        return Collections.unmodifiableMap(errorPage);
    }

    @Override
    public String toString() {
        return "[ ServerName=".concat(serverName)
                .concat(", PortNumber=").concat(Integer.toString(portNumber)
                        .concat(", DocumentPath=").concat(documentPath)
                        .concat(", LogPath=").concat(logPath)
                        .concat(" ]"));
    }
}
