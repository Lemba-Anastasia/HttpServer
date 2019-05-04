package Conroller;

import Model.Client;
import View.MainWindow;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.*;

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private BufferedOutputStream out;
    private static final Logger log = Logger.getLogger(ClientHandler.class);
    private MainWindow mainWindow;
    private HttpController httpController;
    private Client client;
    private static final String REGEX_FOR_REQUEST = "^[A-Z]{3,7} \\/[a-zA-Z\\.\\/\\_]* HTTP\\/1\\.(\\d+)$";
    private static final String ENDPOINT_FOR_REGISTRATION = "/reg";
    public static final String FOR_BY = "/forBY/";
    public static final String FOR_RU = "/forRU/";
    public static final String COMMON_PAGES = "CommonPages/";
    public static final String ERROR_PAGES = "ErrorPages/";
    public static final String PATH_TO_RESOURCES = "src/main/resources/";
    public static final String NOT_ACCEPTABLE_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "not_acceptable.html";
    public static final String NOT_FOUND_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "not_found.html";
    public static final String HTTP_VERSION_NOT_SUPPORTED_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "http_version_not_supported.html";
    public static final String INTERNAL_SERVER_ERROR_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "internal_server_error.html";
    public static final String BAD_REQUEST_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "bad_request.html";
    public static final String NOT_IMPLEMENTED_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "not_implemented.html";
    public static final String MOVED_PERMANENTLY_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "moved_permanently.html";
    public static final String MOVED_TEMPORARILY_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "moved_temporarily.html";
    public static final String UNAUTHORIZED_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "unauthorized.html";
    public static final String FORBIDDEN_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "forbidden.html";
    public static final String SERVICE_UNAVAILABLE_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "service_unavailable.html";
    public static final String REQUEST_TIMEOUT_PAGE = PATH_TO_RESOURCES + ERROR_PAGES + "request_timeout.html";
    private static final String PAGE_AFTER_REGISTRATION = PATH_TO_RESOURCES + COMMON_PAGES + "page_after_reg.html";
    private static String SCRIPTS = "scripts.js";
    static final String SCRIPTFORTEST408 = "scriptForTest408.js";
    private static final int WAIT_TIME = 10000;
    private WorkWithFile workWithFile;
    private Map<String, byte[]> mapForResponse;

    ClientHandler(Socket socket, MainWindow mainWindow) throws IOException {
        this.socket = socket;
        this.mainWindow = mainWindow;
        httpController = new HttpController();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedOutputStream(socket.getOutputStream());
        workWithFile = new WorkWithFile();
    }

    ClientHandler(Socket socket, MainWindow mainWindow, Client client) throws IOException {
        this.socket = socket;
        this.mainWindow = mainWindow;
        httpController = new HttpController();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedOutputStream(socket.getOutputStream());
        workWithFile = new WorkWithFile();
        this.client = client;
    }

    @Override
    public void run() {
        try {
            String request;
            while (!socket.isClosed()) {
                if ((request = in.readLine()) != null && request.matches(REGEX_FOR_REQUEST)) {
                    StringTokenizer parse = new StringTokenizer(request);
                    String command = parse.nextToken();
                    String endPoint = parse.nextToken();
                    String httpVersion = parse.nextToken();
                    mainWindow.deliverMessage("Request:\n"+request);
                    log.info("Request:\n"+request);
                    if (httpController.isSupportedHttpVersion(httpVersion)) {
                        Map<String, String> headers = getHeadersFromRequest(command);
                        handlingRequest(command, endPoint, headers);
                    } else
                        sentResponse(httpController.handlingHttpResponse(HTTP_VERSION_NOT_SUPPORTED_PAGE, HttpController.HTTP_VERSION_NOT_SUPPORTED));
                }
            }
        } catch (IOException e) {
            sentResponse(httpController.handlingHttpResponse(SERVICE_UNAVAILABLE_PAGE, HttpController.SERVICE_UNAVAILABLE));
            log.log(Level.ERROR, e);
        }
    }

    private void handlingRequest(String command, String endPoint, Map<String, String> headers) {
        switch (command) {
            case "GET":
                handlingRequestForGet(endPoint, headers);
                break;
            case "POST":
                //String bodyRequest2 ="{  \'name\' : \'Nastya\',  \'country\' : \'Belarus\'}";
                Type mapType = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String[]> map = new Gson().fromJson(headers.get("BODY"), mapType);
                handlingMessageForPost(endPoint, headers, map);
                break;
            case "HEAD":
                handlingMessageForHead(endPoint, headers);
                break;
            default:
                sentResponse(httpController.handlingHttpResponse(NOT_IMPLEMENTED_PAGE, HttpController.NOT_IMPLEMENTED));
        }
    }

    private Map<String, String> getHeadersFromRequest(String command) throws IOException {
        String header = "";
        String bodyRequest = "";
        String message = "";
        Map<String, String> headers = new HashMap<>();
        LineNumberReader lineNumberReader = new LineNumberReader(in);
        while (lineNumberReader.ready()) {
            header = in.readLine();
            log.info("HEADER: "+header+"\n");
            message += "\n" + header + "\n";
            if (header.matches("Host: (\\w+):(\\d+)")) {
                headers.put("Host", header.substring(6));
            } else if (header.matches("Authorization: (\\w+)")) {
                headers.put("Authorization", header.substring(15));
            } else if (header.startsWith("Accept: ")) {
                headers.put("Accept", header.substring(8));
            } else if (header.equals("")) {
                if (command.equals("POST"))
                    if (lineNumberReader.ready()) {
                        bodyRequest = in.readLine();
                        headers.put("BODY", bodyRequest);
                        message += "\n" + bodyRequest + "\n";
                    }
            }
        }
        String requestString = getAllHeadersForLog(headers);
        mainWindow.deliverMessage(bodyRequest);
        log.info(requestString);
        return headers;
    }

    private String getAllHeadersForLog(Map<String, String> headers) {
        StringJoiner joiner = new StringJoiner("\n", "\n","\n");
        for(String key : headers.keySet()){
            joiner.add(key+": "+headers.get(key));
        }
        return joiner.toString();
    }

    private void handlingRequestForGet(String endPoint, Map<String, String> headers) {
        if (client != null) {
            sentResponse(handlingRequestGetForExistedClient(headers, endPoint));
        } else if (isMovingResource(endPoint)) {
        } else if (workWithFile.isEndPointBelongToPublicPage(endPoint)) {
            sentResponse(handlingRequestGetForNotExistedClientForPublicPage(endPoint));
        } else if (isRequestedRecourceExisted(endPoint)) {
            sentResponse(httpController.handlingHttpResponse(NOT_FOUND_PAGE, HttpController.NOT_FOUND));
        } else {
            sentResponse(httpController.handlingHttpResponse(UNAUTHORIZED_PAGE, HttpController.UNAUTHORIZED));
        }
    }

    private boolean isMovingResource(String endPoint) {
        Map<String, String> infoAboutMovingFile = workWithFile.howThisFileWereMoving(endPoint);
        if (infoAboutMovingFile.get("statusMoving") != null) {
            if (infoAboutMovingFile.get("statusMoving").equals("movePerm")) {
                sentResponse(httpController.handlingHttpResponse(infoAboutMovingFile.get("newLocation"), HttpController.MOVED_PERMANENTLY));
            } else if (infoAboutMovingFile.get("statusMoving").equals("moveTemp"))
                sentResponse(httpController.handlingHttpResponse(infoAboutMovingFile.get("newLocation"), HttpController.MOVED_TEMPORARILY));
            return true;
        }
        return false;
    }

    private Map<String, byte[]> handlingRequestGetForNotExistedClientForPublicPage(String endPoint) {
        String localPage = workWithFile.getFileLocation(PATH_TO_RESOURCES, workWithFile.parseToGetFile(endPoint));
        return httpController.handlingHttpResponse(localPage, HttpController.OK);
    }

    private Map<String, byte[]> handlingRequestGetForExistedClient(Map<String, String> headers, String endPoint) {
        if (isAuthorizedClient(headers)) {
            return handlingRequestGetForExistedAuthorizedClient(headers, endPoint);
        } else
            return httpController.handlingHttpResponse(BAD_REQUEST_PAGE, HttpController.BAD_REQUEST);
    }

    private Map<String, byte[]> handlingRequestGetForExistedAuthorizedClient(Map<String, String> headers, String endPoint) {
        if (isRecourseAccepted(headers, endPoint)) {
            return handlingRequestGetForExistedClientAndAcceptedRecource(headers, endPoint);
        } else
            return httpController.handlingHttpResponse(FORBIDDEN_PAGE, HttpController.FORBIDDEN);
    }

    private Map<String, byte[]> handlingRequestGetForExistedClientAndAcceptedRecource(Map<String, String> headers, String endPoint) {
        if (isClientAccepted(headers)) {
            return httpController.handlingHttpResponse(NOT_ACCEPTABLE_PAGE, HttpController.NOT_ACCEPTABLE);
        } else if (isRequestedRecourceExisted(endPoint)) {
            return httpController.handlingHttpResponse(NOT_FOUND_PAGE, HttpController.NOT_FOUND);
        } else {
            return handlingRequestGetForExistedAcceptedClientWithExistedAcceptedResource(endPoint);
        }
    }

    private Map<String, byte[]> handlingRequestGetForExistedAcceptedClientWithExistedAcceptedResource(String endPoint) {
        String locationFile = workWithFile.getFileLocation(PATH_TO_RESOURCES, workWithFile.parseToGetFile(endPoint));
        Map<String, String> infoAboutMovingFile = workWithFile.howThisFileWereMoving(endPoint);
        if (infoAboutMovingFile.get("statusMoving") != null) {
            if (infoAboutMovingFile.get("statusMoving").equals("movePerm")) {
                return httpController.handlingHttpResponse(infoAboutMovingFile.get("newLocation"), HttpController.MOVED_PERMANENTLY);
            } else if (infoAboutMovingFile.get("statusMoving").equals("moveTemp"))
                return httpController.handlingHttpResponse(infoAboutMovingFile.get("newLocation"), HttpController.MOVED_TEMPORARILY);
        } else
            return httpController.handlingHttpResponse(locationFile, HttpController.OK);
        return null;
    }

    private boolean isRequestedRecourceExisted(String endPoint) {
        return !workWithFile.existFile(PATH_TO_RESOURCES, workWithFile.parseToGetFile(endPoint));
    }

    private boolean isClientAccepted(Map<String, String> headers) {
        return headers.get("Accept") == null;
    }

    private boolean isRecourseAccepted(Map<String, String> headers, String endPoint) {
        return isClientHaveAccessToResource(endPoint, headers.get("Authorization")) ||
                workWithFile.isEndPointBelongToPublicPage(endPoint);
    }

    private boolean isAuthorizedClient(Map<String, String> headers) {
        return headers.get("Authorization") != null;
    }

    private void handlingMessageForHead(String endPoint, Map<String, String> headers) {
        Map<String, byte[]> mapForResponse;
        if (client != null) {
            mapForResponse = handlingRequestGetForExistedClient(headers, endPoint);
        } else if (workWithFile.isEndPointBelongToPublicPage(endPoint)) {
            mapForResponse = handlingRequestGetForNotExistedClientForPublicPage(endPoint);
        } else if (isRequestedRecourceExisted(endPoint)) {
            mapForResponse = httpController.handlingHttpResponse(NOT_FOUND_PAGE, HttpController.NOT_FOUND);
        } else {
            mapForResponse = httpController.handlingHttpResponse(UNAUTHORIZED_PAGE, HttpController.UNAUTHORIZED);
        }
        mapForResponse.put("fileDate", "".getBytes());
        sentResponse(mapForResponse);
    }

    private boolean isClientHaveAccessToResource(String endPoint, String authorization) {
        return (endPoint.startsWith(FOR_BY) && authorization.equals("Belarus") && client.getCountry().equals("Belarus"))
                || endPoint.startsWith(FOR_RU) && authorization.equals("Russia") && client.getCountry().equals("Russia");
    }

    private void handlingMessageForPost(String endPoint, Map<String, String> headers, Map body) {
        if (endPoint.equals(ENDPOINT_FOR_REGISTRATION)) {
            try {
                final List<Object> result = new ArrayList<>();
                TimeoutBlock timeoutBlock = new TimeoutBlock(WAIT_TIME);
                Runnable runnable = () -> {
                    try {
                        ScriptEngineManager manager = new ScriptEngineManager();
                        ScriptEngine engine = manager.getEngineByName("JavaScript");
                        Reader scriptReader = new InputStreamReader(new FileInputStream(PATH_TO_RESOURCES + SCRIPTS));
                        try {
                            engine.eval(scriptReader);
                        } finally {
                            scriptReader.close();
                        }
                        Invocable invEngine = (Invocable) engine;
                        result.add(invEngine.invokeFunction("checkCountry", body.get("country")));
                    } catch (ScriptException | NoSuchMethodException | IOException e) {
                        sentResponse(httpController.handlingHttpResponse(INTERNAL_SERVER_ERROR_PAGE, HttpController.INTERNAL_SERVER_ERROR));
                        log.log(Level.ERROR, e);
                    }
                };
                timeoutBlock.addBlock(runnable);
                if ((boolean) result.get(0)) {
                    log.info("registration client" + body.get("name"));
                    client = new Client((String) (body.get("name")), (String) body.get("country"), socket);
                    sentResponse(httpController.handlingHttpResponse(PAGE_AFTER_REGISTRATION, HttpController.CREATED));
                } else {
                    sentResponse(httpController.handlingHttpResponse(BAD_REQUEST_PAGE, HttpController.BAD_REQUEST));
                }
            } catch (Throwable e) {
                sentResponse(httpController.handlingHttpResponse(REQUEST_TIMEOUT_PAGE, HttpController.REQUEST_TIMEOUT));
                log.error(e.getMessage());
            }
        }
    }

    private void sentResponse(Map<String, byte[]> mapForResponse) {
        try {
            this.mapForResponse = mapForResponse;
            out.write(mapForResponse.get("responseHeaders"));
            out.write("\r\n".getBytes());
            out.write(mapForResponse.get("fileDate"));
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            sentResponse(httpController.handlingHttpResponse(SERVICE_UNAVAILABLE_PAGE, HttpController.SERVICE_UNAVAILABLE));
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mapForResponse.get("responseHeaders"));
        int c;
        StringBuilder linesForMainWindows = new StringBuilder("");
        while ((c = byteArrayInputStream.read()) != -1) {
            linesForMainWindows.append((char) c);
        }
        mainWindow.deliverMessage("\nResponse: "+linesForMainWindows.toString()+"\n");
        log.info("\nResponse: "+linesForMainWindows.toString()+"\n");
    }

    public Map<String, byte[]> getMapForResponse() {
        return mapForResponse;
    }

    public void setPathScript(String pathNewScript) {
        SCRIPTS = pathNewScript;
    }

}