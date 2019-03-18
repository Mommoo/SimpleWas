package com.mommoo.utils;

import com.mommoo.conf.ServerSpec;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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

    public static void copyFileToStream(Path path, OutputStream outputStream) throws IOException {
        copyFileToWriter(path, new OutputStreamWriter(outputStream));
    }

    public static void copyFileToWriter(Path path, Writer writer) throws IOException {
        int size = 2048;
        BufferedWriter bufferedWriter = new BufferedWriter(writer, size);
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        Charset charset = Charset.forName("UTF-8");

        while (fileChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();
            bufferedWriter.write(charset.decode(byteBuffer).toString());
            byteBuffer.clear();
        }

        bufferedWriter.flush();
        fileChannel.close();
    }

    public static Path getPathIfExist(String basicPath, String URI) {
        if (basicPath == null || URI == null) {
            return null;
        }
        Path filePath = Paths.get(basicPath, URI);
        if (Files.notExists(filePath)) {
            return null;
        }

        return filePath;
    }
}
