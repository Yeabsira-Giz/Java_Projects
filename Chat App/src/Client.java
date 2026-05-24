
import java.io.*;
import java.net.Socket;
import javax.swing.*;
public class Client {
        Socket socket;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        Main main;
        String userName;
        JLabel Message;
        JLabel fileLabel;
        JLabel fileMessage;

    public  Client(Main main, String userName) throws IOException {
            this.main = main;
            this.userName = userName;
        }
        public void startClient() {
            try {
                socket = new Socket("localhost", 5000);

                dataInputStream = new DataInputStream(socket.getInputStream());

                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataOutputStream.writeUTF(userName);
                dataOutputStream.flush();
                
            readMessage();

            }catch (Exception e){
                System.out.println("Error in creating a connection");
            }
        }
    public synchronized void sendMessage( String msg, String targetUser) {

        try {

            dataOutputStream.writeUTF("TEXT");
            dataOutputStream.writeUTF(targetUser);


            dataOutputStream.writeUTF(msg);

            dataOutputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        public synchronized void sendFile(File file, String targetUser){
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] fileBytes= new byte[(int) file.length()];

                fis.read(fileBytes);

                dataOutputStream.writeUTF("FILE");

                dataOutputStream.writeUTF(targetUser);


                dataOutputStream.writeUTF(file.getName());

                dataOutputStream.writeInt(fileBytes.length);

                dataOutputStream.write(fileBytes);

                dataOutputStream.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    public void readMessage() {

        new Thread(() -> {

            try {

                while (true) {

                    String type =
                            dataInputStream.readUTF();

                    if (type.equals("TEXT")) {

                        String message =
                                dataInputStream.readUTF();

                        SwingUtilities.invokeLater(() -> {
                            main.addReceivedMessage(message);
                        });

                    } else if (type.equals("HISTORY_MSG")) {
                        
                        String message = dataInputStream.readUTF();
                        
                        SwingUtilities.invokeLater(() -> {
                            main.addReceivedMessage(message);
                        });
                        
                    }

                    else if (type.equals("FILE")) {

                        String senderName =
                                dataInputStream.readUTF();

                        String fileName =
                                dataInputStream.readUTF();

                        int size =
                                dataInputStream.readInt();

                        byte[] fileBytes =
                                new byte[size];

                        dataInputStream.readFully(fileBytes);

                        File receivedFile =
                                new File("received_" + fileName);

                        try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                            fos.write(fileBytes);
                        }

                        SwingUtilities.invokeLater(() -> {
                            main.addReceivedFile(senderName + " sent: " + fileName, receivedFile);
                        });

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(main.main, "Connection to server lost.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                });
            }

        }).start();
    }


    public synchronized void requestHistory(String targetUser) {
        try {
            dataOutputStream.writeUTF("HISTORY");
            dataOutputStream.writeUTF(targetUser);
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }



