package com.blog.view;

import com.blog.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class UserProfilePanel extends JPanel {
    private final BlogApp app;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextArea bioArea;
    private JLabel profileImage;
    private byte[] imageData;
    private JPanel formPanel;
    private JTabbedPane tabbedPane;
    
    // Password change components
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordStatus;
    
    private static final int IMAGE_WIDTH = 150;
    private static final int IMAGE_HEIGHT = 150;

    private JLabel roleDisplayLabel;

    public UserProfilePanel(BlogApp app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("User Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // User info and back button panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JButton backButton = new JButton("Back to Blog");
        backButton.addActionListener(e -> app.showBlogPanel());
        userPanel.add(backButton);
        
        headerPanel.add(userPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed panel for profile and password
        tabbedPane = new JTabbedPane();
        
        // Profile tab
        JPanel profileTab = createProfileTab();
        tabbedPane.addTab("Profile", profileTab);
        
        // Password tab
        JPanel passwordTab = createPasswordTab();
        tabbedPane.addTab("Change Password", passwordTab);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createProfileTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create form panel
        formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Profile Image 
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        
        JPanel imagePanel = new JPanel(new BorderLayout());
        
        // Default image placeholder
        profileImage = new JLabel();
        profileImage.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        profileImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        profileImage.setHorizontalAlignment(JLabel.CENTER);
        
        imagePanel.add(profileImage, BorderLayout.CENTER);
        
        JButton uploadButton = new JButton("Upload Image");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseProfileImage();
            }
        });
        imagePanel.add(uploadButton, BorderLayout.SOUTH);
        
        formPanel.add(imagePanel, gbc);
        
        // Full Name
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        JLabel fullNameLabel = new JLabel("Full Name:");
        formPanel.add(fullNameLabel, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        fullNameField = new JTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Email
        gbc.gridx = 1;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 1;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Role Display (read-only)
        gbc.gridx = 1;
        gbc.gridy = 2;
        JLabel roleLabel = new JLabel("Role:");
        formPanel.add(roleLabel, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 2;
        roleDisplayLabel = new JLabel();
        roleDisplayLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(roleDisplayLabel, gbc);
        
        // Bio
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel bioLabel = new JLabel("Bio:");
        formPanel.add(bioLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        bioArea = new JTextArea(5, 30);
        bioArea.setLineWrap(true);
        JScrollPane bioScroll = new JScrollPane(bioArea);
        formPanel.add(bioScroll, gbc);
        
        // Reset weight
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        
        // Status message
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);
        
        // Save button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save Profile");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfile(statusLabel);
            }
        });
        formPanel.add(saveButton, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JPanel createPasswordTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Current Password
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        formPanel.add(currentPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        currentPasswordField = new JPasswordField(20);
        formPanel.add(currentPasswordField, gbc);
        
        // New Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel newPasswordLabel = new JLabel("New Password:");
        formPanel.add(newPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        newPasswordField = new JPasswordField(20);
        formPanel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        passwordStatus = new JLabel(" ");
        passwordStatus.setForeground(Color.RED);
        formPanel.add(passwordStatus, gbc);
        
        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
        formPanel.add(changePasswordButton, gbc);
        
        mainPanel.add(formPanel, BorderLayout.NORTH);
        return mainPanel;
    }
    
    public void loadUserData() {
        if (!app.getUserController().isLoggedIn()) {
            return;
        }
        
        User user = app.getUserController().getCurrentUser();
        
        // Load profile data
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        bioArea.setText(user.getBio());
        
        // Set the role display
        roleDisplayLabel.setText(user.getRole());
        
        // Change the text color based on role
        if ("admin".equals(user.getRole())) {
            roleDisplayLabel.setForeground(new Color(128, 0, 0)); // Dark red
        } else if ("author".equals(user.getRole())) {
            roleDisplayLabel.setForeground(new Color(0, 128, 0)); // Dark green
        } else {
            roleDisplayLabel.setForeground(Color.BLUE); // Blue for reader
        }
        
        // Load profile image
        if (user.getProfileImageData() != null) {
            displayProfileImage(user.getProfileImageData());
            imageData = user.getProfileImageData();
        } else {
            displayDefaultImage();
        }
    }
    
    private void displayProfileImage(byte[] data) {
        if (data == null || data.length == 0) {
            displayDefaultImage();
            return;
        }
        
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            if (img != null) {
                // Create a circular image for profile picture
                int size = Math.min(IMAGE_WIDTH, IMAGE_HEIGHT);
                BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = circularImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                
                // Draw the image scaled to fit the circle
                Image scaledImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                g2.drawImage(scaledImg, 0, 0, null);
                g2.dispose();
                
                profileImage.setIcon(new ImageIcon(circularImage));
            }
        } catch (IOException e) {
            System.err.println("Error displaying profile image: " + e.getMessage());
            displayDefaultImage();
        }
    }
    
    private void displayDefaultImage() {
        profileImage.setIcon(null);
        profileImage.setText("No Image");
    }
    
    private void chooseProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage originalImage = ImageIO.read(selectedFile);
                
                // Resize image if necessary
                Image scaledImage = originalImage.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                BufferedImage resizedImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resizedImage.createGraphics();
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();
                
                // Convert to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", baos);
                this.imageData = baos.toByteArray();
                
                // Display the image
                displayProfileImage(this.imageData);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading image: " + e.getMessage(), 
                    "Image Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveProfile(JLabel statusLabel) {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String bio = bioArea.getText().trim();
        
        if (fullName.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Full name and email are required");
            return;
        }
        
        // Update profile
        boolean profileUpdated = app.getUserController().updateProfile(fullName, email, bio);
        
        // Update profile picture if there's a new one
        if (profileUpdated && imageData != null) {
            app.getUserController().updateProfilePicture(imageData);
        }
        
        if (profileUpdated) {
            statusLabel.setText("Profile updated successfully");
            statusLabel.setForeground(new Color(0, 150, 0));
            
            // Reload user data to reflect changes
            loadUserData();
        } else {
            statusLabel.setText("Failed to update profile");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void changePassword() {
        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        
        // Validate inputs
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            passwordStatus.setText("All fields are required");
            return;
        }
        
        if (!newPass.equals(confirmPass)) {
            passwordStatus.setText("New password and confirmation do not match");
            return;
        }
        
        // Change password
        boolean success = app.getUserController().changePassword(currentPass, newPass);
        if (success) {
            passwordStatus.setText("Password changed successfully");
            passwordStatus.setForeground(new Color(0, 150, 0));
            
            // Clear the fields
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            passwordStatus.setText("Failed to change password. Current password may be incorrect.");
            passwordStatus.setForeground(Color.RED);
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        loadUserData();
    }
} 