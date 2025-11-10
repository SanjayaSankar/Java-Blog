package com.blog.view;

import com.blog.util.DebugUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RegisterPanel extends JPanel {
    private final BlogApp app;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<String> roleComboBox;
    private JTextArea bioTextArea;
    private JButton profilePictureButton;
    private JLabel profilePictureLabel;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private byte[] profileImageData;
    private File selectedImageFile;

    public RegisterPanel(BlogApp app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Blog Application - Register");
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
        
        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel emailLabel = new JLabel("Email:");
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel fullNameLabel = new JLabel("Full Name:");
        formPanel.add(fullNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        fullNameField = new JTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Role
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JLabel roleLabel = new JLabel("Role:");
        formPanel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        roleComboBox = new JComboBox<>(new String[]{"reader", "author", "admin"});
        formPanel.add(roleComboBox, gbc);
        
        // Bio
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        JLabel bioLabel = new JLabel("Bio:");
        formPanel.add(bioLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        bioTextArea = new JTextArea(4, 20);
        bioTextArea.setLineWrap(true);
        bioTextArea.setWrapStyleWord(true);
        JScrollPane bioScrollPane = new JScrollPane(bioTextArea);
        formPanel.add(bioScrollPane, gbc);
        
        // Profile Picture
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        JLabel profilePictureLabelTitle = new JLabel("Profile Picture:");
        formPanel.add(profilePictureLabelTitle, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        profilePictureButton = new JButton("Choose File");
        profilePictureButton.addActionListener(e -> selectProfilePicture());
        formPanel.add(profilePictureButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        profilePictureLabel = new JLabel("No file selected");
        formPanel.add(profilePictureLabel, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        registerButton = new JButton("Register");
        formPanel.add(registerButton, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 9;
        cancelButton = new JButton("Cancel");
        formPanel.add(cancelButton, gbc);
        
        // Login button
        gbc.gridx = 2;
        gbc.gridy = 9;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> app.showLoginPanel());
        formPanel.add(loginButton, gbc);
        
        // Admin Portal button
        gbc.gridx = 1;
        gbc.gridy = 10;
        JButton adminPortalButton = new JButton("Admin Portal");
        adminPortalButton.addActionListener(e -> app.showAdminPortal());
        formPanel.add(adminPortalButton, gbc);
        
        // Debug button
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        JButton debugButton = new JButton("Debug");
        debugButton.addActionListener(e -> {
            DebugUtil.printUsers();
        });
        formPanel.add(debugButton, gbc);
        
        // Add form panel to center
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Add action listeners
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
                app.showLoginPanel();
            }
        });
    }
    
    private void selectProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            profilePictureLabel.setText(selectedImageFile.getName());
            
            try {
                // Read the image file as bytes
                profileImageData = Files.readAllBytes(selectedImageFile.toPath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error reading image file: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                profileImageData = null;
                profilePictureLabel.setText("No file selected");
            }
        }
    }
    
    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();
        String fullName = fullNameField.getText();
        String role = (String) roleComboBox.getSelectedItem();
        String bio = bioTextArea.getText();
        
        // Validate inputs
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() 
                || email.isEmpty() || fullName.isEmpty()) {
            statusLabel.setText("All fields are required except Bio and Profile Picture");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match");
            return;
        }
        
        // Simple email validation
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Invalid email format");
            return;
        }
        
        System.out.println("Registration attempt: Username=" + username + ", Email=" + email);
        
        // Create user
        boolean success = app.register(username, password, email, fullName, role, bio, profileImageData);
        if (success) {
            clearFields();
            // If user registered as admin, show admin portal, otherwise show blog
            if ("admin".equals(role)) {
                app.showAdminPortal();
            } else {
                app.showBlogPanel();
            }
        } else {
            statusLabel.setText("Registration failed. Please try again.");
        }
    }
    
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        emailField.setText("");
        fullNameField.setText("");
        bioTextArea.setText("");
        profilePictureLabel.setText("No file selected");
        profileImageData = null;
        selectedImageFile = null;
        statusLabel.setText("");
        roleComboBox.setSelectedIndex(0);
    }
} 