package com.mommoo.conf;

import com.mommoo.utils.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ServerConfigurationTest {
    @Test()
    @DisplayName("ServerConfiguration 객체 생성 테스트")
    public void serverConfigurationCreateTest() throws Exception {
        String serverConfigFilePath = FileUtils.getResourcePathOrNull("server.json");
        ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigFilePath);
    }

    @Test()
    @DisplayName("ServerSpec 객체 생성 및 값 테스트")
    public void serverSpecTest() throws Exception {
        String serverConfigFilePath = FileUtils.getResourcePathOrNull("server.json");
        ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigFilePath);

        for (ServerSpec serverSpec : serverConfiguration.getServerSpecs(1111)) {
            doAssertEquals(serverSpec, 1);
        }

        for (ServerSpec serverSpec : serverConfiguration.getServerSpecs(2222)) {
            doAssertEquals(serverSpec, 2);
        }
    }

    private void doAssertEquals(ServerSpec serverSpec, int prefix) {
        Assertions.assertEquals(serverSpec.getThreadCount(), 20 - prefix);
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
