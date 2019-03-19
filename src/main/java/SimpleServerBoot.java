import com.mommoo.SimpleServer;
import com.mommoo.conf.JSONKeyNotFoundException;
import com.mommoo.conf.ServerConfiguration;
import com.mommoo.conf.ServerSpec;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * Boot 클래스 입니다.
 *
 * 설정 파일 경로를 받아, {@link ServerConfiguration} 인스턴스를 생성하고,
 * {@link ServerSpec}을 분류하여 {@link SimpleServer}에 전달하고 실행하는 역할을 가집니다.
 *
 * 설정 파일에 기술되어 있는 mainLog가 파싱되지 않을 수 있으니,
 * 해당 클래스의 로그는 전부 System.out 인스턴스를 이용했습니다.
 */
public class SimpleServerBoot {
    public static void main(String[] args) {
        try {
            // 파일 경로를 받습니다.
            String serverConfigPath = args[0];

            // 파일 구성을 인스턴스화 합니다. 파일 구성에 실패하면 하단의 catch 코드를 타게 됩니다.
            ServerConfiguration serverConfiguration = new ServerConfiguration(serverConfigPath);

            // 파일 구성에 적힌 mainLogPath 와 threadCount를 가져옵니다.
            String mainLogPath = serverConfiguration.getMainLogPath();
            int threadCount = serverConfiguration.getThreadCount();

            // 포트별로 SimpleServer를 구동 시킵니다.
            for (int portNumber : serverConfiguration.getServerPortList()) {
                // 포트별로 분류된 ServerSpec 리스트를 구합니다.
                List<ServerSpec> serverSpecList = serverConfiguration.getServerSpecs(portNumber);

                // SimpleServer 인스턴스를 생성합니다.
                SimpleServer simpleServer = new SimpleServer(mainLogPath, threadCount);
                // 같은 포트로 구성된 ServerSpec 리스트를 SimpleServer에 추가 합니다.
                simpleServer.addServerSpecs(serverSpecList);

                // 여러개의 포트가 존재하는 경우 여러 SimpleServer가 실행 될 수 있으므로
                // 스레드로 병렬 처리를 진행했습니다.
                new Thread(simpleServer::start).start();

            }

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
        } catch (InvalidParameterException ipe) {
            System.err.println("SimpleServer에 유효하지 않은 인자를 추가했습니다.");
            ipe.printStackTrace();
        }
    }
}
