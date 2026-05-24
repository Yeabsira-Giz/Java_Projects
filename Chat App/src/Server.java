import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    Socket socket;
    ServerSocket serverSocket;
    static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    DatabaseHelper db;

    public void startServer() {
        try {
            db = new DatabaseHelper();
            db.connect();
            db.createTables();

            serverSocket = new ServerSocket(5000);
            System.out.println("Server started on port 5000. Listening for clients...");

            while (true) {
                socket = serverSocket.accept();
                try {
                    ClientHandler client = new ClientHandler(socket, db);
                    clients.add(client);

                    Thread thread = new Thread(client);
                    thread.start();
                } catch (Exception e) {
                    System.out.println("Failed to initialize connection for client: " + e.getMessage());
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Critical server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
