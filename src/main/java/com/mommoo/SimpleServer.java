package com.mommoo;

import com.mommoo.conf.ServerConfiguration;
import com.mommoo.http.request.HttpRequestBuilder;
import com.mommoo.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO 정적 파일을 Jar 파일 바깥에다가 위치시키게 코딩한다.
public class SimpleServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);

    private final ServerConfiguration serverConfiguration;

    public SimpleServer(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public void start() {
        int nThread = serverConfiguration.getThreadCount();
        int portNumber = serverConfiguration.getPortNumber();
        logger.info("WAS 를 구동했습니다.\n## 총 스레드 개수 : " + nThread +"\n## 개방 포트 : " + portNumber);

        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                logger.info("Connection을 기다리고 있습니다.");
                Socket socket = serverSocket.accept();
                logger.info(getSocketInfo(socket));

                HttpRequestBuilder requestBuilder = new HttpRequestBuilder(socket);
                requestBuilder.setOnBuildCompleteListener(httpRequest -> {
                    if (httpRequest.getURI().equals("/")) {
                        try {
                            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                            bos.write("HTTP/1.1 200 OK\nContent-Type: text/html\n\n".getBytes());
                            bos.flush();
                            String indexPagePath = serverConfiguration.getRootHomePath() + "/" + serverConfiguration.getIndexPage();
                            System.out.println(indexPagePath);
                            System.out.println(FileUtils.getResourcePathOrNull(indexPagePath));
                            Path path = Paths.get(FileUtils.getResourcePathOrNull(indexPagePath));
                            FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
                            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
                            while(fileChannel.read(byteBuffer) != -1) {
                                byteBuffer.flip();
                                bos.write(byteBuffer.array());
                                byteBuffer.clear();
                            }
                            System.out.println("alldone!");
                            bos.flush();
                            bos.close();

                            fileChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                executorService.submit(requestBuilder);
            }

        } catch(IOException io) {
            logger.error("소켓을 열지 못하여 서버가 다운 됬습니다.", io);
        }
    }

    private String getSocketInfo(Socket socket) {
        return new StringBuilder()
                .append("Accept Connection").append("\n")
                .append("## Connection IP : ").append(socket.getRemoteSocketAddress()).append("\n")
                .toString();
    }
}
