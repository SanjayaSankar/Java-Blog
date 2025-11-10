package com.blog.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static Connection connection;
    private static final String DB_URL = "jdbc:sqlite:blog.db";
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Create connection
                connection = DriverManager.getConnection(DB_URL);
                
                // Create tables if they don't exist
                createTables();
                
                return connection;
            } catch (SQLException e) {
                System.err.println("Error connecting to database: " + e.getMessage());
                return null;
            }
        }
        
        // Check if the connection is closed and reopen if needed
        try {
            if (connection.isClosed()) {
                System.out.println("DEBUG: Reopening closed database connection");
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Error checking or reopening database connection: " + e.getMessage());
            try {
                connection = DriverManager.getConnection(DB_URL);
            } catch (SQLException ex) {
                System.err.println("Failed to reopen database connection: " + ex.getMessage());
            }
        }
        
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("DEBUG: Database connection closed by application");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    private static void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "full_name TEXT NOT NULL," +
                "role TEXT," +
                "profile_picture TEXT," +
                "bio TEXT," +
                "profile_image_data BLOB" +
                ")"
            );
            
            // Blog posts table with enhanced fields
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS blog_posts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "status TEXT DEFAULT 'draft'," +
                "tags TEXT," +
                "comment_count INTEGER DEFAULT 0," +
                "like_count INTEGER DEFAULT 0," +
                "dislike_count INTEGER DEFAULT 0," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );
            
            // Media table for blog post attachments
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS media (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "post_id INTEGER NOT NULL," +
                "file_name TEXT NOT NULL," +
                "file_type TEXT NOT NULL," +
                "file_data BLOB," +
                "file_path TEXT," +
                "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "caption TEXT," +
                "FOREIGN KEY (post_id) REFERENCES blog_posts(id) ON DELETE CASCADE" +
                ")"
            );
            
            // Comments table with support for threaded comments
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "post_id INTEGER NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "content TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "hidden BOOLEAN DEFAULT 0," +
                "parent_id INTEGER," +
                "FOREIGN KEY (post_id) REFERENCES blog_posts(id) ON DELETE CASCADE," +
                "FOREIGN KEY (user_id) REFERENCES users(id)," +
                "FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE" +
                ")"
            );
            
            // Likes table for tracking user reactions to posts
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS likes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "post_id INTEGER NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "is_like BOOLEAN NOT NULL," +  // true for like, false for dislike
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (post_id) REFERENCES blog_posts(id) ON DELETE CASCADE," +
                "FOREIGN KEY (user_id) REFERENCES users(id)," +
                "UNIQUE(post_id, user_id)" +  // Each user can only have one reaction per post
                ")"
            );
            
            // Sessions table for user login sessions
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "session_token TEXT NOT NULL UNIQUE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "expires_at TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );
            
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
    
    public static String getFileType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
        }
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}