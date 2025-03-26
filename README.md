# Data Management App

A Spring Boot application for managing dynamic tables.

## Prerequisites
- Java 21
- Maven 3.9.9
- MySQL

## Setup
1. Clone the repository:
```git clone <repository-url>```
2. Set up the MySQL database and create a database named `your_database_name`.
3. Configure the database credentials:
- Set the following environment variables:
DB_URL=jdbc:mysql://localhost:3306/your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
- Alternatively, create a `.env` file in the project root with the above variables.
4. Run the application:
mvn clean spring-boot:run
5. Access the application at `http://localhost:5050/login`.

## Default Credentials
- Username: `aman`
- Password: (as per your database)

## Features
- Create, edit, and delete tables.
- View table data and add new records.
- User registration and login.