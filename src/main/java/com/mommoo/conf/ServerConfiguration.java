package com.mommoo.conf;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ServerConfiguration {
    private static final String MAIN_LOG_PATH = "mainLogPath";
    private static final String SERVER_SPEC = "serverSpec";

    private static final String THREAD_COUNT = "threadCount";
    private static final String SERVER_NAME = "serverName";
    private static final String PORT_NUMBER = "portNumber";
    private static final String DOCUMENT_PATH = "documentPath";
    private static final String LOG_PATH = "logPath";
    private static final String INDEX_PAGE = "indexPage";
    private static final String ERROR_PAGE = "errorPage";

    private static final String[] SERVER_SPEC_KEYS = {THREAD_COUNT, SERVER_NAME, PORT_NUMBER, DOCUMENT_PATH, LOG_PATH, INDEX_PAGE, ERROR_PAGE};

    private static final String CODE = "code";
    private static final String PAGE = "page";

    private final String mainLogPath;
    private final Map<Integer, List<ServerSpec>> serverSpecFinder = new HashMap<>();

    public ServerConfiguration(String configFilePath) throws IOException, ClassCastException, ParseException, JSONKeyNotFoundException {
        FileReader fileReader = new FileReader(new File(configFilePath));

        JSONObject serverJSONObject = (JSONObject) new JSONParser().parse(fileReader);

        if (!isContainServerConfigurationKey(serverJSONObject)) {
            throw new JSONKeyNotFoundException();
        }

        this.mainLogPath = (String) serverJSONObject.get(MAIN_LOG_PATH);

        JSONArray serverSpecJSONArray = (JSONArray) serverJSONObject.get(SERVER_SPEC);

        for (Object serverSpecObject : serverSpecJSONArray) {
            JSONObject serverSpecJSON = (JSONObject) serverSpecObject;

            if (!isContainServerSpecKeys(serverSpecJSON)) {
                throw new JSONKeyNotFoundException();
            }

            ServerSpec serverSpec = parseServerSpec(serverSpecJSON);
            addServerSpec(serverSpec);
        }
    }

    private static boolean isContainServerConfigurationKey(JSONObject jsonObject) {
        return jsonObject.containsKey(MAIN_LOG_PATH) && jsonObject.containsKey(SERVER_SPEC);
    }

    private static boolean isContainServerSpecKeys(JSONObject jsonObject) {
        boolean isKeyExist = Arrays.stream(SERVER_SPEC_KEYS).allMatch(jsonObject::containsKey);

        if (!isKeyExist) {
            return false;
        }

        JSONArray errorPageJSONArray = (JSONArray) jsonObject.get(ERROR_PAGE);
        for (Object errorPageObject : errorPageJSONArray) {
            JSONObject errorPageJSONObject = (JSONObject) errorPageObject;

            if ( errorPageJSONObject.containsKey(CODE) && errorPageJSONObject.containsKey(PAGE)) {
                continue;
            }

            return false;
        }
        return true;
    }

    private ServerSpec parseServerSpec(JSONObject jsonObject) throws ClassCastException {
        ServerSpecBuilder serverSpecBuilder
                = new ServerSpecBuilder()
                .setThreadCount((Long) jsonObject.get(THREAD_COUNT))
                .setServerName((String) jsonObject.get(SERVER_NAME))
                .setPortNumber((Long) jsonObject.get(PORT_NUMBER))
                .setDocumentPath((String) jsonObject.get(DOCUMENT_PATH))
                .setLogPath((String) jsonObject.get(LOG_PATH))
                .setIndexPage((String) jsonObject.get(INDEX_PAGE));

        JSONArray errorPageJSONArray = (JSONArray)jsonObject.get(ERROR_PAGE);
        for (Object errorPageObject : errorPageJSONArray) {
            JSONObject errorPageJSONObject = (JSONObject) errorPageObject;
            serverSpecBuilder.addErrorPage((long)errorPageJSONObject.get(CODE), (String) errorPageJSONObject.get(PAGE));
        }

        return serverSpecBuilder.build();
    }

    private void addServerSpec(ServerSpec serverSpec) {
        if (!serverSpecFinder.containsKey(serverSpec.getPortNumber())) {
            serverSpecFinder.put(serverSpec.getPortNumber(), new ArrayList<>());
        }

        serverSpecFinder
                .get(serverSpec.getPortNumber())
                .add(serverSpec);
    }

    public String getMainLogPath() {
        return mainLogPath;
    }

    public List<ServerSpec> getServerSpecs(int portNumber) {
        return Collections.unmodifiableList(serverSpecFinder.get(portNumber));
    }

    public List<Integer> getServerPortList() {
        return Collections.unmodifiableList(new ArrayList<>(serverSpecFinder.keySet()));
    }
}
