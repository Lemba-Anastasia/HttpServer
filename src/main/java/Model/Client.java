package Model;

import java.net.Socket;

public class Client {
    private String name;
    private Socket socket;
    private String country;
    private long clientId;
    private String accessRights;//TODO подумать как будут представляться права доступа

    public Client(String name, String country, Socket socket) {
        this.name = name;
        this.country = country;
        this.socket = socket;
        clientId = IdCounter.generateId();
    }

    public long getClientId() {
        return clientId;
    }


    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {//ну может пригодиться, а может и нет
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;

        if (name != client.getName()) return false;
        return socket == client.getSocket();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                "country='" + country + '\'' +
                ", socket=" + socket +
                ", clientId=" + clientId +
                '}';
    }

    public Object getCountry() {
        return country;
    }
}
