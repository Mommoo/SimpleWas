package com.mommoo.conf;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ServerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfiguration.class);

    private static final int MIN_THREAD_COUNT = 1;
    private static final int MAX_THREAD_COUNT = 50;

    private static final String THREAD_COUNT = "threadCount";
    private static final String PORT_NUMBER = "portNumber";
    private static final String HOME_PATH = "homePath";
    private static final String INDEX_PAGE = "indexPage";

    private int threadCount = 30;
    private int portNumber = 8080;
    private String homePath = "home";
    private String indexPage = "index.html";

    public ServerConfiguration () {

    }

    public ServerConfiguration(String configFilePath) {
        File file = new File(configFilePath);

        try {
            FileReader fileReader = new FileReader(file);
            JSONObject configJSONObject = (JSONObject) new JSONParser().parse(fileReader);
            long threadCount = (Long)configJSONObject.get(THREAD_COUNT);
            this.threadCount = (int)Math.max(MIN_THREAD_COUNT, Math.min(threadCount, MAX_THREAD_COUNT));

            long portNumber = (Long)configJSONObject.get(PORT_NUMBER);
            if (0 < portNumber  && portNumber <= 65535) {
                this.portNumber = (int)portNumber;
            }

            this.homePath = (String) configJSONObject.get(HOME_PATH);

            this.indexPage = (String) configJSONObject.get(INDEX_PAGE);


        } catch (ParseException e) {
            logger.error("서버 configuration 파일 구성이 JSON이 아닙니다.", e);
        } catch (ClassCastException ce) {
            logger.error("서버 configuration 요소의 데이터 타입이 맞지 않습니다.");
        } catch (NullPointerException ne) {
            logger.error("서버 configuration 요소 키 값이 존재하지 않습니다.");
        } catch (IOException io) {
            logger.info("서버 configuration 파일이 존재하지 않습니다.");
        }
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

    public String getRootHomePath() {
        return this.homePath;
    }

    public String getIndexPage() {
        return this.indexPage;
    }
}
