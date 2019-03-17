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
    private static final int MIN_THREAD_COUNT = 1;
    private static final int MAX_THREAD_COUNT = 20;

    private static final String MAIN_LOG_PATH = "mainLogPath";
    private static final String THREAD_COUNT = "threadCount";
    private static final String SERVER_SPEC = "serverSpec";

    private static final String[] CONFIG_KEYS = {MAIN_LOG_PATH, THREAD_COUNT, SERVER_SPEC};

    private static final String SERVER_NAME = "serverName";
    private static final String PORT_NUMBER = "portNumber";
    private static final String DOCUMENT_PATH = "documentPath";
    private static final String LOG_PATH = "logPath";
    private static final String INDEX_PAGE = "indexPage";
    private static final String ERROR_PAGE = "errorPage";

    private static final String[] SERVER_SPEC_KEYS = {SERVER_NAME, PORT_NUMBER, DOCUMENT_PATH, LOG_PATH, INDEX_PAGE, ERROR_PAGE};

    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_INDEX_PAGE = "errorIndexPage";

    private static final String[] ERROR_PAGE_KEYS = {ERROR_CODE, ERROR_INDEX_PAGE};

    private final int threadCount;
    private final String mainLogPath;
    private final Map<Integer, List<ServerSpec>> serverSpecFinder = new HashMap<>();

    public ServerConfiguration(String configFilePath) throws IOException, ClassCastException, ParseException, JSONKeyNotFoundException {
        FileReader fileReader = new FileReader(new File(configFilePath));

        JSONObject serverJSONObject = (JSONObject) new JSONParser().parse(fileReader);

        if (isNotContainServerConfigurationKey(serverJSONObject)) {
            throw new JSONKeyNotFoundException();
        }

        this.mainLogPath = (String) serverJSONObject.get(MAIN_LOG_PATH);
        this.threadCount = (int)Math.min(MAX_THREAD_COUNT, Math.max(MIN_THREAD_COUNT, (long) serverJSONObject.get(THREAD_COUNT)));

        JSONArray serverSpecJSONArray = (JSONArray) serverJSONObject.get(SERVER_SPEC);

        for (Object serverSpecObject : serverSpecJSONArray) {
            JSONObject serverSpecJSON = (JSONObject) serverSpecObject;

            if (isNotContainServerSpecKeys(serverSpecJSON)) {
                throw new JSONKeyNotFoundException();
            }

            ServerSpec serverSpec = parseServerSpec(serverSpecJSON);
            addServerSpec(serverSpec);
        }
    }

    private static boolean isNotContainsKeys(JSONObject jsonObject, String[] keys) {
        return !Arrays.stream(keys).allMatch(jsonObject::containsKey);
    }

    private static boolean isNotContainServerConfigurationKey(JSONObject jsonObject) {
        return isNotContainsKeys(jsonObject, CONFIG_KEYS);
    }

    private static boolean isNotContainServerSpecKeys(JSONObject jsonObject) {
        if (isNotContainsKeys(jsonObject, SERVER_SPEC_KEYS)) {
            return true;
        }

        JSONArray errorPageJSONArray = (JSONArray) jsonObject.get(ERROR_PAGE);
        for (Object errorPageObject : errorPageJSONArray) {
            JSONObject errorPageJSONObject = (JSONObject) errorPageObject;

            if (isNotContainsKeys(errorPageJSONObject, ERROR_PAGE_KEYS)) {
                return true;
            }
        }
        return false;
    }

    private ServerSpec parseServerSpec(JSONObject jsonObject) throws ClassCastException {
        ServerSpecBuilder serverSpecBuilder
                = new ServerSpecBuilder()
                .setServerName((String) jsonObject.get(SERVER_NAME))
                .setPortNumber((Long) jsonObject.get(PORT_NUMBER))
                .setDocumentPath((String) jsonObject.get(DOCUMENT_PATH))
                .setLogPath((String) jsonObject.get(LOG_PATH))
                .setIndexPage((String) jsonObject.get(INDEX_PAGE));

        JSONArray errorPageJSONArray = (JSONArray)jsonObject.get(ERROR_PAGE);
        for (Object errorPageObject : errorPageJSONArray) {
            JSONObject errorPageJSONObject = (JSONObject) errorPageObject;
            long errorPageCode = (long)errorPageJSONObject.get(ERROR_CODE);
            String errorIndexPage = (String) errorPageJSONObject.get(ERROR_INDEX_PAGE);
            serverSpecBuilder.addErrorPage(errorPageCode, errorIndexPage);
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

    public int getThreadCount() {
        return this.threadCount;
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
