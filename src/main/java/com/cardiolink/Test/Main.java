package com.cardiolink.Test;

import com.cardiolink.Models.Comment;
import com.cardiolink.Models.Post;
import com.cardiolink.Services.ServiceComment;
import com.cardiolink.Services.ServicePost;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ServicePost servicePost = new ServicePost();
        ServiceComment serviceComment = new ServiceComment();

        try {

            // =========================
            // AJOUT POST
            // =========================
            System.out.println("=== AJOUT POST ===");

            Post p = new Post();
            p.setTitle("Post test");
            p.setContent("Contenu test");
            p.setCreated_at(LocalDateTime.now());
            p.setUser_id(2);

            servicePost.add(p);


            // RELOAD POSTS
            List<Post> posts = servicePost.getAll();
            Post lastPost = posts.get(posts.size() - 1);


            // =========================
            // AJOUT COMMENT
            // =========================
            System.out.println("\n=== AJOUT COMMENT ===");

            Comment c = new Comment();
            c.setContent("Comment test");
            c.setCreated_at(LocalDateTime.now());
            c.setUser_id(2);
            c.setPost_id(lastPost.getId());

            serviceComment.add(c);


            // =========================
            // MODIFICATION POST
            // =========================
            System.out.println("\n=== UPDATE POST ===");

            lastPost.setTitle("Post modifié");
            lastPost.setContent("Contenu modifié");

            servicePost.update(lastPost);


            // =========================
            // MODIFICATION COMMENT
            // =========================
            List<Comment> comments = serviceComment.getAll();
            Comment lastComment = comments.get(comments.size() - 1);

            System.out.println("\n=== UPDATE COMMENT ===");

            lastComment.setContent("Comment modifié");

            serviceComment.update(lastComment);


            // =========================
            // SUPPRESSION COMMENT
            // =========================
            System.out.println("\n=== DELETE COMMENT ===");

            serviceComment.delete(lastComment);


            // =========================
            // SUPPRESSION POST
            // =========================
            System.out.println("\n=== DELETE POST ===");

            servicePost.delete(lastPost);


            // =========================
            // AFFICHAGE FINAL
            // =========================
            System.out.println("\n=== POSTS RESTANTS ===");

            for (Post post : servicePost.getAll()) {
                System.out.println(post);
            }

            System.out.println("\n=== COMMENTS RESTANTS ===");

            for (Comment comment : serviceComment.getAll()) {
                System.out.println(comment);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}