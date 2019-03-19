package com.mommoo.conf;

import com.mommoo.utils.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 해당 테스트 클래스는 2가지를 테스트합니다. server.json 파일은 src.test.resources.server.json 경로에 위치 합니다.
 *
 *  1. classPath에 존재하는 server.json 파일을 대상으로,
 *     {@link ServerConfiguration} 인스턴스가 올바르게 생성되는지 테스트 합니다. {@link #serverConfigurationCreateTest()}
 *
 *  2. classPath에 존재하는 server.json 파일을 대상으로, 관련 인스턴스가 올바르게 생성되는지 테스트 하며, {@link ServerSpec}
 *     값을 올바르게 파싱하였는지 여부를 테스트 합니다. {@link #serverSpecTest()}
 *
 */
public class ServerConfigurationTest {
    @Test()
    @DisplayName("ServerConfiguration 객체 생성 테스트")
    public void serverConfigurationCreateTest() throws Exception {
        // classpath resource 경로에 있는 server.json 파일 경로를 구합니다.
        String serverConfigFilePath = FileUtils.getResourcePathOrNull("server.json");
        // server.json 파일로 ServerConfiguration 인스턴스화를 진행합니다.
        ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigFilePath);
    }

    @Test()
    @DisplayName("ServerSpec 객체 생성 및 값 테스트")
    public void serverSpecTest() throws Exception {
        // classpath resource 경로에 있는 server.json 파일 경로를 구합니다.
        String serverConfigFilePath = FileUtils.getResourcePathOrNull("server.json");
        // server.json 파일로 ServerConfiguration 인스턴스화를 진행합니다.
        ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigFilePath);

        // server.json 파일에 적힌 값을 하드 코딩으로 확인합니다.
        Assertions.assertEquals(serverConfiguration.getThreadCount(), 20);
        Assertions.assertEquals(serverConfiguration.getMainLogPath(), "log");

        for (ServerSpec serverSpec : serverConfiguration.getServerSpecs(1111)) {
            doServerSpecAssertEquals(serverSpec, 1);
        }

        for (ServerSpec serverSpec : serverConfiguration.getServerSpecs(2222)) {
            doServerSpecAssertEquals(serverSpec, 2);
        }
    }

    private void doServerSpecAssertEquals(ServerSpec serverSpec, int prefix) {
        Assertions.assertEquals(serverSpec.getServerName(), "mommoo"+prefix+".com");
        Assertions.assertEquals(serverSpec.getPortNumber(), 1000*prefix + 100*prefix + 10*prefix + prefix);
        Assertions.assertEquals(serverSpec.getDocumentPath(), "home"+prefix);
        Assertions.assertEquals(serverSpec.getLogPath(), "log" +prefix);
        Assertions.assertEquals(serverSpec.getIndexPage(), "index"+prefix+".html");
        Map<Integer, String> errorPage = serverSpec.getErrorPage();
        for (int code : errorPage.keySet()) {
            Assertions.assertEquals(errorPage.get(code), "error"+prefix+code+".html");
        }
    }
}
