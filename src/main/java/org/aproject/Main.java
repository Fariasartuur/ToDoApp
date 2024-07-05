package org.aproject;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aproject.DatabaseManager.createTasksTableIfNotExists;
import static org.aproject.DatabaseManager.createUsersTableIfNotExists;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Connection con = null;
        try {
            con = DatabaseManager.getConnection();
            TaskManager task = new TaskManager();
            DatabaseManager DB = new DatabaseManager();

            if(con != null){
                createUsersTableIfNotExists(con);
                createTasksTableIfNotExists(con);
            }

            Scanner sc = new Scanner(System.in);

            int option;
            String username;
            String password;
            User user = null;
            do {
                System.out.println("Log-in (1)");
                System.out.println("Register (2)");
                System.out.println("EXIT (0)");

                option = task.insertInt(sc);
                sc.nextLine();

                switch (option) {
                    case 0:
                        break;
                    case 1:
                        System.out.print("USERNAME: ");
                        username = sc.nextLine();
                        System.out.print("PASSWORD: ");
                        password = sc.nextLine();

                        user = DB.loginUser(username, password);
                        if (user == null) {
                            logger.warning("Login failed. Please try again.");
                        }
                        break;
                    case 2:
                        System.out.print("EMAIL: ");
                        String email = sc.nextLine();
                        System.out.print("USERNAME: ");
                        username = sc.nextLine();
                        System.out.print("PASSWORD: ");
                        password = sc.nextLine();

                        boolean registered = DB.registerUser(username, password, email);
                        if (registered) {
                            logger.info("Registration successful. Please log in.");
                        } else {
                            logger.warning("Registration failed. Please try again.");
                        }
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } while (option != 0 && user == null);

            int option1;
            do {
                task.loadTasksFromDB(con, user);
                System.out.println("Choose an option: ");
                System.out.println("1 - Insert Task");
                System.out.println("2 - Remove Task");
                System.out.println("3 - Show Table (TASKS)");
                System.out.println("4 - User info");
                System.out.println("0 - EXIT");
                option1 = task.insertInt(sc);
                sc.nextLine();

                switch (option1) {
                    case 1:
                        System.out.print("Enter the task name/title: ");
                        String name = sc.nextLine();
                        System.out.print("Enter the task description: ");
                        String description = sc.nextLine();
                        System.out.print("Enter the task deadline (YYYY/MM/DD): ");
                        String date = sc.nextLine();

                        task.createTask(con, user, name, description, date);
                        break;
                    case 2:
                        if (TaskManager.getTask().isEmpty()) {
                            logger.warning("No tasks exist.");
                            break;
                        }
                        task.print();
                        System.out.println("Enter the task ID: ");
                        int id = task.insertInt(sc);
                        sc.nextLine();

                        task.removeTask(con, user, id);
                        break;
                    case 3:
                        if (TaskManager.getTask().isEmpty()) {
                            logger.warning("No tasks exist.");
                            break;
                        }

                        try {
                            assert user != null;
                            task.showTasks(con, user.getId());
                        } catch (SQLException ex) {
                            logger.log(Level.SEVERE, "Error while showing tasks", ex);
                        }
                        break;
                    case 4:
                        assert user != null;
                        System.out.println("ID: " + user.getId() + " - Name: " + user.getUsername() + " - Email: " + user.getEmail());
                        break;
                    case 0:
                        break;
                    default:
                        System.err.println("Invalid option, please try again.");
                }
            } while (option1 != 0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during program execution", e);
        } finally {
            // Closing the database connection
            DatabaseManager.closeConnection(con);
        }

    }
}
