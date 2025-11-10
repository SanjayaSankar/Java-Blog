package com.blog.test;

import com.blog.controller.UserController;
import com.blog.model.User;

public class TestAdminLogin {
    public static void main(String[] args) {
        System.out.println("Testing admin user login...");
        
        // Get the UserController instance
        UserController userController = UserController.getInstance();
        
        // Try to login as the admin user (user id 1)
        boolean loginSuccess = userController.login("abinsaji", "12345678");
        
        if (loginSuccess) {
            System.out.println("Login successful!");
            
            // Get the current user
            User currentUser = userController.getCurrentUser();
            
            // Print user details
            System.out.println("User ID: " + currentUser.getId());
            System.out.println("Username: " + currentUser.getUsername());
            System.out.println("Role: " + currentUser.getRole());
            System.out.println("Is Admin (from user.isAdmin()): " + currentUser.isAdmin());
            System.out.println("Is Admin (from controller): " + userController.isCurrentUserAdmin());
            
            // Verify if the user can edit content
            System.out.println("Can Edit Content: " + userController.canCurrentUserEditContent());
        } else {
            System.out.println("Login failed!");
        }
    }
} 