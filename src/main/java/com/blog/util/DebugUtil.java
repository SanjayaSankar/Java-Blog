package com.blog.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.blog.controller.UserController;
import com.blog.dao.UserDAO;
import com.blog.model.User;

import java.util.List;

/**
 * Utility class with debug methods for development purposes.
 */
public class DebugUtil {
    
    /**
     * Print all users in the database for debug purposes
     */
    public static void printUsers() {
        try {
            // Print all users in the database for debugging
            UserDAO userDAO = UserDAO.getInstance();
            List<User> users = userDAO.getAllUsers();
            
            System.out.println("=== Users in database ===");
            for (User user : users) {
                System.out.println("ID: " + user.getId() + 
                                   ", Username: " + user.getUsername() + 
                                   ", Password: " + user.getPassword() + 
                                   ", Email: " + user.getEmail() +
                                   ", Role: " + user.getRole());
            }
            System.out.println("========================");
        } catch (Exception e) {
            System.err.println("Error printing users: " + e.getMessage());
        }
    }
    
    /**
     * Creates a default admin user if none exists in the database.
     * This is useful for testing the admin functionality.
     */
    public static void ensureAdminUserExists() {
        UserDAO userDAO = UserDAO.getInstance();
        UserController userController = UserController.getInstance();
        
        // Check if there's any user with admin role
        List<User> users = userDAO.getAllUsers();
        boolean adminExists = false;
        
        for (User user : users) {
            if ("admin".equals(user.getRole())) {
                adminExists = true;
                System.out.println("Admin user exists: " + user.getUsername());
                break;
            }
        }
        
        // If no admin user exists, create one
        if (!adminExists) {
            boolean created = userController.registerUser(
                "admin",
                "adminpassword",
                "admin@example.com",
                "Admin User",
                "admin",
                "Default administrator account"
            );
            
            if (created) {
                System.out.println("Created default admin user. Username: admin, Password: adminpassword");
            } else {
                System.err.println("Failed to create default admin user");
            }
        }
    }
    
    /**
     * Print all blog posts in the database for debug purposes
     */
    public static void printBlogPosts() {
        try {
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM blog_posts");
            
            System.out.println("\n=== BLOG POSTS IN DATABASE ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                        ", Title: " + rs.getString("title") + 
                        ", User ID: " + rs.getInt("user_id") + 
                        ", Status: " + rs.getString("status") + 
                        ", Tags: " + rs.getString("tags"));
            }
            System.out.println("============================\n");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error printing blog posts: " + e.getMessage());
        }
    }
    
    /**
     * Print all media attachments in the database for debug purposes
     */
    public static void printMedia() {
        try {
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, post_id, file_name, file_type FROM media");
            
            System.out.println("\n=== MEDIA IN DATABASE ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                        ", Post ID: " + rs.getInt("post_id") + 
                        ", File Name: " + rs.getString("file_name") + 
                        ", File Type: " + rs.getString("file_type"));
            }
            System.out.println("========================\n");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error printing media: " + e.getMessage());
        }
    }
    
    /**
     * Print all tables in the database
     */
    public static void printAllTables() {
        try {
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
            
            System.out.println("\n=== TABLES IN DATABASE ===");
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
            System.out.println("========================\n");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error printing tables: " + e.getMessage());
        }
    }
    
    /**
     * Print the schema for a specific table
     */
    public static void printTableSchema(String tableName) {
        try {
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
            
            System.out.println("\n=== SCHEMA FOR " + tableName + " ===");
            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + rs.getString("type") + 
                        (rs.getInt("pk") > 0 ? " (PRIMARY KEY)" : ""));
            }
            System.out.println("========================\n");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error printing schema: " + e.getMessage());
        }
    }
} 