package com.blog.view;

import com.blog.util.DebugUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private final BlogApp app;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    public LoginPanel(BlogApp app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Blog Application - Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        loginButton = new JButton("Login");
        formPanel.add(loginButton, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        registerButton = new JButton("Register");
        formPanel.add(registerButton, gbc);
        
        // Admin Portal button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        JButton adminPortalButton = new JButton("ADMIN PORTAL");
        adminPortalButton.setBackground(new Color(50, 100, 150));
        adminPortalButton.setForeground(Color.WHITE);
        adminPortalButton.setFont(new Font("Arial", Font.BOLD, 14));
        adminPortalButton.setPreferredSize(new Dimension(200, 40));
        adminPortalButton.setToolTipText("Access the admin portal (requires admin credentials)");
        adminPortalButton.setFocusPainted(false);
        adminPortalButton.setBorderPainted(false);
        adminPortalButton.setOpaque(true);
        adminPortalButton.addActionListener(e -> handleAdminLogin());
        formPanel.add(adminPortalButton, gbc);
        
        // Debug button
        gbc.gridx = 2;
        gbc.gridy = 3;
        JButton debugButton = new JButton("Debug");
        debugButton.addActionListener(e -> DebugUtil.printUsers());
        formPanel.add(debugButton, gbc);
        
        // Add form panel to center
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.showRegisterPanel();
            }
        });
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty");
            return;
        }
        
        System.out.println("Login attempt: Username=" + username + ", Password=" + password);
        DebugUtil.printUsers();
        
        boolean success = app.login(username, password);
        if (success) {
            usernameField.setText("");
            passwordField.setText("");
            statusLabel.setText("");
            app.showBlogPanel();
        } else {
            statusLabel.setText("Invalid username or password");
        }
    }

    private void handleAdminLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty");
            return;
        }
        
        System.out.println("Admin login attempt: Username=" + username);
        
        boolean success = app.login(username, password);
        if (success && app.getUserController().isCurrentUserAdmin()) {
            usernameField.setText("");
            passwordField.setText("");
            statusLabel.setText("");
            app.showAdminPortal();
        } else {
            statusLabel.setText("Invalid admin credentials");
        }
    }
}