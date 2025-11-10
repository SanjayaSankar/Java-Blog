package com.blog.view;

import com.blog.controller.BlogController;
import com.blog.controller.UserController;
import com.blog.model.BlogPost;
import com.blog.model.User;
import com.blog.service.AnalyticsService;
import com.blog.service.AuthenticationService;
import com.blog.util.AnalyticsReportUtil;
import com.blog.util.DatabaseSetup;
import com.blog.util.DatabaseUtil;
import com.blog.util.DebugUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

public class BlogApp {
    private final UserController userController;
    private final BlogController blogController;
    private final AuthenticationService authService;
    private final AnalyticsService analyticsService;
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private AppBar appBar;  // App bar that will contain the profile button
    
    // Card names
    private static final String LOGIN_PANEL = "LOGIN";
    private static final String REGISTER_PANEL = "REGISTER";
    private static final String BLOG_PANEL = "BLOG";
    private static final String PROFILE_PANEL = "PROFILE";
    private static final String POST_EDITOR_PANEL = "POST_EDITOR";
    private static final String POST_VIEW_PANEL = "POST_VIEW";
    private static final String ADMIN_PORTAL_PANEL = "ADMIN_PORTAL";
    
    // Map to cache PostView panels by post ID
    private java.util.Map<Integer, PostView> postViewCache = new java.util.HashMap<>();
    
    /**
     * Inner class for the application bar that appears on top of all screens
     */
    private class AppBar extends JPanel {
        private JLabel titleLabel;
        private JButton profileButton;
        private JButton logoutButton;
        private JButton adminButton;
        
        public AppBar() {
            setLayout(new BorderLayout());
            setBackground(new Color(70, 130, 180));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            
            titleLabel = new JLabel("Blog Application");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setForeground(Color.WHITE);
            add(titleLabel, BorderLayout.WEST);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonPanel.setOpaque(false);
            
            // Admin portal button (visible only for admin users)
            adminButton = new JButton("Admin Portal");
            adminButton.addActionListener(e -> showAdminPortal());
            adminButton.setVisible(false); // Initially hidden
            buttonPanel.add(adminButton);
            
            // Profile button
            profileButton = createProfileButton();
            profileButton.addActionListener(e -> showProfilePanel());
            JPanel profilePanel = new JPanel(new BorderLayout());
            profilePanel.setOpaque(false);
            profilePanel.add(profileButton, BorderLayout.CENTER);
            
            // Add a label under the profile button
            JLabel profileLabel = new JLabel("Profile", JLabel.CENTER);
            profileLabel.setForeground(Color.WHITE);
            profileLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            profilePanel.add(profileLabel, BorderLayout.SOUTH);
            
            buttonPanel.add(profilePanel);
            
            // Logout button (visible only when logged in)
            logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> logout());
            logoutButton.setVisible(false); // Initially hidden
            buttonPanel.add(logoutButton);
            
            add(buttonPanel, BorderLayout.EAST);
        }
        
        private JButton createProfileButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    
                    if (!userController.isLoggedIn()) {
                        return;
                    }
                    
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    User user = userController.getCurrentUser();
                    
                    // Try to use profile image if available
                    boolean imageDrawn = false;
                    if (user.getProfileImageData() != null) {
                        try {
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(user.getProfileImageData()));
                            if (img != null) {
                                int size = Math.min(getWidth(), getHeight());
                                g2d.setClip(new java.awt.geom.Ellipse2D.Float(
                                    (getWidth() - size) / 2,
                                    (getHeight() - size) / 2,
                                    size, size));
                                g2d.drawImage(img.getScaledInstance(size, size, Image.SCALE_SMOOTH), 
                                    (getWidth() - size) / 2, 
                                    (getHeight() - size) / 2, 
                                    this);
                                imageDrawn = true;
                            }
                        } catch (IOException e) {
                            // Fall back to initials if image loading fails
                            System.err.println("Error loading profile image: " + e.getMessage());
                        }
                    }
                    
                    if (!imageDrawn) {
                        // Draw circle with user initials
                        int size = Math.min(getWidth(), getHeight()) - 4;
                        g2d.setColor(new Color(100, 150, 200));
                        g2d.fillOval(
                            (getWidth() - size) / 2,
                            (getHeight() - size) / 2,
                            size, size);
                        
                        // Get user initials
                        String initials = "";
                        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                            initials = String.valueOf(user.getUsername().charAt(0)).toUpperCase();
                            if (user.getFullName() != null && user.getFullName().contains(" ")) {
                                String[] parts = user.getFullName().split(" ");
                                if (parts.length > 1 && parts[1].length() > 0) {
                                    initials += String.valueOf(parts[1].charAt(0)).toUpperCase();
                                }
                            }
                        }
                        
                        // Draw initials
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Arial", Font.BOLD, 16));
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(initials);
                        int textHeight = fm.getHeight();
                        g2d.drawString(initials, 
                            (getWidth() - textWidth) / 2, 
                            (getHeight() - textHeight) / 2 + fm.getAscent());
                    }
                    
                    g2d.dispose();
                }
            };
            
            button.setPreferredSize(new Dimension(40, 40));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            
            return button;
        }
        
        @Override
        public void updateUI() {
            super.updateUI();
            // Initialize components if null
            if (titleLabel == null) {
                titleLabel = new JLabel("Blog Application");
            }
            if (profileButton == null) {
                profileButton = createProfileButton();
            }
            if (logoutButton == null) {
                logoutButton = new JButton("Logout");
            }
            if (adminButton == null) {
                adminButton = new JButton("Admin Portal");
            }
            
            if (userController != null) {
                boolean loggedIn = userController.isLoggedIn();
                logoutButton.setVisible(loggedIn);
                
                // Show admin button only for admin users
                if (loggedIn && userController.isCurrentUserAdmin()) {
                    adminButton.setVisible(true);
                } else {
                    adminButton.setVisible(false);
                }
                
                if (loggedIn && userController.getCurrentUser() != null) {
                    titleLabel.setText("Welcome, " + userController.getCurrentUser().getUsername());
                } else {
                    titleLabel.setText("Blog Application");
                }
                
                if (profileButton != null) {
                    profileButton.repaint();
                }
            }
        }
    }
    
    public BlogApp() {
        // Initialize controllers
        userController = UserController.getInstance();
        
        blogController = new BlogController(userController);
        
        authService = AuthenticationService.getInstance();
        
        // Initialize analytics service
        analyticsService = AnalyticsService.getInstance();
        
        // Initialize the database schema if needed
        DatabaseSetup.initializeDatabase();
        
        // Initialize the UI
        initUI();
        
        // Try to resume session if there's a valid token
        tryResumeSession();
    }
    
    private void initUI() {
        // Create main frame
        mainFrame = new JFrame("Blog Application");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(900, 700);
        mainFrame.setLocationRelativeTo(null);
        
        // Create the app bar
        appBar = new AppBar();
        
        // Create content panel to hold app bar and card panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Add app bar to the top
        contentPanel.add(appBar, BorderLayout.NORTH);
        
        // Create card layout and panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Create view components
        LoginPanel loginPanel = new LoginPanel(this);
        RegisterPanel registerPanel = new RegisterPanel(this);
        BlogPanel blogPanel = new BlogPanel(this);
        UserProfilePanel profilePanel = new UserProfilePanel(this);
        PostEditorPanel postEditorPanel = new PostEditorPanel(this);
        AdminPortalPanel adminPortalPanel = new AdminPortalPanel(this);
        
        // Add panels to card layout
        cardPanel.add(loginPanel, LOGIN_PANEL);
        cardPanel.add(registerPanel, REGISTER_PANEL);
        cardPanel.add(blogPanel, BLOG_PANEL);
        cardPanel.add(profilePanel, PROFILE_PANEL);
        cardPanel.add(postEditorPanel, POST_EDITOR_PANEL);
        cardPanel.add(adminPortalPanel, ADMIN_PORTAL_PANEL);
        
        // Add card panel to content panel
        contentPanel.add(cardPanel, BorderLayout.CENTER);
        
        // Add content panel to frame
        mainFrame.add(contentPanel);
        
        // Show login panel by default
        cardLayout.show(cardPanel, LOGIN_PANEL);
        
        // Add window listener to handle database connection close
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Close database connection
                try {
                    DatabaseUtil.closeConnection();
                    System.out.println("Database connection closed by application");
                    
                    // Shutdown analytics service
                    analyticsService.shutdown();
                    System.out.println("Analytics service shut down");
                } catch (Exception e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        });
        
        // Try to resume previous session
        tryResumeSession();
    }
    
    private void tryResumeSession() {
        // This would typically load a saved session token from a cookie/file
        // and then try to resume the session
        // For now, we'll just start with the login panel
    }
    
    public void showLoginPanel() {
        cardLayout.show(cardPanel, LOGIN_PANEL);
        // Track page view
        if (userController.isLoggedIn()) {
            analyticsService.trackEvent(AnalyticsService.EVENT_PAGE_VIEW, 
                userController.getCurrentUser(), "page", "login");
        } else {
            analyticsService.trackEvent(AnalyticsService.EVENT_PAGE_VIEW, 
                null, "page", "login");
        }
        appBar.updateUI(); // Update the app bar
    }
    
    public void showRegisterPanel() {
        cardLayout.show(cardPanel, REGISTER_PANEL);
        // Track page view
        if (userController.isLoggedIn()) {
            analyticsService.trackEvent(AnalyticsService.EVENT_PAGE_VIEW, 
                userController.getCurrentUser(), "page", "register");
        } else {
            analyticsService.trackEvent(AnalyticsService.EVENT_PAGE_VIEW, 
                null, "page", "register");
        }
        appBar.updateUI(); // Update the app bar
    }
    
    public void showBlogPanel() {
        cardLayout.show(cardPanel, BLOG_PANEL);
        // Refresh blog panel content
        BlogPanel blogPanel = (BlogPanel) findComponentByName(BLOG_PANEL);
        if (blogPanel != null) {
            blogPanel.refreshContent();
        }
        // Track page view
        if (userController.isLoggedIn()) {
            analyticsService.trackEvent(AnalyticsService.EVENT_PAGE_VIEW, 
                userController.getCurrentUser(), "page", "blog_list");
        } else {
            analyticsService.trackEvent(AnalyticsService.EVENT_PAGE_VIEW, 
                null, "page", "blog_list");
        }
        appBar.updateUI(); // Update the app bar
    }
    
    public void showProfilePanel() {
        UserProfilePanel profilePanel = (UserProfilePanel) findComponentByName(PROFILE_PANEL);
        if (profilePanel != null) {
            profilePanel.loadUserData();
            cardLayout.show(cardPanel, PROFILE_PANEL);
            appBar.updateUI(); // Update the app bar
        }
    }
    
    /**
     * Show the post editor panel for creating a new post
     */
    public void showNewPostEditor() {
        System.out.println("DEBUG: showNewPostEditor method called");
        
        // Create a new PostEditorPanel if it wasn't found
        PostEditorPanel editorPanel = (PostEditorPanel) findComponentByName(POST_EDITOR_PANEL);
        if (editorPanel == null) {
            System.out.println("DEBUG: PostEditorPanel not found, creating a new one");
            editorPanel = new PostEditorPanel(this);
            cardPanel.add(editorPanel, POST_EDITOR_PANEL);
        }
        
        System.out.println("DEBUG: Resetting for new post");
        editorPanel.resetForNewPost();
        System.out.println("DEBUG: Showing POST_EDITOR_PANEL card");
        cardLayout.show(cardPanel, POST_EDITOR_PANEL);
    }
    
    /**
     * Show the post editor panel for editing an existing post
     */
    public void showEditPostEditor(int postId) {
        System.out.println("DEBUG: showEditPostEditor called for postId=" + postId);
        Optional<BlogPost> postOpt = blogController.getPostById(postId);
        if (postOpt.isPresent()) {
            // Create a new PostEditorPanel if it wasn't found
            PostEditorPanel editorPanel = (PostEditorPanel) findComponentByName(POST_EDITOR_PANEL);
            if (editorPanel == null) {
                System.out.println("DEBUG: PostEditorPanel not found, creating a new one");
                editorPanel = new PostEditorPanel(this);
                cardPanel.add(editorPanel, POST_EDITOR_PANEL);
            }
            
            System.out.println("DEBUG: Loading post data for editing");
            editorPanel.loadPost(postOpt.get());
            System.out.println("DEBUG: Showing POST_EDITOR_PANEL card");
            cardLayout.show(cardPanel, POST_EDITOR_PANEL);
        } else {
            System.out.println("DEBUG: ERROR - Post not found with ID " + postId);
        }
    }
    
    /**
     * Show the full post view for a specific post
     */
    public void showPostView(int postId) {
        System.out.println("DEBUG: showPostView called with postId=" + postId);
        
        // Check if we already have a cached PostView for this post
        PostView postView = postViewCache.get(postId);
        
        if (postView == null) {
            System.out.println("DEBUG: Creating new PostView for postId=" + postId);
            // Create a new PostView
            postView = new PostView(this, postId);
            postViewCache.put(postId, postView);
            
            // Add it to the card panel
            String cardName = POST_VIEW_PANEL + "_" + postId;
            cardPanel.add(postView, cardName);
            System.out.println("DEBUG: Adding to card panel with name: " + cardName);
        } else {
            System.out.println("DEBUG: Reusing cached PostView for postId=" + postId);
            // Refresh the existing PostView
            postView.refreshContent();
        }
        
        // Show the PostView
        String cardName = POST_VIEW_PANEL + "_" + postId;
        cardLayout.show(cardPanel, cardName);
        System.out.println("DEBUG: Showing card: " + cardName);
        
        // Track post view event
        Map<String, Object> properties = new HashMap<>();
        properties.put("postId", postId);
        
        // Get post title if available
        Optional<BlogPost> post = blogController.getPostById(postId);
        post.ifPresent(p -> properties.put("title", p.getTitle()));
        
        analyticsService.trackEvent(AnalyticsService.EVENT_POST_VIEW, 
            userController.getCurrentUser(), properties);
    }
    
    /**
     * Show the admin portal
     */
    public void showAdminPortal() {
        if (!userController.isLoggedIn() || !userController.isCurrentUserAdmin()) {
            // If not logged in as admin, go to login panel instead
            showLoginPanel();
            return;
        }
        
        // Show the admin portal
        cardLayout.show(cardPanel, ADMIN_PORTAL_PANEL);
        
        // Refresh the admin portal content
        AdminPortalPanel adminPanel = (AdminPortalPanel) findComponentByName(ADMIN_PORTAL_PANEL);
        if (adminPanel != null) {
            adminPanel.refresh();
        }
        
        appBar.updateUI(); // Update the app bar
    }
    
    /**
     * Helper method to find a component in the card panel by name
     */
    private Component findComponentByName(String name) {
        CardLayout layout = (CardLayout) cardPanel.getLayout();
        for (Component comp : cardPanel.getComponents()) {
            Object constraint = null;
            try {
                // Using reflection to access the constraints
                java.lang.reflect.Field field = CardLayout.class.getDeclaredField("cons");
                field.setAccessible(true);
                java.util.Hashtable<Component, Object> cons = (java.util.Hashtable<Component, Object>) field.get(layout);
                constraint = cons.get(comp);
            } catch (Exception e) {
                System.err.println("Error accessing CardLayout constraints: " + e.getMessage());
            }
            
            if (name.equals(constraint)) {
                return comp;
            }
        }
        return null;
    }
    
    public boolean login(String username, String password) {
        boolean result = authService.login(username, password);
        if (result) {
            // Update app bar UI to show logout and possibly admin button
            appBar.logoutButton.setVisible(true);
            appBar.adminButton.setVisible(userController.isCurrentUserAdmin());
            appBar.repaint();
            
            // Track login event
            analyticsService.trackEvent(AnalyticsService.EVENT_LOGIN, 
                userController.getCurrentUser(), "method", "credentials");
        }
        return result;
    }
    
    public boolean register(String username, String password, String email, String fullName) {
        // Use AuthenticationService instead of UserController directly
        return authService.register(username, password, email, fullName);
    }
    
    public boolean register(String username, String password, String email, String fullName, 
                           String role, String bio, byte[] profileImageData) {
        // Use AuthenticationService instead of UserController directly
        return authService.register(username, password, email, fullName, role, bio, profileImageData);
    }
    
    public void logout() {
        // Track logout event before actually logging out
        if (userController.isLoggedIn()) {
            analyticsService.trackEvent(AnalyticsService.EVENT_LOGOUT, 
                userController.getCurrentUser(), "session_duration_seconds", 
                (System.currentTimeMillis() - authService.getLastLoginTime()) / 1000);
        }
        
        // Perform logout
        authService.logout();
        appBar.logoutButton.setVisible(false);
        appBar.adminButton.setVisible(false);
        appBar.repaint();
        
        // Return to login page
        showLoginPanel();
    }
    
    public UserController getUserController() {
        return userController;
    }
    
    public BlogController getBlogController() {
        return blogController;
    }
    
    public void display() {
        mainFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Initialize database tables
        DatabaseSetup.initializeDatabase();
        
        // Ensure admin user exists for testing
        DebugUtil.ensureAdminUserExists();
        
        // Create and display the application
        SwingUtilities.invokeLater(() -> {
            BlogApp app = new BlogApp();
            app.display();
            
            // Add a menu for generating analytics reports
            JFrame frame = app.mainFrame;
            JMenuBar menuBar = new JMenuBar();
            JMenu adminMenu = new JMenu("Admin");
            
            JMenuItem analyticsItem = new JMenuItem("View Analytics");
            analyticsItem.addActionListener(e -> {
                // Only allow admin users to view analytics
                if (app.userController.isLoggedIn() && app.userController.isCurrentUserAdmin()) {
                    AnalyticsReportUtil.printSimpleReport();
                } else {
                    JOptionPane.showMessageDialog(frame, 
                        "You must be logged in as an admin to view analytics", 
                        "Permission Denied", 
                        JOptionPane.WARNING_MESSAGE);
                }
            });
            
            adminMenu.add(analyticsItem);
            menuBar.add(adminMenu);
            frame.setJMenuBar(menuBar);
        });
    }
} 