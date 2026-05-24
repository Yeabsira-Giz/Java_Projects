import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/Chatapp?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // XAMPP default: no password

    private Connection connection;


    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Connected to database successfully!");
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void createTables() {
        if (connection == null) {
            System.err.println("Cannot create tables: Database connection is not initialized.");
            return;
        }
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  username VARCHAR(50) UNIQUE NOT NULL," +
                "  password VARCHAR(255) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Messages table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS messages (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  sender VARCHAR(50) NOT NULL," +
                "  receiver VARCHAR(50) NOT NULL," +
                "  message TEXT NOT NULL," +
                "  type VARCHAR(10) DEFAULT 'TEXT'," +
                "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (sender) REFERENCES users(username)," +
                "  FOREIGN KEY (receiver) REFERENCES users(username)" +
                ")"
            );

            System.out.println("Tables created successfully!");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public boolean registerUser(String username, String password) {
        if (connection == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO users (username, password) VALUES (?, ?)"
        )) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        if (connection == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT id FROM users WHERE username = ? AND password = ?"
        )) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

  
    public boolean userExists(String username) {
        if (connection == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT id FROM users WHERE username = ?"
        )) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveMessage(String sender, String receiver, String message, String messageType) {
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO messages (sender, receiver, message, type) VALUES (?, ?, ?, ?)"
        )) {
            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, message);
            ps.setString(4, messageType);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving message: " + e.getMessage());
            e.printStackTrace();
        }
    }

  
    public List<String> getChatHistory(String user1, String user2, int limit) {
        List<String> history = new ArrayList<>();
        if (connection == null) return history;
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT sender, message, type, timestamp FROM messages " +
            "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
            "ORDER BY timestamp ASC LIMIT ?"
        )) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, user2);
            ps.setString(4, user1);
            ps.setInt(5, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sender = rs.getString("sender");
                    String msg = rs.getString("message");
                    Timestamp time = rs.getTimestamp("timestamp");
                    history.add(sender + ": " + msg + " [" + time + "]");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public List<String> getMessagesForUser(String username, int limit) {
        List<String> messages = new ArrayList<>();
        if (connection == null) return messages;
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT sender, message, type, timestamp FROM messages " +
            "WHERE receiver = ? ORDER BY timestamp DESC LIMIT ?"
        )) {
            ps.setString(1, username);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sender = rs.getString("sender");
                    String msg = rs.getString("message");
                    Timestamp time = rs.getTimestamp("timestamp");
                    messages.add(sender + ": " + msg + " [" + time + "]");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }


    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
