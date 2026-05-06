package com.cardiolink.Models;

/**
 * Représente une réaction emoji d'un utilisateur sur un message.
 * Contrainte DB : unique_message_user_reaction (message_id, user_id, emoji)
 * → Un utilisateur ne peut mettre qu'UN emoji identique par message.
 */
public class MessageReaction {

    private int    id;
    private int    messageId;
    private int    userId;
    private String emoji;
    private String createdAt;

    public MessageReaction() {}

    public MessageReaction(int messageId, int userId, String emoji, String createdAt) {
        this.messageId = messageId;
        this.userId    = userId;
        this.emoji     = emoji;
        this.createdAt = createdAt;
    }

    public MessageReaction(int id, int messageId, int userId, String emoji, String createdAt) {
        this.id        = id;
        this.messageId = messageId;
        this.userId    = userId;
        this.emoji     = emoji;
        this.createdAt = createdAt;
    }

    /* ── Getters / Setters ──────────────────────────────────────────── */

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getMessageId()               { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public int getUserId()                  { return userId; }
    public void setUserId(int userId)       { this.userId = userId; }

    public String getEmoji()                { return emoji; }
    public void setEmoji(String emoji)      { this.emoji = emoji; }

    public String getCreatedAt()            { return createdAt; }
    public void setCreatedAt(String c)      { this.createdAt = c; }

    @Override
    public String toString() {
        return "MessageReaction{id=" + id + ", messageId=" + messageId
                + ", userId=" + userId + ", emoji='" + emoji + "'}";
    }
}
