import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception{
        ServerSocket socket = new ServerSocket(8080);
        System.out.println("대기중...");
        Socket acceptedSocket = socket.accept();

        StringBuilder builder = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(acceptedSocket.getInputStream()), 1024);


        char [] buffer = new char[1024];

        while (true) {

            System.out.println("받은 값..----------------------------");
            String dd = bufferedReader.readLine();
            System.out.println(dd);
            if (dd.equals("")) {
                System.out.println("break!!");
                break;
            }
        }

        System.out.println(builder.toString());


        System.out.println("종료!");
        socket.close();
        bufferedReader.close();
    }
}
