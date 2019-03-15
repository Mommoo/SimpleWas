import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception{
        File file = new File("hello.conf");
        FileOutputStream out = new FileOutputStream(file);
        out.write("hello~".getBytes());
        out.flush();
        out.close();

        file = new File("hello.conf");
        FileInputStream inputStream = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        System.out.println(bufferedReader.readLine());
        bufferedReader.close();
    }
}
