package Conroller;

import Model.Client;
import View.MainWindow;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientHandlerTest extends TestCase {

    public void testRunToCheckOkStatusCodeWhenClientNotAuthorizedAndGettingAPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain, " +
                "Accept-Encoding: gzip, deflate, br\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow);
        clientHandler.run();
        String exceptedResult = "\nHTTP/1.1 " + "200 OK\r\n" +
                "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                "Server: HTTPServer by Ruslan and Lemba \r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckOkStatusCodeWhenClientRegistrationWithPost() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "POST /reg HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\r\n" +
                "\n" +
                "{\"name\" : \"Nastya\", \"country\" : \"Belarus\"}" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Nastya", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult = "\nHTTP/1.1 201 CREATED\r\n" +
                "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                "Server: HTTPServer by Ruslan and Lemba \r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckOkStatusCodeWhenClientAuthorizedAndGettingANotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /forBY/bel_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 200 OK\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckMovedPermStatusCodeWhenClientAuthorizedAndGettingANotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /page_after_reg.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 301 MOVED PERMANENTLY\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n"+
                        "Location: /CommonPages/page_after_reg.html\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckMovedPermStatusCodeWhenClientNotAuthorizedAndGettingANotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /page_after_reg.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 301 MOVED PERMANENTLY\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n"+
                        "Location: /CommonPages/page_after_reg.html\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckMovedTermStatusCodeWhenClientAuthorizedAndGettingANotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /resources/hello.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 302 MOVED TEMPORARILY\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n"+
                        "Location: /CommonPages/hello.html\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckBadRStatusCodeWhenClientAuthorizedAndCommandIsGetAndForNotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /forBY/bel_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 400 Bad Request\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckUnauthorizedStatusCodeWhenClientNotAuthorizedAndCommandIsGetAndForNotPublicEndPointAndNotExistedPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /forBY/bel_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 401 UNAUTHORIZED\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n"+
                        "WWW-Authorized: registration please\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckOkStatusCodeWhenClientAuthorizedAndGettingANotAccessiblePage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /forRU/rus_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult = "\nHTTP/1.1 403 FORBIDDEN\r\n" +
                "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                "Server: HTTPServer by Ruslan and Lemba \r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: (\\d+)\r\n" +
                "WWW-Authorized: registration please\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckNotFoundStatus() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /dont_exist.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain, " +
                "Accept-Encoding: gzip, deflate, br\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow);
        clientHandler.run();
        String expectedResult =
                "\nHTTP/1.1 " + "404 NOT FOUND\r\n" +
                "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                "Server: HTTPServer by Ruslan and Lemba \r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(expectedResult));
    }

    public void testRunToCheckNotAcceptStatusCodeWhenClientAuthorizedAndCommandIsGetAndForNotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /forBY/bel_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 406 NOT ACCEPTABLE\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckTimeOutStatusCodeWhenClietRegistrationWithPost() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "POST /reg HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain, \n" +
                "Accept-Encoding: gzip, deflate, br\r\n" +
                "\n" +
                "{\"name\" : \"Nastya\", \"country\" : \"Belarus\"}" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Nastya", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.setPathScript(ClientHandler.SCRIPTFORTEST408);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 408 REQUEST TIMEOUT\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckNotImplementedStatusCodeWhenCommandIsOptions() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "OPTIONS /forBY/bel_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 501 NOT IMPLEMENTED\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckHTTPVersionNotSupportedStatusCode() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "GET /forBY/bel_main_page.html HTTP/1.0\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 505 HTTP VERSION NOT SUPPORTED\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertNotNull(clientHandler.getMapForResponse().get("fileDate"));
    }

    public void testRunToCheckOkStatusCodeWhenClientNotAuthorizedAndTypeOfRequestIsHeadForAPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "HEAD /main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow);
        clientHandler.run();
        String exceptedResult = "\nHTTP/1.1 " + "200 OK\r\n" +
                "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                "Server: HTTPServer by Ruslan and Lemba \r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: (\\d+)\r\n";
        System.out.println(exceptedResult);
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertEquals(new String(clientHandler.getMapForResponse().get("fileDate"), StandardCharsets.UTF_8), "");
    }

    public void testRunToCheckMovedPermStatusCodeWhenClientAuthorizedAndCommandHeadNotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "HEAD /page_after_reg.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 301 MOVED PERMANENTLY\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n"+
                        "Location: /CommonPages/page_after_reg.html\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertEquals(new String(clientHandler.getMapForResponse().get("fileDate"), StandardCharsets.UTF_8), "");
    }

    public void testRunToCheckMovedTermStatusCodeWhenClientAuthorizedAndCommandHeadANotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "HEAD /resources/hello.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Authorization: Belarus\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 302 MOVED TEMPORARILY\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n"+
                        "Location: /CommonPages/hello.html\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertEquals(new String(clientHandler.getMapForResponse().get("fileDate"), StandardCharsets.UTF_8), "");
    }

    public void testRunToCheckBadRStatusCodeWhenClientAuthorizedAndCommandIsHeadAndForNotPublicPage() throws IOException {
        Socket socket = mock(Socket.class);
        MainWindow mainWindow = mock(MainWindow.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                "HEAD /forBY/bel_main_page.html HTTP/1.1\n" +
                "Host: localhost:51265\n" +
                "Connection: keep-alive\n" +
                "Accept: application/json, text/plain\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                ""));
        when((socket.getOutputStream())).thenReturn(mock(OutputStream.class));
        when(socket.isClosed()).thenReturn(false).thenReturn(true);
        Client client = new Client("Alex", "Belarus", socket);
        ClientHandler clientHandler = new ClientHandler(socket, mainWindow, client);
        clientHandler.run();
        String exceptedResult =
                "\nHTTP/1.1 400 Bad Request\r\n" +
                        "Date: (\\w+) (\\w+) (\\d+) (\\d+):(\\d+):(\\d+) (\\w+) (\\d+)\r\n" +
                        "Server: HTTPServer by Ruslan and Lemba \r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: (\\d+)\r\n";
        assertTrue(new String(clientHandler.getMapForResponse().get("responseHeaders"), StandardCharsets.UTF_8).matches(exceptedResult));
        assertEquals(new String(clientHandler.getMapForResponse().get("fileDate"), StandardCharsets.UTF_8), "");
    }

}