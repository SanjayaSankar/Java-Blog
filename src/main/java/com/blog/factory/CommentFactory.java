package com.blog.factory;

import com.blog.model.Comment;
import com.blog.model.User;

import java.util.Date;

/**
 * Factory class for creating different types of comments
 * Implementation of the Factory Method pattern
 */
public class CommentFactory {
    
    /**
     * Create a new top-level comment
     * 
     * @param content The comment text
     * @param author The user who created the comment
     * @param postId The ID of the post to which this comment belongs
     * @return A new Comment object
     */
    public static Comment createTopLevelComment(String content, User author, int postId) {
        return new Comment(content, author, postId);
    }
    
    /**
     * Create a new reply comment (a comment that is a reply to another comment)
     * 
     * @param content The comment text
     * @param author The user who created the comment
     * @param postId The ID of the post to which this comment belongs
     * @param parentId The ID of the parent comment
     * @param level The nesting level of this comment (typically parent's level + 1)
     * @return A new Comment object
     */
    public static Comment createReplyComment(String content, User author, int postId, int parentId, int level) {
        return new Comment(content, author, postId, parentId, level);
    }
    
    /**
     * Create a comment from database data
     * 
     * @param id The comment ID
     * @param content The comment text
     * @param createdAt The date when the comment was created
     * @param author The user who created the comment
     * @param postId The ID of the post to which this comment belongs
     * @param hidden Whether the comment is hidden
     * @param parentId The ID of the parent comment (null for top-level comments)
     * @return A new Comment object
     */
    public static Comment createFromDatabase(int id, String content, Date createdAt, User author, int postId, boolean hidden, Integer parentId) {
        return new Comment(id, content, createdAt, author, postId, hidden, parentId);
    }
} 