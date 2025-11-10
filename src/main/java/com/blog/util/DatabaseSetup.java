package com.blog.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for setting up the database
 */
public class DatabaseSetup {
    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseSetup() {
    }
    
    /**
     * Initialize the database by creating necessary tables if they don't exist
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "username TEXT UNIQUE NOT NULL, " +
                         "password TEXT NOT NULL, " +
                         "email TEXT UNIQUE NOT NULL, " +
                         "role TEXT NOT NULL DEFAULT 'reader', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "last_login TIMESTAMP)");
            
            // Create posts table
            stmt.execute("CREATE TABLE IF NOT EXISTS posts (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "title TEXT NOT NULL, " +
                         "content TEXT NOT NULL, " +
                         "author_id INTEGER NOT NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "status TEXT NOT NULL DEFAULT 'published', " +
                         "view_count INTEGER DEFAULT 0, " +
                         "tags TEXT, " +
                         "FOREIGN KEY (author_id) REFERENCES users(id))");
            
            // Create comments table
            stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "content TEXT NOT NULL, " +
                         "author_id INTEGER NOT NULL, " +
                         "post_id INTEGER NOT NULL, " +
                         "parent_id INTEGER DEFAULT 0, " +
                         "level INTEGER DEFAULT 0, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "hidden INTEGER DEFAULT 0, " +
                         "FOREIGN KEY (author_id) REFERENCES users(id), " +
                         "FOREIGN KEY (post_id) REFERENCES posts(id))");
            
            // Create media table
            stmt.execute("CREATE TABLE IF NOT EXISTS media (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "filename TEXT NOT NULL, " +
                         "type TEXT NOT NULL, " +
                         "size INTEGER NOT NULL, " +
                         "path TEXT NOT NULL, " +
                         "uploaded_by INTEGER NOT NULL, " +
                         "upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (uploaded_by) REFERENCES users(id))");
            
            // Create post_media table for many-to-many relationship
            stmt.execute("CREATE TABLE IF NOT EXISTS post_media (" +
                         "post_id INTEGER NOT NULL, " +
                         "media_id INTEGER NOT NULL, " +
                         "PRIMARY KEY (post_id, media_id), " +
                         "FOREIGN KEY (post_id) REFERENCES posts(id), " +
                         "FOREIGN KEY (media_id) REFERENCES media(id))");
            
            // Create likes table
            stmt.execute("CREATE TABLE IF NOT EXISTS likes (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "post_id INTEGER NOT NULL, " +
                         "user_id INTEGER NOT NULL, " +
                         "type TEXT NOT NULL, " + // 'like' or 'dislike'
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "UNIQUE(post_id, user_id), " +
                         "FOREIGN KEY (post_id) REFERENCES posts(id), " +
                         "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
} 