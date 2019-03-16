import com.mommoo.SimpleServer;
import com.mommoo.conf.JSONKeyNotFoundException;
import com.mommoo.conf.ServerConfiguration;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String serverConfigPath = args[0];
            ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigPath);
        } catch (ArrayIndexOutOfBoundsException aobe) {
            System.err.println("server.json 설정 파일 경로를 넣어주세요.");
            aobe.printStackTrace();
        } catch (IOException ioe) {
            System.err.println("server.json 설정 파일이 존재 하지 않습니다.");
            ioe.printStackTrace();
        } catch (ParseException pe) {
            System.err.println("server.json 설정 파일의 데이터 구성이 JSON이 아닙니다.");
            pe.printStackTrace();
        } catch (JSONKeyNotFoundException jnfe) {
            System.err.println("server.json 설정 파일의 필요한 데이터 키가 없습니다.");
            jnfe.printStackTrace();
        } catch (ClassCastException cce) {
            System.err.println("server.json 설정 파일의 데이터 값 타입이 올바르지 않습니다.");
            cce.printStackTrace();
        }
    }
}
