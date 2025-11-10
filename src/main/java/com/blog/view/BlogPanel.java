package com.blog.view;

import com.blog.model.BlogPost;
import com.blog.model.Media;
import com.blog.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class BlogPanel extends JPanel {
    private final BlogApp app;
    private JPanel postsPanel;
    private JPanel sidebarPanel;
    private JLabel welcomeLabel;
    private JButton newPostButton;
    private JButton myPostsButton;
    private JButton allPostsButton;
    private JTextField searchField;
    private JComboBox<String> tagFilterComboBox;
    private JLabel statusLabel;
    
    // View state
    private boolean showingUserPosts = false;
    private String currentTag = null;
    private String currentSearch = null;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BlogPanel(BlogApp app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        welcomeLabel = new JLabel("Blog Posts");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel with sidebar and posts area
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Sidebar
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        sidebarPanel.setBackground(new Color(240, 240, 245));
        sidebarPanel.setPreferredSize(new Dimension(200, 500));
        
        newPostButton = new JButton("Create New Post");
        newPostButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPostButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, newPostButton.getPreferredSize().height));
        newPostButton.addActionListener(e -> {
            System.out.println("DEBUG: Create New Post button clicked");
            app.showNewPostEditor();
        });
        sidebarPanel.add(newPostButton);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        myPostsButton = new JButton("My Posts");
        myPostsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        myPostsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, myPostsButton.getPreferredSize().height));
        myPostsButton.addActionListener(e -> {
            showingUserPosts = true;
            currentTag = null;
            refreshContent();
        });
        sidebarPanel.add(myPostsButton);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        allPostsButton = new JButton("All Posts");
        allPostsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        allPostsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, allPostsButton.getPreferredSize().height));
        allPostsButton.addActionListener(e -> {
            showingUserPosts = false;
            currentTag = null;
            refreshContent();
        });
        sidebarPanel.add(allPostsButton);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel tagsLabel = new JLabel("Filter by Tag:");
        tagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(tagsLabel);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        tagFilterComboBox = new JComboBox<>(new String[]{"All Tags"});
        tagFilterComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagFilterComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, tagFilterComboBox.getPreferredSize().height));
        tagFilterComboBox.addActionListener(e -> {
            String selected = (String) tagFilterComboBox.getSelectedItem();
            if (selected != null && !"All Tags".equals(selected)) {
                currentTag = selected;
            } else {
                currentTag = null;
            }
            refreshContent();
        });
        sidebarPanel.add(tagFilterComboBox);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel searchLabel = new JLabel("Search Posts:");
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(searchLabel);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        searchField = new JTextField();
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height));
        sidebarPanel.add(searchField);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchButton.getPreferredSize().height));
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                currentSearch = null;
            } else {
                currentSearch = searchText;
                // Reset other filters
                showingUserPosts = false;
                currentTag = null;
            }
            refreshContent();
        });
        sidebarPanel.add(searchButton);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton clearSearchButton = new JButton("Clear Search");
        clearSearchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearSearchButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, clearSearchButton.getPreferredSize().height));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            currentSearch = null;
            refreshContent();
        });
        sidebarPanel.add(clearSearchButton);
        
        // Also add action listener to the search field for pressing Enter
        searchField.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                currentSearch = null;
            } else {
                currentSearch = searchText;
                // Reset other filters
                showingUserPosts = false;
                currentTag = null;
            }
            refreshContent();
        });
        
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Posts area
        JPanel postsContainer = new JPanel(new BorderLayout());
        postsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add status label at the top of posts container
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        postsContainer.add(statusLabel, BorderLayout.NORTH);
        
        postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(postsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        postsContainer.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(postsContainer, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Refresh the content of the blog panel
     */
    public void refreshContent() {
        updateWelcomeMessage();
        updateTags();
        
        // Clear existing posts
        postsPanel.removeAll();
        
        // Update status label
        if (currentSearch != null) {
            statusLabel.setText("Search results for: \"" + currentSearch + "\"");
        } else if (currentTag != null) {
            statusLabel.setText("Showing posts with tag: " + currentTag);
        } else if (showingUserPosts) {
            statusLabel.setText("Showing your posts");
        } else {
            statusLabel.setText("Showing all published posts");
        }
        
        List<BlogPost> posts;
        
        if (currentSearch != null) {
            // Search mode takes priority
            posts = app.getBlogController().searchPosts(currentSearch);
            if (posts.isEmpty()) {
                JLabel noResultsLabel = new JLabel("No results found for: " + currentSearch);
                noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                postsPanel.add(noResultsLabel);
                postsPanel.revalidate();
                postsPanel.repaint();
                return;
            }
        } else if (currentTag != null) {
            // Filter by tag
            posts = app.getBlogController().getPostsByTag(currentTag);
        } else if (showingUserPosts) {
            // Show user's posts
            posts = app.getBlogController().getCurrentUserPosts();
        } else {
            // Show all published posts
            posts = app.getBlogController().getAllPosts();
        }
        
        if (posts.isEmpty()) {
            JLabel emptyLabel = new JLabel("No posts found");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            postsPanel.add(emptyLabel);
        } else {
            for (BlogPost post : posts) {
                postsPanel.add(createPostPanel(post));
                postsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        postsPanel.revalidate();
        postsPanel.repaint();
    }
    
    /**
     * Create a panel for displaying a blog post
     */
    private JPanel createPostPanel(BlogPost post) {
        JPanel postPanel = new JPanel(new BorderLayout());
        postPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        postPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // Post header with title and author info
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String dateStr = dateFormat.format(post.getCreatedAt());
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        postPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Post content
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Truncate content for preview
        String contentPreview = post.getContent();
        if (contentPreview.length() > 200) {
            contentPreview = contentPreview.substring(0, 200) + "...";
        }
        
        JTextArea contentArea = new JTextArea(contentPreview);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(postPanel.getBackground());
        contentArea.setRows(3);
        
        contentPanel.add(contentArea, BorderLayout.CENTER);
        
        // Add thumbnail of first image if available
        if (post.getMediaAttachments() != null && !post.getMediaAttachments().isEmpty()) {
            for (Media media : post.getMediaAttachments()) {
                if (media.isImage()) {
                    try {
                        app.getBlogController().getMediaById(media.getId()).ifPresent(fullMedia -> {
                            try {
                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(fullMedia.getFileData()));
                                if (img != null) {
                                    int thumbHeight = 80;
                                    int thumbWidth = (int)(img.getWidth() * ((double)thumbHeight / img.getHeight()));
                                    
                                    Image scaledImg = img.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
                                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                                    imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
                                    contentPanel.add(imageLabel, BorderLayout.WEST);
                                }
                            } catch (IOException e) {
                                System.err.println("Error creating thumbnail: " + e.getMessage());
                            }
                        });
                        break; // Only show the first image
                    } catch (Exception e) {
                        System.err.println("Error creating thumbnail: " + e.getMessage());
                    }
                }
            }
        }
        
        postPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Footer with author info, tags, and like/comment counts
        JPanel footerPanel = new JPanel(new BorderLayout());
        
        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftFooter.setOpaque(false);
        
        JLabel authorLabel = new JLabel("Author: " + post.getAuthor().getUsername());
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        leftFooter.add(authorLabel);
        
        // Add like and comment counts
        leftFooter.add(Box.createHorizontalStrut(10));
        int likeCount = app.getBlogController().getLikeCount(post.getId());
        int dislikeCount = app.getBlogController().getDislikeCount(post.getId());
        int commentCount = app.getBlogController().getCommentCount(post.getId());
        
        JLabel likesLabel = new JLabel("👍 " + likeCount);
        likesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        leftFooter.add(likesLabel);
        
        leftFooter.add(Box.createHorizontalStrut(5));
        JLabel dislikesLabel = new JLabel("👎 " + dislikeCount);
        dislikesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        leftFooter.add(dislikesLabel);
        
        leftFooter.add(Box.createHorizontalStrut(5));
        JLabel commentsLabel = new JLabel("💬 " + commentCount);
        commentsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        leftFooter.add(commentsLabel);
        
        footerPanel.add(leftFooter, BorderLayout.WEST);
        
        // Add tags if available
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            JLabel tagsLabel = new JLabel("Tags: " + post.getTags());
            tagsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            footerPanel.add(tagsLabel, BorderLayout.EAST);
        }
        
        // Add view details button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton viewButton = new JButton("View Details, Like & Comment");
        viewButton.addActionListener(e -> {
            System.out.println("DEBUG: View details button clicked for post: " + post.getTitle() + " (ID: " + post.getId() + ")");
            app.showPostView(post.getId());
        });
        bottomPanel.add(viewButton);
        
        // Add action buttons to bottom panel if the post belongs to current user or if user is admin
        if (app.getUserController().isLoggedIn() && 
            (app.getUserController().getCurrentUser().getId() == post.getAuthor().getId() ||
             app.getUserController().getCurrentUser().isAdmin())) {
            
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> app.showEditPostEditor(post.getId()));
            bottomPanel.add(editButton);
            
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this, 
                        "Are you sure you want to delete this post?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = app.getBlogController().deletePost(post.getId());
                    if (success) {
                        refreshContent();
                    } else {
                        JOptionPane.showMessageDialog(
                                this, 
                                "Failed to delete post", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            bottomPanel.add(deleteButton);
        }
        
        // Add the bottom panel to the footer
        footerPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        postPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Add hover effect and click listener to open full post
        postPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add mouse listeners for hover effect
        postPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                postPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                postPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Show full post view
                System.out.println("DEBUG: Post clicked by mouse: " + post.getTitle() + " (ID: " + post.getId() + ")");
                app.showPostView(post.getId());
            }
        });
        
        return postPanel;
    }
    
    /**
     * Update the welcome message with user info
     */
    private void updateWelcomeMessage() {
        if (app.getUserController().isLoggedIn()) {
            User currentUser = app.getUserController().getCurrentUser();
            welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        } else {
            welcomeLabel.setText("Welcome to the Blog");
        }
    }
    
    /**
     * Update available tags in the tag filter
     */
    private void updateTags() {
        tagFilterComboBox.removeAllItems();
        tagFilterComboBox.addItem("All Tags");
        
        // Extract unique tags from all posts
        List<BlogPost> allPosts = app.getBlogController().getAllPosts();
        for (BlogPost post : allPosts) {
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                for (String tag : post.getTagsArray()) {
                    boolean exists = false;
                    for (int i = 0; i < tagFilterComboBox.getItemCount(); i++) {
                        if (tag.equals(tagFilterComboBox.getItemAt(i))) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        tagFilterComboBox.addItem(tag);
                    }
                }
            }
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        refreshContent();
    }
} 