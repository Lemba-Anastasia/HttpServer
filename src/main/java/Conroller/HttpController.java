package Conroller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpController {
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int MOVED_PERMANENTLY = 301;// запрошенный документ был окончательно перенесен на новый URI, указанный в поле Location заголовка
    public static final int MOVED_TEMPORARILY = 302;// запрошенный документ временно доступен по другому URI, указанному в заголовке в поле Location
    public static final int BAD_REQUEST = 400;// синтаксическая ошибка в запросе сервера нет обработчика сооьветсвующего URL
    public static final int UNAUTHORIZED = 401;// для доступа к запрашиваему ресурсу требуется аутентификация
    public static final int FORBIDDEN = 403;//сервер понял запрос, но он отказывется его выполнять из-за ограниченности доступа пользователя к ресурсу
    public static final int NOT_FOUND = 404;
    public static final int NOT_ACCEPTABLE = 406;//запрошенный URI не может удовлетворить переданным в заголовке характеристикам. Если метод был не HEAD, то сервер должен вернуть список допустимых характеристик для данного ресурса
    public static final int REQUEST_TIMEOUT = 408;// время ожидания сервером передачи от клиента истекло; используется для метода POST
    public static final int INTERNAL_SERVER_ERROR = 500;//любая внутренняя ошибка сервера, которая не входит в рамки остальных ошибок класса ошибка при запуске скрипта
    public static final int NOT_IMPLEMENTED = 501;//сервер не поддерживает возможностей, необходимых для обработки запроса
    public static final int SERVICE_UNAVAILABLE = 503;//сервер временно не имеет возможности обрабатывать запросы по техническим причинам
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;//сервер не поддерживает или отказывается поддерживать указанную в запросе версию протокола HTTP.
    private WorkWithFile workWithFile;

    public static String getInformationAbout(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 201:
                return "CREATED";
            case 301:
                return "MOVED PERMANENTLY";
            case 302:
                return "MOVED TEMPORARILY";
            case 400:
                return "Bad Request";
            case 401:
                return "UNAUTHORIZED";
            case 403:
                return "FORBIDDEN";
            case 404:
                return "NOT FOUND";
            case 406:
                return "NOT ACCEPTABLE";
            case 408:
                return "REQUEST TIMEOUT";
            case 500:
                return "INTERNAL SERVER ERROR";
            case 501:
                return "NOT IMPLEMENTED";
            case 503:
                return "SERVICE UNAVAILABLE";
            case 505:
                return "HTTP VERSION NOT SUPPORTED";
        }
        return "Unknown Response Code";
    }

    HttpController() {
        workWithFile = new WorkWithFile();
    }

    public Map<String, byte[]> handlingHttpResponse(String page, int error) {
        String contentType = getContentType(page);
        Map<String, byte[]> response;
        switch (error) {
            case UNAUTHORIZED:
                response = createResponseForUnauthorizedStatusCode(contentType);
                break;
            case FORBIDDEN:
                response = createResponseForForbiddenStatusCode(contentType);
                break;
            case MOVED_TEMPORARILY:
                response = createResponseForMovedTempStatusCode(contentType, page);
                break;
            case MOVED_PERMANENTLY:
                response = createResponseForMovedPermStatusCode(contentType, page);
                break;
            default:
                response = createResponse(contentType, page, error);
        }
        return response;
    }

    private Map<String, byte[]> createResponse(String contentType, String page, int error) {
        Map<String, byte[]> response = new HashMap<>();
        byte[] fileDate = workWithFile.readFromBinaryFile(page);
        String responseHeaders = answerHeaders(fileDate, contentType, error, HttpController.getInformationAbout(error));
        response.put("responseHeaders", responseHeaders.getBytes());
        response.put("fileDate", fileDate);
        return response;
    }

    private Map<String, byte[]> createResponseForMovedPermStatusCode(String contentType, String page) {
        Map<String, byte[]> response = new HashMap<>();
        byte[] fileDate = workWithFile.readFromBinaryFile(ClientHandler.MOVED_PERMANENTLY_PAGE);
        String responseHeaders = answerHeaders(fileDate, contentType, MOVED_PERMANENTLY, HttpController.getInformationAbout(MOVED_PERMANENTLY)) +
                "Location: " + page+"\r\n";
        response.put("responseHeaders", responseHeaders.getBytes());
        response.put("fileDate", fileDate);
        return response;
    }

    private Map<String, byte[]> createResponseForMovedTempStatusCode(String contentType, String page) {
        Map<String, byte[]> response = new HashMap<>();
        byte[] fileDate = workWithFile.readFromBinaryFile(ClientHandler.MOVED_TEMPORARILY_PAGE);
        String responseHeaders = answerHeaders(fileDate, contentType, MOVED_TEMPORARILY, HttpController.getInformationAbout(MOVED_TEMPORARILY)) +
                "Location: " + page+"\r\n";
        response.put("responseHeaders", responseHeaders.getBytes());
        response.put("fileDate", fileDate);
        return response;
    }

    private Map<String, byte[]> createResponseForForbiddenStatusCode(String contentType) {
        Map<String, byte[]> response = new HashMap<>();
        byte[] fileDate = workWithFile.readFromBinaryFile(ClientHandler.FORBIDDEN_PAGE);
        String responseHeaders = answerHeaders(fileDate, contentType, FORBIDDEN, HttpController.getInformationAbout(FORBIDDEN)) +
                "WWW-Authorized: registration please\r\n";
        response.put("responseHeaders", responseHeaders.getBytes());
        response.put("fileDate", fileDate);
        return response;
    }

    private Map<String, byte[]> createResponseForUnauthorizedStatusCode( String contentType) {
        Map<String, byte[]> response = new HashMap<>();
        byte[] fileDate = workWithFile.readFromBinaryFile(ClientHandler.UNAUTHORIZED_PAGE);
        String responseHeaders = answerHeaders(fileDate, contentType, UNAUTHORIZED, HttpController.getInformationAbout(UNAUTHORIZED)) +
                "WWW-Authorized: registration please\r\n";
        response.put("responseHeaders", responseHeaders.getBytes());
        response.put("fileDate", fileDate);
      return response;
    }

    private String getContentType(String endPoint) {
        if (endPoint.endsWith(".html") || endPoint.equals("reg")) {
            return "text/html";
        } else if (endPoint.endsWith(".png")) {
            return "image/png";
        } else
            return "text/plain, image/png";
    }

    public boolean isSupportedHttpVersion(String httpVersion) {
        return httpVersion.endsWith("1.1");
    }

    private String answerHeaders(byte[] fileDate,
                                 String contentType,
                                 int responseCode,
                                 String infoAboutCode) {
        String response = "\nHTTP/1.1 " + responseCode + " "
                + infoAboutCode + "\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Server: HTTPServer by Ruslan and Lemba \r\n" +
                "Content-Type: " + contentType + "\r\n";
        response += "Content-Length: " + fileDate.length + "\r\n";
        return response;
    }
}