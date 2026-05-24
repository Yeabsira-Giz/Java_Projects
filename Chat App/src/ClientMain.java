import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) throws IOException {
        String username =
                javax.swing.JOptionPane.showInputDialog(
                        "Enter username"
                );
        
        if (username == null || username.trim().isEmpty()) {
            System.exit(0);
        }

        Main main = new Main();
        Client client = new Client(main, username.trim());

        main.setClient(client);

        new Thread(() -> {
            client.startClient();
        }).start();
    }
}
