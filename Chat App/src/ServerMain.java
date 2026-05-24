import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {


        Server server = new Server();

        new Thread(() -> {
            server.startServer();
        }).start();

    }
}