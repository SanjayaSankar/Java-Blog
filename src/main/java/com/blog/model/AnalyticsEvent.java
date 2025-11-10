package com.blog.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Analytics event class implementing the Prototype pattern
 * Used to track user interactions with the blog
 */
public class AnalyticsEvent implements Cloneable {
    private String eventType;
    private Date timestamp;
    private int userId;
    private String username;
    private Map<String, Object> properties;
    
    // Constructor for creating a new event template
    public AnalyticsEvent(String eventType) {
        this.eventType = eventType;
        this.timestamp = new Date();
        this.properties = new HashMap<>();
    }
    
    // Clone the event (Prototype pattern implementation)
    @Override
    public AnalyticsEvent clone() {
        try {
            AnalyticsEvent clone = (AnalyticsEvent) super.clone();
            clone.timestamp = new Date(); // Fresh timestamp
            clone.properties = new HashMap<>(this.properties); // Deep copy of properties
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone AnalyticsEvent", e);
        }
    }
    
    // Create a copy with user information
    public AnalyticsEvent withUser(User user) {
        AnalyticsEvent clone = this.clone();
        clone.userId = user != null ? user.getId() : -1;
        clone.username = user != null ? user.getUsername() : "anonymous";
        return clone;
    }
    
    // Add a property to the event
    public AnalyticsEvent withProperty(String key, Object value) {
        AnalyticsEvent clone = this.clone();
        clone.properties.put(key, value);
        return clone;
    }
    
    // Getters and setters
    public String getEventType() {
        return eventType;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");
        sb.append(eventType).append(" - ");
        sb.append("User: ").append(username).append(" (").append(userId).append(") ");
        
        if (!properties.isEmpty()) {
            sb.append("Properties: {");
            properties.forEach((key, value) -> 
                sb.append(key).append("=").append(value).append(", "));
            sb.delete(sb.length() - 2, sb.length());
            sb.append("}");
        }
        
        return sb.toString();
    }
} 