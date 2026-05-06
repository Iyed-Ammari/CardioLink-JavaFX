package com.cardiolink.Services; // Adapte selon ton package

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.cardiolink.Models.Post;
import java.util.Map;
import java.util.HashMap;
public class RecommendationService {
    private final String API_URL = "http://127.0.0.1:8007"; // Ton nouveau port
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // 1. Appelle l'IA pour transformer un texte en vecteur numérique
    public List<Double> getVector(String text) {
        try {
            // Nettoyage sommaire du texte pour le JSON
            String cleanText = text.replace("\"", "\\\"").replace("\n", " ");
            String jsonInput = "{\"text\": \"" + cleanText + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/vectorize"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Extraction de la liste "vector" depuis le JSON de retour
            JsonNode rootNode = mapper.readTree(response.body());
            return mapper.convertValue(rootNode.get("vector"), new TypeReference<List<Double>>() {});

        } catch (Exception e) {
            System.err.println("Erreur Vectorisation IA: " + e.getMessage());
            return null;
        }
    }

    // 2. Calcule le nouveau profil de l'utilisateur (Logique mathématique)
    public List<Double> updateInterest(List<Double> currentInterest, List<Double> postVector, boolean isLike) {
        // Si l'utilisateur n'a pas encore de profil, on l'initialise avec des zéros
        if (currentInterest == null || currentInterest.isEmpty()) {
            currentInterest = new ArrayList<>();
            for (int i = 0; i < postVector.size(); i++) currentInterest.add(0.0);
        }

        List<Double> newInterest = new ArrayList<>();
        // On pénalise plus le dislike (-1.5) qu'on ne récompense le like (1.0)
        // pour que le système soit très réactif à ce que l'utilisateur déteste.
        double factor = isLike ? 1.0 : -1.5;

        for (int i = 0; i < postVector.size(); i++) {
            double updatedValue = currentInterest.get(i) + (postVector.get(i) * factor);
            newInterest.add(updatedValue);
        }
        return newInterest;
    }
    public List<Post> rankPosts(List<Double> userVector, List<Post> allPosts) {
        try {
            // 1. Préparer l'objet JSON pour Python (Utilisation de .put() pour une Map)
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_vector", userVector); // ⭐ Corrigé : .put() au lieu de .add()
            payload.put("posts", allPosts);       // ⭐ Corrigé : .put() au lieu de .add()

            String jsonInput = mapper.writeValueAsString(payload);

            // 2. Préparer la requête vers l'API Python (Port 8007)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/rank_posts"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            // 3. Envoyer la requête
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Vérifier si la réponse est OK (Code 200)
            if (response.statusCode() == 200) {
                // Retourne la liste triée renvoyée par Python
                return mapper.readValue(response.body(), new TypeReference<List<Post>>(){});
            } else {
                System.err.println("Erreur Serveur Python : Code " + response.statusCode());
                return allPosts;
            }

        } catch (Exception e) {
            System.err.println("Erreur Ranking IA: " + e.getMessage());
            // En cas de crash (ex: serveur Python éteint), on retourne la liste originale pour ne pas bloquer l'appli
            return allPosts;
        }
    }
}