package com.blog.view;

import javax.swing.*;
import java.awt.*;

/**
 * Standard implementation of the DialogFactory interface
 */
public class StandardDialogFactory implements DialogFactory {
    
    @Override
    public String showInputDialog(JComponent parent, String title, String promptText, String initialValue) {
        return JOptionPane.showInputDialog(parent, promptText, title, JOptionPane.QUESTION_MESSAGE);
    }
    
    @Override
    public boolean showConfirmDialog(JComponent parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            parent, 
            message, 
            title, 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    @Override
    public String showCommentReplyDialog(JComponent parent, String authorName) {
        // Create a custom dialog for the reply
        JDialog replyDialog = new JDialog(
            (JFrame)SwingUtilities.getWindowAncestor(parent),
            "Reply to Comment", 
            true
        );
        replyDialog.setLayout(new BorderLayout(10, 10));
        replyDialog.setSize(400, 200);
        replyDialog.setLocationRelativeTo(parent);
        
        // Add a title label
        JLabel titleLabel = new JLabel("Reply to " + authorName + ":");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        replyDialog.add(titleLabel, BorderLayout.NORTH);
        
        // Create text area with fixed width
        JTextArea replyTextArea = new JTextArea();
        replyTextArea.setLineWrap(true);
        replyTextArea.setWrapStyleWord(true);
        replyTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        replyTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JScrollPane scrollPane = new JScrollPane(replyTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        scrollPane.setPreferredSize(new Dimension(380, 100));
        replyDialog.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Result reference that will be set by button actions
        final String[] result = {null};
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            replyDialog.dispose();
        });
        
        JButton okButton = new JButton("Submit Reply");
        okButton.addActionListener(e -> {
            String text = replyTextArea.getText().trim();
            if (!text.isEmpty()) {
                result[0] = text;
                replyDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(
                    replyDialog,
                    "Reply cannot be empty.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        replyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default button and focus
        replyDialog.getRootPane().setDefaultButton(okButton);
        SwingUtilities.invokeLater(() -> replyTextArea.requestFocus());
        
        // Show the dialog
        replyDialog.setVisible(true);
        
        // Return the result (will be null if canceled)
        return result[0];
    }
} 