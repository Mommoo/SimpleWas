package com.mommoo.contents;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.HttpStatus;
import com.mommoo.servlet.SimpleServlet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@link ServerSpec}에 맞는 컨텐츠를 찾는 역할을 가진 클래스 입니다.
 *
 * 컨텐츠를 찾는 과정에서 {@link HttpStatus}가 변경될 수도 있습니다.
 *  ex) 올바른 요청이지만, 컨텐츠를 찾이 못한 경우 : {@link HttpStatus#CODE_200} -> {@link HttpStatus#CODE_404}
 *
 * 찾은 결과물을 {@link ServerContents} 인스턴스로 제공합니다.
 *
 * @author mommoo
 */
public class ServerContentsFinder {
    private final ServerContents serverContents;

    public ServerContentsFinder(ServerSpec serverSpec, String URI, HttpStatus httpStatus) {
        boolean isHttpOK = httpStatus == HttpStatus.CODE_200;

        if (isHttpOK && isServletRequest(URI)) {
            SimpleServlet servlet = createServlet(URI);
            serverContents = new ServerContents(HttpStatus.CODE_200, ServerContentsType.SERVLET, servlet);
            return;
        }

        String fileURI = isHttpOK ? URI : serverSpec.getErrorIndexPageOrNull(httpStatus);

        if (isHttpOK && URI.equals("/")) {
            fileURI = "/".concat(serverSpec.getIndexPage());
        }

        String documentPath = serverSpec.getDocumentPath();

        if (isFileNotExist(documentPath, fileURI)) {
            HttpStatus properStatus = isHttpOK ? HttpStatus.CODE_404 : httpStatus;
            serverContents = new ServerContents(properStatus, ServerContentsType.NONE, null);
            return;
        }

        Path filePath = Paths.get(documentPath, fileURI);
        serverContents = new ServerContents(httpStatus, ServerContentsType.FILE, filePath);
    }

    public ServerContents getContents() {
        return serverContents;
    }

    private SimpleServlet createServlet(String URI) {
        String lastResourceName = URI.substring(URI.lastIndexOf("/") + 1);
        try {
            return (SimpleServlet) Class.forName(lastResourceName).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isFileNotExist(String documentPath, String fileURI) {
        if (fileURI == null) {
            return true;
        }

        Path filePath = Paths.get(documentPath, fileURI);
        return Files.isDirectory(filePath) || Files.notExists(filePath);
    }

    public static boolean isServletRequest(String URI) {
        String lastResourceName = URI.substring(URI.lastIndexOf("/") + 1);
        try {
            Class.forName(lastResourceName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
