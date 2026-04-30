package com.cardiolink.Services;

import java.net.URI;
import java.net.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModerationService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "http://127.0.0.1:8001/predict";

    public boolean isCommentSafe(String comment) {
        try {
            String jsonInput = String.format("{\"text\": \"%s\"}", comment.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode data = objectMapper.readTree(response.body());

            // RÉCUPÉRATION DIRECTE DU BOOLEAN DÉCIDÉ PAR PYTHON
            boolean isToxic = data.get("is_toxic").asBoolean();
            double score = data.get("score").asDouble();
            String label = data.get("label").asText();

            if (isToxic) {
                System.out.println("Modération CardioLink : BLOQUÉ [" + label + "] Score: " + score);
                return false; // Bloque le commentaire
            }

            return true; // Autorise le commentaire

        } catch (Exception e) {
            System.err.println("Erreur Modération (Python éteint ?) : " + e.getMessage());
            return true; // En cas de panne, on laisse passer pour ne pas bloquer l'appli
        }
    }
}