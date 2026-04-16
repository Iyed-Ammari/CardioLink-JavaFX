package com.cardiolink.Models;

import java.time.LocalDateTime;

public class Comment {

    private int id;
    private String content;
    private LocalDateTime created_at;
    private int post_id;
    private int user_id;

    // constructeur vide
    public Comment() {
    }

    // constructeur sans id
    public Comment(String content, LocalDateTime created_at, int post_id, int user_id) {
        this.content = content;
        this.created_at = created_at;
        this.post_id = post_id;
        this.user_id = user_id;
    }

    // constructeur complet
    public Comment(int id, String content, LocalDateTime created_at, int post_id, int user_id) {
        this.id = id;
        this.content = content;
        this.created_at = created_at;
        this.post_id = post_id;
        this.user_id = user_id;
    }

    // getters setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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


    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", created_at=" + created_at +
                ", post_id=" + post_id +
                ", user_id=" + user_id +
                '}';
    }
}