package com.mommoo.contents;

import com.mommoo.conf.ServerSpec;
import com.mommoo.conf.ServerSpecBuilder;

public class ServerContentsFinderTest {

    private static ServerSpec createMockServerSpec() {
        return new ServerSpecBuilder().setDocumentPath("/var/www")
                .setLogPath("log")
                .setIndexPage("index.html")
                .setPortNumber(6766)
                .setServerName("mommoo.com")
                .build();
    }


}
