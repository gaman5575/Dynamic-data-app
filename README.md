# Data Management App

A Spring Boot application for managing dynamic tables, allowing users to create, edit, and delete tables, view table data, add new records, and manage user accounts through registration and login.

## Prerequisites
- Java 21
- Maven 3.9.9
- MySQL

## Setup
1. **Clone the repository**:
   ```bash
   git clone <repository-url>
2. **Set up the MySQL database**:
    - Create a database named your_database_name.
    - Example command in MySQL:
 ```CREATE DATABASE your_database_name;```
3. **Configure the database credentials**:
    - Set the following environment variables:
        - ```export DB_URL=jdbc:mysql://localhost:3306/your_database_name```
        - ```export DB_USERNAME=your_username```
        - ```export DB_PASSWORD=your_password```
4. **Run the application**:
    - Navigate to the project directory and run:
        - Install Dependencies: ``` mvn clean install``` 
        - Run the Application: ```mvn clean spring-boot:run``` 
5. **Access the application**:
    - Open your browser and go to http://localhost:5050/login.

## Features
- Create, edit, and delete tables dynamically.
- View table data and add new records to existing tables.
- User registration and login for secure access.

## Project Structure
- *Templates*:
    - login.html: User login page.
    - register.html: User registration page.
    - dashboard.html: Displays a list of tables with options to view, edit, or delete.
    - create-table.html: Form to create a new table.
    - edit-table.html: Form to edit an existing table.
    - view-table.html: Displays table data with options to add or delete records.
    - add-data.html: Form to add new records to a table.
    - fragments/fragments.html: Reusable navbar fragment.
- *Static Files*:
    - css/styles.css: Custom styles for the application.
- *Database*:
    - Uses MySQL to store user and table data.
    - Default user (aman) is pre-inserted via data.sql.

## Troubleshooting
- *Database Connection Issues*:
    - Ensure MySQL is running and the database your_database_name exists.
    - Verify that DB_URL, DB_USERNAME, and DB_PASSWORD are set correctly.
- *Port Conflicts*:
    - If port 5050 is in use, update server.port in application.properties to a different port.
- *Login Issues*:
    - Use the default credentials (aman/password123) to log in.
    -   If the default user is not created, check that data.sql is executed (spring.sql.init.mode=always in application.properties).

## Notes
- The application uses Spring Boot with Thymeleaf for templating.
- Passwords are stored securely using BCrypt hashing.
- The database schema is automatically updated on startup (spring.jpa.hibernate.ddl-auto=update).


---