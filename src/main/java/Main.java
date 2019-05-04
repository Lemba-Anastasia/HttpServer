
import Conroller.HTTPServer;
import View.MainWindow;

public class Main {
    public static void main(String[] args) {
        MainWindow mainWindow = new MainWindow();
        new Thread(new HTTPServer(mainWindow)).start();
    }
}
