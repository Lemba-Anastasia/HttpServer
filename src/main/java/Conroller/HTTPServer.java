package Conroller;

import View.MainWindow;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HTTPServer implements Runnable {
    private static final Logger log = Logger.getLogger(HTTPServer.class);
    private MainWindow mainWindow;
    private List<ClientHandler> threadClientHandlerList;
    private List<Thread> threadList;
    private ServerSocket server;
    private Socket socket;

    public HTTPServer(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        threadClientHandlerList = new ArrayList<>();
        threadList = new ArrayList<>();
        try {
            server = new ServerSocket(60000);
            int port=server.getLocalPort();
            String message="Server started\nPort: " + port + "\n...\n";
            log.info(message);
            mainWindow.deliverMessage(message);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void run() {
        log.info("Waiting for client connection");
        while (!server.isClosed()) {
            try {
                socket = server.accept();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            ClientHandler clientHandler = null;
            try {
                clientHandler = new ClientHandler(socket, mainWindow);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            threadClientHandlerList.add(clientHandler);
            Thread thread = new Thread(clientHandler);
            thread.start();
            threadList.add(thread);
        }
    }
}