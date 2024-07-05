# Task Manager Project

## Overview
This project is a basic task manager application implemented in Java. Its purpose is primarily educational, aimed at learning database management with JDBC.

## Functionality
The task manager allows users to:
- Log in or register new accounts.
- Create, remove, and view tasks associated with their account.
- Store tasks in a MySQL database.
- Utilize password hashing for secure user authentication.

## Setting Up
### Requirements
- JDK (Java Development Kit)
- MySQL database server
- Maven (for building the project)

### Configuration
1. **Database Setup:**
   - Create a MySQL database for the application.
   - Use the provided `schema.sql` file to set up the necessary table (`users` and `tasks`).
   
2. **Environment Variables:**
   - Create a `.env` file in the project root directory.
   - Add the following lines to specify your database connection details:
     ```
     DATABASE_URL=jdbc:mysql://localhost:3306/your_database_name
     DATABASE_USER=your_database_username
     DATABASE_PASSWORD=your_database_password
     ```

### Building and Running
1. **Build the Project:**
     ```
     mvn clean package
     ```

2. **Run the Application:**
     ```
     java -jar target/TaskManager.jar
     ```


### Notes
- Ensure that Maven (`mvn`) and Java (`java`) are added to your system's PATH environment variable.
- Modify the `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` values in the `.env` file according to your MySQL database configuration.
- Additional modifications may be necessary depending on your local database setup and environment.

## Contributors
- [Artur](https://github.com/fariasartuur) - Creator and maintainer

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

     
