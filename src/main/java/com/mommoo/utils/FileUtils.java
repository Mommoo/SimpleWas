package com.mommoo.utils;

import java.io.File;
import java.net.URL;

public class FileUtils {
    private FileUtils(){

    }

    public static String getResourcePathOrNull(String path) {
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        URL fileURL = classLoader.getResource(path);

        if (fileURL == null) {
            return null;
        }

        File file = new File(fileURL.getFile());
        return file.getAbsolutePath();
    }
}
