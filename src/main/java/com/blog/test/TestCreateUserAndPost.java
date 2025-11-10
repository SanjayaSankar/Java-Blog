package com.blog.test;

import com.blog.controller.BlogController;
import com.blog.controller.UserController;

public class TestCreateUserAndPost {
    public static void main(String[] args) {
        System.out.println("Creating test user and post...");
        
        // Get the UserController instance
        UserController userController = UserController.getInstance();
        
        // Create a new test user
        if (userController.registerUser("testuser", "12345678", "test@example.com", "Test User")) {
            System.out.println("Test user created successfully!");
            
            // Login as test user
            if (userController.login("testuser", "12345678")) {
                System.out.println("Logged in as test user!");
                
                // Create a new post
                BlogController blogController = new BlogController(userController);
                if (blogController.createPost("Test Post Title", "This is a test post content created by testuser.", "test,tutorial", "published")) {
                    System.out.println("Test post created successfully!");
                } else {
                    System.out.println("Failed to create test post.");
                }
                
                // Logout
                userController.logout();
                System.out.println("Logged out successfully.");
            } else {
                System.out.println("Failed to login as test user.");
            }
        } else {
            System.out.println("Failed to create test user.");
        }
    }
} 