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

            String label = data.get("label").asText();
            double score = data.get("score").asDouble();

            // LOGIQUE DE BLOCAGE (Identique à ton Symfony)
            // On bloque si c'est "negative" et que le score dépasse 0.60
            if ("negative".equals(label) && score > 0.60) {
                System.out.println("Modération JavaFX : Contenu toxique détecté !");
                return false; // Bloque
            }
            return true; // Autorise

        } catch (Exception e) {
            e.printStackTrace();
            return true; // Si Python est éteint, on laisse passer (comme dans Symfony)
        }
    }
}