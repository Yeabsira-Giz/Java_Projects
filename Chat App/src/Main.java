import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Main {

    JFrame main;
    JButton sendButton;
    JPanel chatPanel;
    JPanel messageRow;
    JPanel bubble;
    JLabel time;
    JLabel message;
    JLabel fileMessage;
    JTextField messageFieldTextField;
    JTextField recipientField;
    JButton chooseFileButton;
    JButton loadHistoryButton;
    JScrollPane chatBox;
    JPanel bottom;

    Client client;

    public Main() throws IOException {


            main = new JFrame();
            main.setLayout(new BorderLayout());



            
            chatPanel = new JPanel();
            chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
            chatPanel.setBackground(new Color(240,240,240));
            chatBox = new JScrollPane(chatPanel);
            recipientField = new JTextField();
            recipientField.setPreferredSize(new Dimension(100, 30));

            main.add(chatBox, BorderLayout.CENTER);

            messageFieldTextField = new JTextField();
            sendButton = new JButton("Send");

            chooseFileButton = new JButton("File");
            loadHistoryButton = new JButton("History");

            // 3. BOTTOM PANEL
            bottom = new JPanel(new BorderLayout());

            JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightButtons.add(loadHistoryButton);
            rightButtons.add(sendButton);

            bottom.add(messageFieldTextField, BorderLayout.CENTER);
            bottom.add(rightButtons, BorderLayout.EAST);
            bottom.add(chooseFileButton, BorderLayout.WEST);

            JPanel recipientPanel = new JPanel(new BorderLayout());
            JLabel recipientLabel = new JLabel(" Recipient: ");
            recipientPanel.add(recipientLabel, BorderLayout.WEST);
            recipientPanel.add(recipientField, BorderLayout.CENTER);
            bottom.add(recipientPanel, BorderLayout.NORTH);

            sendButton.setBackground(new Color(0,132,255));

            sendButton.setForeground(Color.WHITE);

            sendButton.setFocusPainted(false);

            chooseFileButton.setBackground(new Color(230,230,230));

            main.add(bottom, BorderLayout.SOUTH);

            main.setSize(500, 500);
            main.setTitle("Chat App");
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main.setVisible(true);




        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendAction();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });



        messageFieldTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()== KeyEvent.VK_ENTER){
                    try {
                        sendAction();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               chooseAndSendFile();

            }
        });
        
        loadHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String targetUser = recipientField.getText().trim();
                if (targetUser.isEmpty()) {
                    JOptionPane.showMessageDialog(main, "Please specify a recipient username.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(client != null) {
                    client.requestHistory(targetUser);
                }
            }
        });
    }


    public void setClient(Client client) {
        this.client = client;
    }

   public void sendAction() throws IOException {
        String msg = messageFieldTextField.getText().trim();
        String targetUser = recipientField.getText().trim();
        if (targetUser.isEmpty()) {
            JOptionPane.showMessageDialog(main, "Please specify a recipient username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (msg.isEmpty()) {
            return;
        }
        messageRow = new JPanel(new BorderLayout());
        messageRow.setBorder(
               BorderFactory.createEmptyBorder(5,5,5,5)
        );


        bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));

        time = new JLabel( LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

       message = new JLabel(
               "You: " + msg);
       bubble.add(message);
       bubble.add(time);
       JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));

       wrapper.setOpaque(false);

       wrapper.add(bubble);

       messageRow.add(wrapper, BorderLayout.EAST);
       message.setAlignmentX(Component.RIGHT_ALIGNMENT);

       message.setForeground(Color.WHITE);

       time.setForeground(Color.WHITE);
       time.setFont(new Font("Arial", Font.PLAIN, 10));
       messageRow.setOpaque(false);

       time.setAlignmentX(Component.RIGHT_ALIGNMENT);
       bubble.setBackground(new Color(0,132,255));

       bubble.setOpaque(true);

       bubble.setBorder(
               BorderFactory.createEmptyBorder(8,12,8,12)
       );

       chatPanel.add(messageRow);
       chatPanel.revalidate();
       chatPanel.repaint();
       chatBox.getVerticalScrollBar().setValue(
               chatBox.getVerticalScrollBar().getMaximum()
       );


        if(client!=null){
            client.sendMessage(msg, targetUser);
        }
        messageFieldTextField.setText("");
    }
    public void chooseAndSendFile(){
        String targetUser = recipientField.getText().trim();
        if (targetUser.isEmpty()) {
            JOptionPane.showMessageDialog(main, "Please specify a recipient username before sending a file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser= new JFileChooser();

        int result = fileChooser.showOpenDialog(null);
        if (result== JFileChooser.APPROVE_OPTION){
            messageRow = new JPanel(new BorderLayout());
            bubble = new JPanel();
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));

            time = new JLabel( LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

            File file = fileChooser.getSelectedFile();
            fileMessage=new JLabel("You sent file: " + file.getName());

            fileMessage.setForeground( Color.BLUE);
            fileMessage.setCursor(new Cursor(Cursor.HAND_CURSOR));

            fileMessage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            


            bubble.add(fileMessage);
            bubble.add(time);
            messageRow.add(bubble, BorderLayout.EAST);
            chatPanel.add(messageRow);
            chatPanel.revalidate();
            chatPanel.repaint();
            chatBox.getVerticalScrollBar().setValue(
                    chatBox.getVerticalScrollBar().getMaximum()
            );

            if(client != null){
                client.sendFile(file, targetUser);
            }
        }

    }

    public void addReceivedMessage(String text) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        row.setOpaque(false);

        JPanel bbl = new JPanel();
        bbl.setLayout(new BoxLayout(bbl, BoxLayout.Y_AXIS));
        bbl.setBackground(new Color(230, 230, 230));
        bbl.setOpaque(true);
        bbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel msgLabel = new JLabel(text);
        msgLabel.setForeground(Color.BLACK);

        JLabel ts = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        ts.setFont(new Font("Arial", Font.PLAIN, 10));
        ts.setForeground(Color.GRAY);

        bbl.add(msgLabel);
        bbl.add(ts);

        row.add(bbl, BorderLayout.WEST);

        chatPanel.add(row);
        chatPanel.revalidate();
        chatPanel.repaint();
        chatBox.getVerticalScrollBar().setValue(
                chatBox.getVerticalScrollBar().getMaximum()
        );
    }

    public void addReceivedFile(String fileName, File receivedFile) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        row.setOpaque(false);

        JPanel bbl = new JPanel();
        bbl.setLayout(new BoxLayout(bbl, BoxLayout.Y_AXIS));
        bbl.setBackground(new Color(230, 230, 230));
        bbl.setOpaque(true);
        bbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel fLabel = new JLabel(fileName);
        fLabel.setForeground(Color.BLUE);
        fLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().open(receivedFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JLabel ts = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        ts.setFont(new Font("Arial", Font.PLAIN, 10));
        ts.setForeground(Color.GRAY);

        bbl.add(fLabel);
        bbl.add(ts);

        row.add(bbl, BorderLayout.WEST);

        chatPanel.add(row);
        chatPanel.revalidate();
        chatPanel.repaint();
        chatBox.getVerticalScrollBar().setValue(
                chatBox.getVerticalScrollBar().getMaximum()
        );
    }
}