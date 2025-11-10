package com.blog.view;

import javax.swing.*;

/**
 * Interface for creating standard dialog boxes in the application
 */
public interface DialogFactory {
    /**
     * Create and show a custom dialog to get user input
     * 
     * @param parent The parent component
     * @param title Dialog title
     * @param promptText Text to display above the input field
     * @param initialValue Initial value for the input field
     * @return The user's input, or null if canceled
     */
    String showInputDialog(JComponent parent, String title, String promptText, String initialValue);
    
    /**
     * Show a confirmation dialog
     * 
     * @param parent The parent component
     * @param message The message to display
     * @param title The dialog title
     * @return True if confirmed, false otherwise
     */
    boolean showConfirmDialog(JComponent parent, String message, String title);
    
    /**
     * Show a standard reply dialog for comments
     * 
     * @param parent The parent component
     * @param authorName Name of the author being replied to
     * @return The reply text, or null if canceled
     */
    String showCommentReplyDialog(JComponent parent, String authorName);
} 