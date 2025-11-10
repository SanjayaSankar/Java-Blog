package com.blog.view;

import com.blog.model.BlogPost;
import com.blog.model.Media;
import com.blog.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for creating and editing blog posts with rich text editing capabilities
 * and media attachment support.
 */
public class PostEditorPanel extends JPanel {
    private final BlogApp app;
    private BlogPost currentPost;
    private boolean isNewPost = true;
    
    // UI Components
    private JTextField titleField;
    private JTextPane contentPane;
    private JTextField tagsField;
    private JComboBox<String> statusComboBox;
    private JButton boldButton;
    private JButton italicButton;
    private JButton underlineButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton publishButton;
    private JButton uploadMediaButton;
    private JPanel mediaPreviewPanel;
    
    // Rich text styles
    private final StyledDocument doc;
    private final Style defaultStyle;
    private final Style boldStyle;
    private final Style italicStyle;
    private final Style underlineStyle;
    
    // Media attachments to add
    private final List<Media> newMediaAttachments;
    
    public PostEditorPanel(BlogApp app) {
        this.app = app;
        this.newMediaAttachments = new ArrayList<>();
        
        // Initialize text styles
        contentPane = new JTextPane();
        doc = contentPane.getStyledDocument();
        defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        
        boldStyle = doc.addStyle("bold", defaultStyle);
        StyleConstants.setBold(boldStyle, true);
        
        italicStyle = doc.addStyle("italic", defaultStyle);
        StyleConstants.setItalic(italicStyle, true);
        
        underlineStyle = doc.addStyle("underline", defaultStyle);
        StyleConstants.setUnderline(underlineStyle, true);
        
        initComponents();
    }
    
    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Post Editor");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        titleField = new JTextField(40);
        formPanel.add(titleField, gbc);
        
        // Tags field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tags:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        tagsField = new JTextField(40);
        formPanel.add(tagsField, gbc);
        
        // Status dropdown
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        String[] statuses = {"draft", "published", "archived"};
        statusComboBox = new JComboBox<>(statuses);
        formPanel.add(statusComboBox, gbc);
        
        // Toolbar for rich text editing
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        boldButton = new JButton("B");
        boldButton.setFont(new Font("Arial", Font.BOLD, 12));
        boldButton.addActionListener(e -> applyStyle(boldStyle));
        toolbarPanel.add(boldButton);
        
        italicButton = new JButton("I");
        italicButton.setFont(new Font("Arial", Font.ITALIC, 12));
        italicButton.addActionListener(e -> applyStyle(italicStyle));
        toolbarPanel.add(italicButton);
        
        underlineButton = new JButton("U");
        underlineButton.setFont(new Font("Arial", Font.PLAIN, 12));
        underlineButton.addActionListener(e -> applyStyle(underlineStyle));
        toolbarPanel.add(underlineButton);
        
        uploadMediaButton = new JButton("Upload Media");
        uploadMediaButton.addActionListener(e -> uploadMedia());
        toolbarPanel.add(uploadMediaButton);
        
        formPanel.add(toolbarPanel, gbc);
        
        // Content editor
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        contentPane.setPreferredSize(new Dimension(600, 400));
        JScrollPane scrollPane = new JScrollPane(contentPane);
        formPanel.add(scrollPane, gbc);
        
        // Media preview panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 0.1;
        
        mediaPreviewPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        mediaPreviewPanel.setBorder(BorderFactory.createTitledBorder("Media Attachments"));
        JScrollPane mediaScrollPane = new JScrollPane(mediaPreviewPanel);
        mediaScrollPane.setPreferredSize(new Dimension(600, 150));
        formPanel.add(mediaScrollPane, gbc);
        
        // Button panel
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Save as Draft");
        saveButton.addActionListener(e -> savePost(false));
        buttonPanel.add(saveButton);
        
        publishButton = new JButton("Publish");
        publishButton.addActionListener(e -> savePost(true));
        buttonPanel.add(publishButton);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> app.showBlogPanel());
        buttonPanel.add(cancelButton);
        
        formPanel.add(buttonPanel, gbc);
        
        contentPanel.add(formPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Load a post for editing
     */
    public void loadPost(BlogPost post) {
        this.currentPost = post;
        this.isNewPost = false;
        
        titleField.setText(post.getTitle());
        
        // Set content
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, post.getContent(), defaultStyle);
        } catch (BadLocationException e) {
            System.err.println("Error loading post content: " + e.getMessage());
        }
        
        // Set tags and status
        tagsField.setText(post.getTags());
        statusComboBox.setSelectedItem(post.getStatus());
        
        // Load media attachments
        if (post.getMediaAttachments() != null) {
            for (Media media : post.getMediaAttachments()) {
                addMediaPreview(media);
            }
        }
        
        saveButton.setText("Save Changes");
    }
    
    /**
     * Reset the editor for creating a new post
     */
    public void resetForNewPost() {
        System.out.println("DEBUG: resetForNewPost called");
        
        // Clear fields
        titleField.setText("");
        
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            System.err.println("Error clearing document: " + e.getMessage());
        }
        
        tagsField.setText("");
        statusComboBox.setSelectedItem("draft");
        mediaPreviewPanel.removeAll();
        mediaPreviewPanel.revalidate();
        mediaPreviewPanel.repaint();
        
        // Reset state
        isNewPost = true;
        currentPost = null;
        newMediaAttachments.clear();
        
        System.out.println("DEBUG: Editor reset complete");
    }
    
    /**
     * Apply a style to the selected text
     */
    private void applyStyle(Style style) {
        int start = contentPane.getSelectionStart();
        int end = contentPane.getSelectionEnd();
        
        if (start != end) {
            StyledDocument doc = contentPane.getStyledDocument();
            doc.setCharacterAttributes(start, end - start, style, false);
        }
    }
    
    /**
     * Upload media attachment
     */
    private void uploadMedia() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
        FileNameExtensionFilter docFilter = new FileNameExtensionFilter(
                "Document files", "pdf", "doc", "docx");
        
        fileChooser.addChoosableFileFilter(imageFilter);
        fileChooser.addChoosableFileFilter(docFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(imageFilter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try {
                // Read file data
                byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
                
                // Create Media object
                String fileName = file.getName();
                String fileType = app.getBlogController().getFileType(fileName);
                Media media = new Media(fileName, fileType, fileData);
                
                // For new post, store media to be added later
                if (isNewPost) {
                    newMediaAttachments.add(media);
                } else {
                    // For existing post, upload immediately
                    app.getBlogController().addMediaToPost(
                            currentPost.getId(), fileName, fileType, fileData, "");
                    
                    // Refresh post
                    currentPost = app.getBlogController().getPostById(currentPost.getId()).orElse(currentPost);
                }
                
                // Add to preview
                addMediaPreview(media);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error reading file: " + e.getMessage(),
                        "Upload Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Add a media preview to the media panel
     */
    private void addMediaPreview(Media media) {
        JPanel mediaItemPanel = new JPanel(new BorderLayout());
        mediaItemPanel.setBorder(BorderFactory.createEtchedBorder());
        mediaItemPanel.setPreferredSize(new Dimension(120, 120));
        
        if (media.isImage()) {
            try {
                // For locally added media
                if (media.getFileData() != null) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(media.getFileData()));
                    if (img != null) {
                        JLabel imageLabel = new JLabel(new ImageIcon(getScaledImage(img, 100, 100)));
                        mediaItemPanel.add(imageLabel, BorderLayout.CENTER);
                    }
                } else if (!isNewPost) {
                    // For existing media, load from database
                    app.getBlogController().getMediaById(media.getId()).ifPresent(fullMedia -> {
                        try {
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(fullMedia.getFileData()));
                            if (img != null) {
                                JLabel imageLabel = new JLabel(new ImageIcon(getScaledImage(img, 100, 100)));
                                mediaItemPanel.add(imageLabel, BorderLayout.CENTER);
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading image: " + e.getMessage());
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("Error loading image: " + e.getMessage());
                JLabel errorLabel = new JLabel("Image Preview Error");
                mediaItemPanel.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            // Document or other file type
            JLabel fileLabel = new JLabel(media.getFileName());
            fileLabel.setHorizontalAlignment(JLabel.CENTER);
            mediaItemPanel.add(fileLabel, BorderLayout.CENTER);
            
            JLabel fileTypeLabel = new JLabel(media.getFileType());
            fileTypeLabel.setHorizontalAlignment(JLabel.CENTER);
            fileTypeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            mediaItemPanel.add(fileTypeLabel, BorderLayout.SOUTH);
        }
        
        // Add remove button
        JButton removeButton = new JButton("×");
        removeButton.setFont(new Font("Arial", Font.BOLD, 10));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.addActionListener(e -> {
            if (!isNewPost && media.getId() > 0) {
                // Remove from database
                app.getBlogController().removeMediaFromPost(media.getId());
            } else {
                // Remove from pending uploads
                newMediaAttachments.remove(media);
            }
            
            // Remove from UI
            mediaPreviewPanel.remove(mediaItemPanel);
            mediaPreviewPanel.revalidate();
            mediaPreviewPanel.repaint();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(removeButton);
        mediaItemPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Add to preview panel
        mediaPreviewPanel.add(mediaItemPanel);
        mediaPreviewPanel.revalidate();
        mediaPreviewPanel.repaint();
    }
    
    /**
     * Scale image to fit preview
     */
    private Image getScaledImage(Image srcImg, int width, int height) {
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, width, height, null);
        g2.dispose();
        return resizedImg;
    }
    
    /**
     * Save the post
     */
    private void savePost(boolean publish) {
        String title = titleField.getText().trim();
        String content = contentPane.getText();
        String tags = tagsField.getText().trim();
        String status = publish ? "published" : statusComboBox.getSelectedItem().toString();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a title for the post",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter content for the post",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success;
        if (isNewPost) {
            // Create new post
            success = app.getBlogController().createPost(title, content, tags, status);
            
            if (success && !newMediaAttachments.isEmpty()) {
                // Get the newly created post ID
                List<BlogPost> userPosts = app.getBlogController().getCurrentUserPosts();
                if (!userPosts.isEmpty()) {
                    BlogPost newPost = userPosts.get(0); // Most recent post first
                    
                    // Add media attachments
                    for (Media media : newMediaAttachments) {
                        app.getBlogController().addMediaToPost(
                                newPost.getId(), media.getFileName(), media.getFileType(), 
                                media.getFileData(), media.getCaption());
                    }
                }
            }
        } else {
            // Update existing post
            success = app.getBlogController().updatePost(
                    currentPost.getId(), title, content, tags, status);
        }
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                    publish ? "Post published successfully!" : "Post saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            app.showBlogPanel();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save post. Please try again.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/**
 * Custom flow layout that wraps at the parent container's width
 */
class WrapLayout extends FlowLayout {
    public WrapLayout(int align) {
        super(align);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }
    
    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }
    
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            
            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
            
            int x = 0;
            int y = insets.top + vgap;
            int rowHeight = 0;
            
            int nmembers = target.getComponentCount();
            
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    
                    if (x > 0) {
                        x += hgap;
                    }
                    
                    if (x + d.width > maxWidth) {
                        x = 0;
                        y += vgap + rowHeight;
                        rowHeight = 0;
                    }
                    
                    x += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }
            
            return new Dimension(
                    insets.left + insets.right + x + hgap * 2,
                    insets.top + insets.bottom + y + rowHeight + vgap * 2);
        }
    }
} 