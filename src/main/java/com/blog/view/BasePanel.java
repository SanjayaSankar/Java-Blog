package com.blog.view;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract base class for all panels in the application
 * Provides common functionality and enforces a consistent structure
 */
public abstract class BasePanel extends JPanel {
    protected final BlogApp app;
    
    /**
     * Constructor for the base panel
     * @param app The main application instance
     */
    public BasePanel(BlogApp app) {
        this.app = app;
        setLayout(new BorderLayout());
    }
    
    /**
     * Initialize all UI components
     * This method must be implemented by all subclasses
     */
    protected abstract void initComponents();
    
    /**
     * Refresh the content of the panel
     * Default implementation just calls initComponents and revalidates
     */
    public void refreshContent() {
        removeAll();
        initComponents();
        revalidate();
        repaint();
    }
    
    /**
     * Show a message dialog
     * @param message The message to display
     * @param title The dialog title
     * @param messageType The type of message (from JOptionPane constants)
     */
    protected void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    /**
     * Show an error message dialog
     * @param message The error message to display
     */
    protected void showErrorMessage(String message) {
        showMessage(message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show an info message dialog
     * @param message The info message to display
     */
    protected void showInfoMessage(String message) {
        showMessage(message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
} 