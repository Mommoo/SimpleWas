package com.mommoo.conf;

import com.mommoo.utils.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ServerConfigurationTest {

    @Test()
    @DisplayName("서버 Configuratuon 파일 객체화 테스트")
    public void serverConfigurationCreateTest() {
        String serverConfigFilePath = FileUtils.getResourcePathOrNull("server.json");
        ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigFilePath);

        Assertions.assertEquals(serverConfiguration.getThreadCount(), 22);
        Assertions.assertEquals(serverConfiguration.getPortNumber(), 5514);
        Assertions.assertEquals(serverConfiguration.getRootHomePath(), "home");
        Assertions.assertEquals(serverConfiguration.getIndexPage(), "index.html");
    }

    @Test()
    @DisplayName("Index 페이지 로드 테스트")
    public void loadIndexPageFileTest() {
        String serverConfigFilePath = FileUtils.getResourcePathOrNull("server.json");
        ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigFilePath);
        String indexPagePath = serverConfiguration.getRootHomePath() + "/" + serverConfiguration.getIndexPage();
        indexPagePath = FileUtils.getResourcePathOrNull(indexPagePath);

        StringBuilder stringBuilder = new StringBuilder();

        try {
            Path path = Paths.get(indexPagePath);
            FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            Charset charset = Charset.defaultCharset();


            while (fileChannel.read(byteBuffer) != -1 ) {
                byteBuffer.flip();
                stringBuilder.append(charset.decode(byteBuffer).toString());
                byteBuffer.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(stringBuilder.toString().contains("Hello World!!"));
    }

    private static String getResourcePath() {
        return ServerConfigurationTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
