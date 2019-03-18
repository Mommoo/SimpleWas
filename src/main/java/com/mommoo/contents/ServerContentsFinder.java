package com.mommoo.contents;

import com.mommoo.conf.ServerSpec;
import com.mommoo.http.HttpStatus;
import com.mommoo.servlet.SimpleServlet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerContentsFinder {
    private ServerContents serverContents;
    private Path filePath;

    public ServerContentsFinder(ServerSpec serverSpec, String URI, HttpStatus httpStatus) {
        boolean isHttpOK = httpStatus == HttpStatus.CODE_200;

        if (isHttpOK && isServletRequest(URI)) {
            this.serverContents = ServerContents.SERVLET;
            return;
        }

        String fileURI = isHttpOK ? URI : serverSpec.getErrorIndexPageOrNull(httpStatus);

        if (isHttpOK && URI.equals("/")) {
            fileURI = "/".concat(serverSpec.getIndexPage());
        }

        String documentPath = serverSpec.getDocumentPath();

        if (isFileNotExist(documentPath, fileURI)) {
            serverContents = ServerContents.NONE;
        } else {
            serverContents = ServerContents.FILE;
            this.filePath = Paths.get(documentPath, fileURI);
        }
    }

    public ServerContents getServerContents() {
        return serverContents;
    }

    public SimpleServlet getServlet(String URI) {
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

    public Path getFilePath() {
        return filePath;
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
