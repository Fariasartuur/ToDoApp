package org.aproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskManager {

    private static final Map<Integer, Task> tasks = new LinkedHashMap<>();

    private static int task_id = 0;
    private static final DatabaseManager dbm = new DatabaseManager();
    private static final Logger logger = Logger.getLogger(TaskManager.class.getName());

    public int insertInt(Scanner scanner) {
        int num = 0;
        boolean valido = false;

        while(!valido) {
            try {
                num = scanner.nextInt();
                valido = true;
            } catch (InputMismatchException var5) {
                System.out.println("Entrada invalida. Por favor, digite um numero valido");
                scanner.next();
            }
        }

        return num;
    }

    public void createTask(Connection con, User user, String name, String description, String date) {
        LocalDate finalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        task_id++;
        Task newTask = new Task(task_id, name, description, finalDate, false);
        tasks.put(task_id, newTask);

        try {
            dbm.insertTaskIntoDB(con, newTask, user);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error trying to insert new task in the database", e);
            throw new RuntimeException(e);
        }
    }

    public void removeTask(Connection con, User user, int id) {
        Task taskToRemove = tasks.get(id);

        if (taskToRemove == null) {
            System.out.println("ID not found.");
            return;
        }

        tasks.remove(id);
        task_id--;

        Map<Integer, Task> tempMap = new LinkedHashMap<>();

        int newId = 1;
        for (Task t : tasks.values()) {
            tempMap.put(newId++, t);
        }

        tasks.clear();
        tasks.putAll(tempMap);

        try {
            dbm.removeTaskFromDB(con, id, user);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error trying to remove the task from the database", e);
            throw new RuntimeException(e);
        }
    }

    public void print() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
            System.out.print("ID: " + entry.getKey() + " - Name: " + entry.getValue().getName() + " - Description: " + entry.getValue().getDescription());
            String formattedDate = entry.getValue().getEnd_Date().format(formatter);
            System.out.println(" - Completed: " + entry.getValue().isStatus() + " - Date: " + formattedDate);
        }
    }

    public void loadTasksFromDB(Connection con, User user) {
        try {
            Map<Integer, Task> tasksFromDB = dbm.getAllTasks(con, user);
            tasks.clear();
            tasks.putAll(tasksFromDB);
            task_id = tasksFromDB.size();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error trying to load tasks from the database", e);
            throw new RuntimeException(e);
        }
    }

    public void showTasks(Connection con, int user_id) throws SQLException {
        String sqlShow = "SELECT id, name, description, end_date, status FROM tasks WHERE user_id = ?";

        try (PreparedStatement pstmtShow = con.prepareStatement(sqlShow)) {
            pstmtShow.setInt(1, user_id);
            ResultSet rs = pstmtShow.executeQuery();

            while (rs.next()) {
                Task retrievedTask = new Task(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getBoolean("status")
                );

                System.out.println("ID: " + retrievedTask.getId() +
                        " - Name: " + retrievedTask.getName() +
                        " - Description: " + retrievedTask.getDescription() +
                        " - Completed: " + retrievedTask.isStatus() +
                        " - Date: " + retrievedTask.getEnd_Date());

            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while showing tasks", e);
            throw e;
        }
    }

    public static Map<Integer, Task> getTask() {
        return tasks;
    }

}
