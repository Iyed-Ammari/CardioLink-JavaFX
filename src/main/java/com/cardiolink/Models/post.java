package com.cardiolink.Models;

import java.time.LocalDateTime;

public class Post {

    private int id;
    private String title;
    private String content;
    private LocalDateTime created_at;
    private int user_id;
    private int likes;
    private String image;
    private String embedding;

    // constructeur vide
    public Post() {
    }

    // constructeur complet
    public Post(int id, String title, String content, LocalDateTime created_at,
                int user_id, int likes, String image, String embedding) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.created_at = created_at;
        this.user_id = user_id;
        this.likes = likes;
        this.image = image;
        this.embedding = embedding;
    }

    // getters setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", created_at=" + created_at +
                ", user_id=" + user_id +
                ", likes=" + likes +
                ", image='" + image + '\'' +
                ", embedding='" + embedding + '\'' +
                '}';
    }
}