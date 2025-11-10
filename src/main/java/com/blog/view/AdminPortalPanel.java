package com.blog.view;

import com.blog.controller.BlogController;
import com.blog.controller.UserController;
import com.blog.model.BlogPost;
import com.blog.model.Comment;
import com.blog.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPortalPanel extends JPanel {
    private final BlogApp app;
    private JTabbedPane tabbedPane;
    private JTable usersTable;
    private JTable postsTable;
    private DefaultTableModel usersTableModel;
    private DefaultTableModel postsTableModel;
    
    public AdminPortalPanel(BlogApp app) {
        this.app = app;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Admin Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton backButton = new JButton("Back to Blog");
        backButton.addActionListener(e -> app.showBlogPanel());
        headerPanel.add(backButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for different admin functions
        tabbedPane = new JTabbedPane();
        
        // Users Management Panel
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users", usersPanel);
        
        // Posts Management Panel
        JPanel postsPanel = createPostsPanel();
        tabbedPane.addTab("Posts", postsPanel);
        
        // Analytics Panel
        JPanel analyticsPanel = createAnalyticsPanel();
        tabbedPane.addTab("Analytics", analyticsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create table model for users
        String[] userColumns = {"ID", "Username", "Email", "Role", "Actions"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only allow editing of the Actions column
            }
        };
        
        usersTable = new JTable(usersTableModel);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Refresh Users");
        refreshButton.addActionListener(e -> loadUsers());
        actionPanel.add(refreshButton);
        
        JButton promoteButton = new JButton("Promote to Admin");
        promoteButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow >= 0) {
                int userId = Integer.parseInt(usersTable.getValueAt(selectedRow, 0).toString());
                String username = usersTable.getValueAt(selectedRow, 1).toString();
                promoteUserToAdmin(userId, username);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a user to promote", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        actionPanel.add(promoteButton);
        
        JButton promoteToAuthorButton = new JButton("Promote to Author");
        promoteToAuthorButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow >= 0) {
                int userId = Integer.parseInt(usersTable.getValueAt(selectedRow, 0).toString());
                String username = usersTable.getValueAt(selectedRow, 1).toString();
                promoteUserToAuthor(userId, username);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a user to promote", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        actionPanel.add(promoteToAuthorButton);
        
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPostsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create table model for posts
        String[] postColumns = {"ID", "Title", "Author", "Status", "Actions"};
        postsTableModel = new DefaultTableModel(postColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only allow editing of the Actions column
            }
        };
        
        postsTable = new JTable(postsTableModel);
        JScrollPane scrollPane = new JScrollPane(postsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Refresh Posts");
        refreshButton.addActionListener(e -> loadPosts());
        actionPanel.add(refreshButton);
        
        JButton viewButton = new JButton("View Post");
        viewButton.addActionListener(e -> {
            int selectedRow = postsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int postId = Integer.parseInt(postsTable.getValueAt(selectedRow, 0).toString());
                app.showPostView(postId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a post to view", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        actionPanel.add(viewButton);
        
        JButton deleteButton = new JButton("Delete Post");
        deleteButton.addActionListener(e -> {
            int selectedRow = postsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int postId = Integer.parseInt(postsTable.getValueAt(selectedRow, 0).toString());
                String title = postsTable.getValueAt(selectedRow, 1).toString();
                deletePost(postId, title);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a post to delete", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        actionPanel.add(deleteButton);
        
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the analytics panel to display post metrics
     */
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Blog Post Analytics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create a tabbed pane for different analytics views
        JTabbedPane analyticsTabs = new JTabbedPane();
        
        // Most Viewed Posts Table
        JPanel popularPostsPanel = new JPanel(new BorderLayout());
        
        String[] popularColumns = {"ID", "Title", "Author", "Views", "Likes", "Comments", "Actions"};
        DefaultTableModel popularPostsModel = new DefaultTableModel(popularColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only allow editing of the Actions column
            }
        };
        
        JTable popularPostsTable = new JTable(popularPostsModel);
        JScrollPane popularScrollPane = new JScrollPane(popularPostsTable);
        popularPostsPanel.add(popularScrollPane, BorderLayout.CENTER);
        
        // Buttons panel for popular posts
        JPanel popularButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshPopularButton = new JButton("Refresh Analytics");
        refreshPopularButton.addActionListener(e -> loadAnalytics(popularPostsModel));
        popularButtonsPanel.add(refreshPopularButton);
        
        JButton viewPostButton = new JButton("View Post");
        viewPostButton.addActionListener(e -> {
            int selectedRow = popularPostsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int postId = Integer.parseInt(popularPostsTable.getValueAt(selectedRow, 0).toString());
                app.showPostView(postId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a post to view", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        popularButtonsPanel.add(viewPostButton);
        
        popularPostsPanel.add(popularButtonsPanel, BorderLayout.SOUTH);
        
        analyticsTabs.addTab("Most Viewed Posts", popularPostsPanel);
        
        // Add the tabs to the panel
        panel.add(analyticsTabs, BorderLayout.CENTER);
        
        // Initial data load
        loadAnalytics(popularPostsModel);
        
        return panel;
    }
    
    /**
     * Load analytics data for the popular posts table
     */
    private void loadAnalytics(DefaultTableModel model) {
        // Clear the table
        model.setRowCount(0);
        
        // Get posts sorted by popularity
        List<BlogPost> popularPosts = app.getBlogController().getPopularPosts();
        
        // Add posts to the table
        for (BlogPost post : popularPosts) {
            Object[] row = {
                post.getId(),
                post.getTitle(),
                post.getAuthor().getUsername(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                ""  // Actions column
            };
            model.addRow(row);
        }
    }
    
    private void loadUsers() {
        // Clear the table
        usersTableModel.setRowCount(0);
        
        // Get all users from the controller
        List<User> users = app.getUserController().getAllUsers();
        
        // Add users to the table
        for (User user : users) {
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                ""  // Actions column
            };
            usersTableModel.addRow(row);
        }
    }
    
    private void promoteUserToAdmin(int userId, String username) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to promote " + username + " to admin?",
            "Confirm Promotion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = app.getUserController().promoteToAdmin(userId);
            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    username + " has been promoted to admin",
                    "Promotion Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadUsers();  // Refresh the user list
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to promote user. Please try again.",
                    "Promotion Failed",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void promoteUserToAuthor(int userId, String username) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to promote " + username + " to author?",
            "Confirm Promotion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = app.getUserController().promoteToAuthor(userId);
            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    username + " has been promoted to author",
                    "Promotion Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadUsers();  // Refresh the user list
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to promote user. Please try again.",
                    "Promotion Failed",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void loadPosts() {
        // Clear the table
        postsTableModel.setRowCount(0);
        
        // Get all posts from the controller (admin view shows all posts)
        List<BlogPost> posts = app.getBlogController().getAllPostsForAdmin();
        
        // Add posts to the table
        for (BlogPost post : posts) {
            Object[] row = {
                post.getId(),
                post.getTitle(),
                post.getAuthor().getUsername(),
                post.getStatus(),
                ""  // Actions column
            };
            postsTableModel.addRow(row);
        }
    }
    
    private void deletePost(int postId, String title) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the post \"" + title + "\"?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = app.getBlogController().deletePost(postId);
            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Post \"" + title + "\" has been deleted",
                    "Deletion Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadPosts();  // Refresh the post list
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete post. Please try again.",
                    "Deletion Failed",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    public void refresh() {
        loadUsers();
        loadPosts();
        
        // Refresh analytics if that tab exists and is visible
        if (tabbedPane.getTabCount() > 2) {
            JPanel analyticsPanel = (JPanel) tabbedPane.getComponentAt(2);
            JTabbedPane analyticsTabs = (JTabbedPane) analyticsPanel.getComponent(1);
            JPanel popularPostsPanel = (JPanel) analyticsTabs.getComponentAt(0);
            JScrollPane scrollPane = (JScrollPane) popularPostsPanel.getComponent(0);
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            loadAnalytics(model);
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        refresh();
    }
} 