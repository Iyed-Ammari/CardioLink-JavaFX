package com.cardiolink.Test;

import com.cardiolink.Models.Post;
import com.cardiolink.Services.ServicePost;
import com.cardiolink.Models.Comment;
import com.cardiolink.Services.ServiceComment;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServiceComment serviceComment = new ServiceComment();
        ServicePost servicePost = new ServicePost();

        try {

            // =========================
            // AJOUT MANUEL
            // =========================
            System.out.println("=== AJOUT COMMENT ===");

            Comment c = new Comment();
            c.setContent("Commentaire test manuel");
            c.setCreated_at(LocalDateTime.now());

            // FK manuel pour test
            c.setUser_id(2);
            c.setPost_id(97);

            serviceComment.add(c);

            // =========================
            // AFFICHAGE
            // =========================
            System.out.println("\n=== LISTE COMMENTS ===");

            List<Comment> comments = serviceComment.getAll();

            for (Comment com : comments) {
                System.out.println(com);
            }

            // =========================
            // MODIFICATION
            // =========================
            System.out.println("\n=== MODIFICATION ===");

            if (!comments.isEmpty()) {

                Comment last = comments.get(comments.size() - 1);

                last.setContent("Comment modifié manuel");

                serviceComment.update(last);

                System.out.println("Comment modifié !");
            }

            // =========================
            // SUPPRESSION
            // =========================
            System.out.println("\n=== SUPPRESSION ===");

            if (!comments.isEmpty()) {

                int lastId = comments.get(comments.size() - 1).getId();

                serviceComment.delete(lastId);

                System.out.println("Comment supprimé !");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}