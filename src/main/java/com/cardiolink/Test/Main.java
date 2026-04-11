package com.cardiolink.Test;

import com.cardiolink.Models.Post;
import com.cardiolink.Services.ServicePost;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ServicePost servicePost = new ServicePost();

        try {

            // =========================
            // 1. AJOUT
            // =========================
            System.out.println("=== AJOUT ===");

            Post p = new Post();
            p.setTitle("Post test CRUD");
            p.setContent("Contenu test CRUD");
            p.setCreated_at(LocalDateTime.now());
            p.setUser_id(2);

            servicePost.add(p);

            System.out.println("Post ajouté !");

            // =========================
            // 2. AFFICHAGE
            // =========================
            System.out.println("\n=== LISTE DES POSTS ===");

            List<Post> posts = servicePost.getAll();

            for (Post post : posts) {
                System.out.println(post);
            }

            // =========================
            // 3. SUPPRESSION
            // =========================
            System.out.println("\n=== SUPPRESSION ===");

            if (!posts.isEmpty()) {

                int lastId = posts.get(posts.size() - 1).getId();

                servicePost.delete(lastId);

                System.out.println("Post supprimé avec id = " + lastId);
            }

            // =========================
            // 4. AFFICHAGE APRÈS SUPPRESSION
            // =========================
            System.out.println("\n=== APRÈS SUPPRESSION ===");

            List<Post> postsAfter = servicePost.getAll();

            for (Post post : postsAfter) {
                System.out.println(post);
            }

            // =========================
            // 5. MODIFICATION
            // =========================
            System.out.println("\n=== MODIFICATION ===");

            List<Post> postsForUpdate = servicePost.getAll();

            if (!postsForUpdate.isEmpty()) {

                Post lastPost = postsForUpdate.get(postsForUpdate.size() - 1);

                lastPost.setTitle("Titre modifié");
                lastPost.setContent("Contenu modifié");
                lastPost.setCreated_at(LocalDateTime.now());
                lastPost.setUser_id(2);

                servicePost.update(lastPost);

                System.out.println("Post modifié avec succès !");
            }

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}