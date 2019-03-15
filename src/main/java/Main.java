import com.mommoo.SimpleServer;
import com.mommoo.conf.ServerConfiguration;

public class Main {
    public static void main(String[] args) {
        new SimpleServer(new ServerConfiguration()).start();
    }
}
