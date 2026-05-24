import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    Socket socket;
    String username;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    DatabaseHelper db;

    public ClientHandler(Socket socket, DatabaseHelper db) throws IOException {
        this.socket = socket;
        this.db = db;

        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        username = dataInputStream.readUTF();
        
        if (!db.userExists(username)) {
            db.registerUser(username, "default_password");
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                String type =
                        dataInputStream.readUTF();

                if (type.equals("TEXT")) {

                    String targetUser =
                            dataInputStream.readUTF();

                    String message =
                            dataInputStream.readUTF();
                            
                    if (!db.userExists(targetUser)) {
                        db.registerUser(targetUser, "default_password");
                    }
                    db.saveMessage(username, targetUser, message, "TEXT");

                    broadcastMessage(message, targetUser);

                } else if (type.equals("FILE")) {

                    String targetUser =
                            dataInputStream.readUTF();

                    String fileName =
                            dataInputStream.readUTF();

                    int size =
                            dataInputStream.readInt();

                    byte[] fileBytes =
                            new byte[size];

                    dataInputStream.readFully(fileBytes);

                    final File receivedFile =
                            new File("received_" + fileName);

                    try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                        fos.write(fileBytes);
                    }
                    
                    if (!db.userExists(targetUser)) {
                        db.registerUser(targetUser, "default_password");
                    }
                    db.saveMessage(username, targetUser, fileName, "FILE");

                    broadcastFile(fileName, receivedFile, targetUser);
                    
                } else if (type.equals("HISTORY")) {
                    String targetUser = dataInputStream.readUTF();
                    
                    java.util.List<String> history = db.getChatHistory(username, targetUser, 50);
                    for(String histMsg : history) {
                        sendHistoryMsg(histMsg);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Client " + (username != null ? username : "unknown") + " disconnected.");
        } finally {
            Server.removeClient(this);
            closeResources();
        }
    }

    public synchronized void sendText(String type, String message) throws IOException {
        dataOutputStream.writeUTF(type);
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
    }

    public synchronized void sendFile(String sender, String fileName, byte[] fileBytes) throws IOException {
        dataOutputStream.writeUTF("FILE");
        dataOutputStream.writeUTF(sender);
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.writeInt(fileBytes.length);
        dataOutputStream.write(fileBytes);
        dataOutputStream.flush();
    }

    public synchronized void sendHistoryMsg(String msg) throws IOException {
        dataOutputStream.writeUTF("HISTORY_MSG");
        dataOutputStream.writeUTF(msg);
        dataOutputStream.flush();
    }

    public void broadcastMessage(String msg, String targetUser) {
        for (ClientHandler client : Server.clients) {
            if (client.username.equals(targetUser)) {
                try {
                    client.sendText("TEXT", username + ": " + msg);
                } catch (IOException e) {
                    System.out.println("Failed to send text to " + targetUser);
                }
            }
        }
    }

    public void broadcastFile(String originalFileName, File file, String targetUser) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);

            for (ClientHandler client : Server.clients) {
                if (client.username.equals(targetUser)) {
                    try {
                        client.sendFile(username, originalFileName, fileBytes);
                    } catch (IOException e) {
                        System.out.println("Failed to send file to " + targetUser);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeResources() {
        try {
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
