package com.blog.model;

import java.io.File;
import java.util.Date;

/**
 * Represents a media attachment for a blog post.
 */
public class Media {
    private int id;
    private int postId;
    private String fileName;
    private String fileType;      // MIME type e.g., "image/jpeg", "application/pdf"
    private byte[] fileData;
    private String filePath;      // Path where file is stored if not in DB
    private Date uploadedAt;
    private String caption;       // Optional caption/description

    public Media() {
        this.uploadedAt = new Date();
    }

    public Media(String fileName, String fileType, byte[] fileData) {
        this();
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
    }

    public Media(int id, int postId, String fileName, String fileType, 
                 byte[] fileData, String filePath, Date uploadedAt, String caption) {
        this.id = id;
        this.postId = postId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
        this.caption = caption;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * Determines if this is an image type media
     */
    public boolean isImage() {
        return fileType != null && fileType.startsWith("image/");
    }
    
    /**
     * Determines if this is a document type media
     */
    public boolean isDocument() {
        return fileType != null && 
               (fileType.startsWith("application/pdf") || 
                fileType.startsWith("application/msword") || 
                fileType.startsWith("application/vnd.openxmlformats-officedocument"));
    }
    
    /**
     * Get the file extension based on the file type
     */
    public String getFileExtension() {
        if (fileType == null) {
            return "";
        }
        
        switch (fileType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "application/pdf":
                return "pdf";
            case "application/msword":
                return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            default:
                // Extract extension from file name if possible
                if (fileName != null && fileName.contains(".")) {
                    return fileName.substring(fileName.lastIndexOf(".") + 1);
                }
                return "";
        }
    }
} 