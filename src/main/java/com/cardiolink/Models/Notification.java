package com.cardiolink.Models;

/**
 * Notification générée automatiquement à chaque nouveau message.
 * Relations : recipient_id → user, sender_id → user,
 *             conversation_id → conversation, message_id → message.
 */
public class Notification {

    private int     id;
    private String  content;
    private boolean isRead;
    private String  createdAt;
    private int     recipientId;
    private int     senderId;
    private int     conversationId;
    private int     messageId;

    public Notification() {}

    /** Constructeur complet (avec id) — pour les lectures DB */
    public Notification(int id, String content, boolean isRead, String createdAt,
                        int recipientId, int senderId, int conversationId, int messageId) {
        this.id             = id;
        this.content        = content;
        this.isRead         = isRead;
        this.createdAt      = createdAt;
        this.recipientId    = recipientId;
        this.senderId       = senderId;
        this.conversationId = conversationId;
        this.messageId      = messageId;
    }

    /** Constructeur sans id — pour les insertions */
    public Notification(String content, boolean isRead, String createdAt,
                        int recipientId, int senderId, int conversationId, int messageId) {
        this.content        = content;
        this.isRead         = isRead;
        this.createdAt      = createdAt;
        this.recipientId    = recipientId;
        this.senderId       = senderId;
        this.conversationId = conversationId;
        this.messageId      = messageId;
    }

    /* ── Getters / Setters ──────────────────────────────────────────── */

    public int     getId()                          { return id; }
    public void    setId(int id)                    { this.id = id; }

    public String  getContent()                     { return content; }
    public void    setContent(String content)       { this.content = content; }

    public boolean isRead()                         { return isRead; }
    public void    setRead(boolean read)            { this.isRead = read; }

    public String  getCreatedAt()                   { return createdAt; }
    public void    setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    public int     getRecipientId()                 { return recipientId; }
    public void    setRecipientId(int r)            { this.recipientId = r; }

    public int     getSenderId()                    { return senderId; }
    public void    setSenderId(int s)               { this.senderId = s; }

    public int     getConversationId()              { return conversationId; }
    public void    setConversationId(int c)         { this.conversationId = c; }

    public int     getMessageId()                   { return messageId; }
    public void    setMessageId(int m)              { this.messageId = m; }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", recipientId=" + recipientId
                + ", isRead=" + isRead + ", content='" + content + "'}";
    }
}
