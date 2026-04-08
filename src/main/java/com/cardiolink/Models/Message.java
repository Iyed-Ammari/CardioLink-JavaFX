package com.cardiolink.Models;

public class Message {
    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private String date;
    private boolean isRead;
    private String classification;
    private boolean isPinned;
    private boolean isArchived;

    public Message() {};

    public Message(int id, int conversationId, int senderId, String content, String date, boolean isRead, String classification, boolean isPinned, boolean isArchived) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.date = date;
        this.isRead = isRead;
        this.classification = classification;
        this.isPinned = isPinned;
        this.isArchived = isArchived;
    }

    public Message(int conversationId, int senderId, String content, String date, boolean isRead, String classification, boolean isPinned, boolean isArchived) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.date = date;
        this.isRead = isRead;
        this.classification = classification;
        this.isPinned = isPinned;
        this.isArchived = isArchived;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", isRead=" + isRead +
                ", classification='" + classification + '\'' +
                ", isPinned=" + isPinned +
                ", isArchived=" + isArchived +
                '}';
    }
}
