package org.aproject;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.Console;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Dotenv dotenv = Dotenv.configure().load();
    private static final String URL = dotenv.get("DATABASE_URL");
    private static final String USER = dotenv.get("DATABASE_USER");
    private static final String PASSWORD = dotenv.get("DATABASE_PASSWORD");
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    public static Connection getConnection() {
        Connection connection = null;

        Console console = System.console();
        if (console == null) {
            System.out.println("Console unavailable. Use Scanner for reading.");
            return null;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            assert URL != null;
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Connection established successfully.");

        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to access driver.", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to access the database.", e);
            throw new RuntimeException(e);
        }

        return connection;
    }

    public static void createUsersTableIfNotExists(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(255) NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "email VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            logger.info("Table 'users' checked and created if necessary.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating users table.", e);
        }
    }

    public static void createTasksTableIfNotExists(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT NOT NULL,"
                + "name VARCHAR(255) NOT NULL,"
                + "description VARCHAR(255),"
                + "end_date DATE,"
                + "status BOOLEAN DEFAULT FALSE,"
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            logger.info("Table 'tasks' checked and created if necessary.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating tasks table.", e);
        }
    }

    public boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            assert conn != null;
            PreparedStatement pstmt = conn.prepareStatement(sql);

            String hashedPassword = hashPassword(password);

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to register user.", e);
            return false;
        }
    }

    private String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection()) {
            assert conn != null;
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
                if (result.verified) {
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to login user.", e);
        }
        return null;
    }

    private boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, "tasks", null)) {
            return resultSet.next();
        }
    }

    private boolean taskExists(Connection connection, int taskId, int userId) throws SQLException {
        String sqlCheck = "SELECT id FROM tasks WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmtCheck = connection.prepareStatement(sqlCheck)) {
            pstmtCheck.setInt(1, taskId);
            pstmtCheck.setInt(2, userId);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                return rs.next();
            }
        }
    }

    protected void insertTaskIntoDB(Connection connection, Task task, User user) throws SQLException {
        if (tableExists(connection)) {
            String sqlInsert = "INSERT INTO tasks (id, user_id, name, description, end_date, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtInsert = connection.prepareStatement(sqlInsert)) {
                pstmtInsert.setInt(1, task.getId());
                pstmtInsert.setInt(2, user.getId());
                pstmtInsert.setString(3, task.getName());
                pstmtInsert.setString(4, task.getDescription());
                pstmtInsert.setDate(5, java.sql.Date.valueOf(task.getEnd_Date()));
                pstmtInsert.setBoolean(6, task.isStatus());

                int rowsAffected = pstmtInsert.executeUpdate();
                logger.info("Rows affected by insertion: " + rowsAffected);

                logger.info("Task inserted successfully.");
            }
        } else {
            logger.warning("This table does not exist.");
        }
    }

    protected void removeTaskFromDB(Connection connection, int taskId, User user) throws SQLException {
        if (!taskExists(connection, taskId, user.getId())) {
            logger.warning("Task with ID " + taskId + " not found or does not belong to the user. No action taken.");
            return;
        }

        if (tableExists(connection)) {
            String sqlRemove = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
            try (PreparedStatement pstmtRemove = connection.prepareStatement(sqlRemove)) {
                pstmtRemove.setInt(1, taskId);
                pstmtRemove.setInt(2, user.getId());

                int rowsAffected = pstmtRemove.executeUpdate();
                logger.info("Rows affected by removal: " + rowsAffected);

                if (rowsAffected > 0) {
                    logger.info("Task removed successfully.");
                } else {
                    logger.warning("Task not removed. It may not belong to the user.");
                }
            }
        } else {
            logger.warning("This table does not exist.");
        }
    }

    public Map<Integer, Task> getAllTasks(Connection connection, User user) throws SQLException {
        Map<Integer, Task> tasks = new LinkedHashMap<>();

        String sqlGetUserId = "SELECT id FROM users WHERE username = ?";
        int userId;
        try (PreparedStatement pstmtGetUserId = connection.prepareStatement(sqlGetUserId)) {
            pstmtGetUserId.setString(1, user.getUsername());
            ResultSet rs = pstmtGetUserId.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("id");
            } else {
                logger.warning("User not found.");
                return tasks;
            }
        }

        if (tableExists(connection)) {
            String sqlSelect = "SELECT id, name, description, end_date, status FROM tasks WHERE user_id = ?";

            try (PreparedStatement pstmtSelect = connection.prepareStatement(sqlSelect)) {
                pstmtSelect.setInt(1, userId);
                ResultSet rs = pstmtSelect.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    LocalDate endDate = rs.getDate("end_date").toLocalDate();
                    boolean status = rs.getBoolean("status");

                    Task task = new Task(id, name, description, endDate, status);
                    tasks.put(id, task);
                }
            }
        } else {
            logger.warning("Table 'tasks' does not exist.");
        }
        return tasks;
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Connection closed successfully.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing connection.", e);
            }
        }
    }
}
