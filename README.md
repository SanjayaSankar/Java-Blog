# Java Blog Application

A simple blog application built with Java Swing using the MVC architecture.

## Features

- User registration and login
- Create, edit, and delete blog posts
- View all posts or just your own posts
- Persistent data storage using SQLite

## Project Structure

The project follows the MVC (Model-View-Controller) architecture:

- **Model**: Contains the data classes (`User`, `BlogPost`)
- **View**: Contains the UI components (`LoginPanel`, `RegisterPanel`, `BlogPanel`)
- **Controller**: Contains the business logic (`UserController`, `BlogController`)
- **DAO**: Contains the data access objects for interacting with the database
- **Util**: Contains utility classes like database connection manager

## Requirements

- Java 17 or higher
- Maven

## How to Run

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/java-blog.git
   cd java-blog
   ```

2. Build the project with Maven:
   ```
   mvn clean package
   ```

3. Run the application:
   ```
   java -jar target/java-blog-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## Getting Started

1. When you run the application, you'll see the login screen.
2. If you're a new user, click "Register" to create an account.
3. Fill in your details and register.
4. Log in with your username and password.
5. Once logged in, you can view all posts, create new posts, and manage your own posts.

## Database

The application uses SQLite for data storage. The database file `blog.db` will be created automatically the first time you run the application.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 