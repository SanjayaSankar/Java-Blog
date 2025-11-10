package com.blog.test;

import com.blog.controller.BlogController;
import com.blog.controller.UserController;
import com.blog.model.BlogPost;
import com.blog.model.User;
import com.blog.view.BlogApp;
import com.blog.view.PostView;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestPostViewButtons {
    public static void main(String[] args) {
        System.out.println("Testing PostView buttons as admin...");
        
        // Get the UserController instance and login as admin
        UserController userController = UserController.getInstance();
        
        // Try to login as the admin user (user id 1)
        boolean loginSuccess = userController.login("abinsaji", "12345678");
        
        if (!loginSuccess) {
            System.out.println("Login failed! Exiting test.");
            return;
        }
        
        System.out.println("Login successful!");
        
        // Verify admin status
        User currentUser = userController.getCurrentUser();
        System.out.println("User ID: " + currentUser.getId());
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Is Admin (from user.isAdmin()): " + currentUser.isAdmin());
        System.out.println("Is Admin (from controller): " + userController.isCurrentUserAdmin());
        
        // Create BlogController with the UserController
        BlogController blogController = new BlogController(userController);
        
        // Get a post that wasn't created by this user
        List<BlogPost> allPosts = blogController.getAllPosts();
        System.out.println("Found " + allPosts.size() + " posts total");
        
        Optional<BlogPost> postOpt = allPosts.stream()
            .filter(p -> p.getAuthor().getId() != currentUser.getId())
            .findFirst();
        
        if (postOpt.isEmpty()) {
            System.out.println("No posts found by non-admin user. Exiting test.");
            return;
        }
        
        BlogPost post = postOpt.get();
        System.out.println("Found post ID: " + post.getId() + ", by author ID: " + post.getAuthor().getId() + ", author username: " + post.getAuthor().getUsername());
        
        // Create a mock BlogApp for testing
        MockBlogApp mockApp = new MockBlogApp(userController, blogController);
        
        // Create the PostView with the mock app
        PostView postView = new PostView(mockApp, post.getId());
        
        // Display the PostView in a frame for manual inspection
        JFrame testFrame = new JFrame("PostView Button Test");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(800, 600);
        testFrame.getContentPane().add(new JScrollPane(postView));
        testFrame.setVisible(true);
    }
    
    // Mock BlogApp class for testing
    static class MockBlogApp extends BlogApp {
        private final UserController userController;
        private final BlogController blogController;
        
        public MockBlogApp(UserController userController, BlogController blogController) {
            // Call super constructor with empty args (it won't actually be used)
            super();
            
            this.userController = userController;
            this.blogController = blogController;
        }
        
        @Override
        public UserController getUserController() {
            return userController;
        }
        
        @Override
        public BlogController getBlogController() {
            return blogController;
        }
        
        @Override
        public void showBlogPanel() {
            System.out.println("Mock: Show blog panel called");
        }
        
        @Override
        public void showEditPostEditor(int postId) {
            System.out.println("Mock: Show edit post editor called for post ID: " + postId);
        }
        
        @Override
        public void showPostView(int postId) {
            System.out.println("Mock: Show post view called for post ID: " + postId);
        }
    }
} 