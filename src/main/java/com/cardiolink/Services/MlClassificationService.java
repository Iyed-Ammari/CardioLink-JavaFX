package com.cardiolink.Services;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service Java qui communique avec le serveur Flask ML (port 5000).
 *
 * Usage type dans ChatControllerAdvanced :
 * <pre>
 *   MlClassificationService ml = new MlClassificationService();
 *   if (ml.isAvailable()) {
 *       MlResult result = ml.analyzeMessage("douleur poitrine intense");
 *       System.out.println(result.getClassification()); // "URGENT"
 *   }
 * </pre>
 */
public class MlClassificationService {

    private static final String BASE_URL      = "http://127.0.0.1:5000";
    private static final int    TIMEOUT_MS    = 3000; // 3 secondes max

    /* ── Suggestions statiques de fallback (si Flask indisponible) ─── */
    private static final Map<String, List<String>> FALLBACK_SUGGESTIONS = Map.of(
        "URGENT",        Arrays.asList(
                "Je vous contacte immédiatement.",
                "Appelez le 15 si les symptômes s'aggravent.",
                "Je prends en charge votre urgence."
        ),
        "ADMINISTRATIF", Arrays.asList(
                "Votre document est bien enregistré.",
                "Je transmets votre demande.",
                "Je vous ferai parvenir les documents sous 24h."
        ),
        "NORMAL",        Arrays.asList(
                "Merci pour votre message.",
                "Je vous réponds rapidement.",
                "Prenez soin de vous."
        )
    );

    /* ════════════════════════════════════════════════════════════════
       MlResult — DTO résultat de classification
    ════════════════════════════════════════════════════════════════ */
    public static class MlResult {
        private final String classification;
        private final double confidence;
        private final String icon;
        private final int    priority;

        public MlResult(String classification, double confidence, String icon, int priority) {
            this.classification = classification;
            this.confidence     = confidence;
            this.icon           = icon;
            this.priority       = priority;
        }

        /** Fallback si Flask est hors ligne */
        public static MlResult fallback(String classification) {
            return new MlResult(classification, 0.0, iconFor(classification),
                    priorityFor(classification));
        }

        public String getClassification() { return classification; }
        public double getConfidence()      { return confidence; }
        public String getIcon()            { return icon; }
        public int    getPriority()        { return priority; }

        /** Badge pour l'UI : "🔴 URGENT (97%)" */
        public String getLabel() {
            return confidence > 0
                    ? icon + " " + classification + " (" + (int) confidence + "%)"
                    : icon + " " + classification;
        }

        private static String iconFor(String cls) {
            return switch (cls) {
                case "URGENT"        -> "🔴";
                case "ADMINISTRATIF" -> "📋";
                default              -> "💬";
            };
        }

        private static int priorityFor(String cls) {
            return switch (cls) {
                case "URGENT"        -> 1;
                case "ADMINISTRATIF" -> 2;
                default              -> 3;
            };
        }

        @Override
        public String toString() {
            return "MlResult{" + classification + ", conf=" + confidence + "%}";
        }
    }

    /* ════════════════════════════════════════════════════════════════
       isAvailable — vérifie que le service Flask répond
    ════════════════════════════════════════════════════════════════ */
    public boolean isAvailable() {
        try {
            HttpURLConnection conn = openConnection("/health", "GET");
            int code = conn.getResponseCode();
            System.out.println("[MlService] Health check: " + code);
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /* ════════════════════════════════════════════════════════════════
       analyzeMessage — POST /analyze_message
       Classifie un message et retourne classification + confiance.
       Retourne un fallback "NORMAL" si le service est indisponible.
    ════════════════════════════════════════════════════════════════ */
    public MlResult analyzeMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return MlResult.fallback("NORMAL");
        }

        try {
            JSONObject body = new JSONObject();
            body.put("content", content.trim());

            JSONObject response = postJson("/analyze_message", body);

            if (response != null && response.has("classification")) {
                return new MlResult(
                        response.getString("classification"),
                        response.optDouble("confidence", 0.0),
                        response.optString("icon", "💬"),
                        response.optInt("priority", 3)
                );
            }
        } catch (Exception e) {
            System.err.println("[MlService] analyzeMessage error: " + e.getMessage());
        }

        // Fallback : heuristique simple sur mots-clés
        return MlResult.fallback(heuristicClassify(content));
    }

    /* ════════════════════════════════════════════════════════════════
       getSuggestedReplies — POST /suggest_replies
       Retourne les suggestions de réponses depuis Flask.
       Si Flask est indisponible, retourne des suggestions statiques.
    ════════════════════════════════════════════════════════════════ */
    public List<String> getSuggestedReplies(String content) {
        if (content == null || content.trim().isEmpty()) {
            return FALLBACK_SUGGESTIONS.get("NORMAL");
        }

        try {
            JSONObject body = new JSONObject();
            body.put("content", content.trim());

            JSONObject response = postJson("/suggest_replies", body);

            if (response != null && response.has("suggestions")) {
                var arr = response.getJSONArray("suggestions");
                List<String> suggestions = new java.util.ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    suggestions.add(arr.getString(i));
                }
                return suggestions;
            }
        } catch (Exception e) {
            System.err.println("[MlService] getSuggestedReplies error: " + e.getMessage());
        }

        // Fallback statique
        String cls = heuristicClassify(content);
        return FALLBACK_SUGGESTIONS.getOrDefault(cls, FALLBACK_SUGGESTIONS.get("NORMAL"));
    }

    /* ════════════════════════════════════════════════════════════════
       checkToxicity — POST /check_toxicity
       Vérifie si un message contient des propos toxiques
    ════════════════════════════════════════════════════════════════ */
    public boolean checkToxicity(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("content", content.trim());

            JSONObject response = postJson("/check_toxicity", body);

            if (response != null && response.has("is_toxic")) {
                boolean isToxic = response.getBoolean("is_toxic");
                double score = response.optDouble("toxicity_score", 0.0);
                System.out.println("[MlService] Résultat toxicité pour '" + (content.length() > 20 ? content.substring(0, 20) + "..." : content) + "' : " + isToxic + " (Score: " + score + ")");
                return isToxic;
            } else {
                System.out.println("[MlService] Réponse invalide ou absente pour checkToxicity");
            }
        } catch (Exception e) {
            System.err.println("[MlService] checkToxicity error: " + e.getMessage());
        }

        // En cas d'erreur du service ML, on ne bloque pas le message par défaut
        return false;
    }

    /* ════════════════════════════════════════════════════════════════
       Heuristique rapide (fallback sans Flask)
    ════════════════════════════════════════════════════════════════ */
    private String heuristicClassify(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("urgent") || lower.contains("douleur")
                || lower.contains("essoufflement") || lower.contains("urgence")
                || lower.contains("syncope") || lower.contains("tachycardie")) {
            return "URGENT";
        }
        if (lower.contains("certificat") || lower.contains("ordonnance")
                || lower.contains("rendez-vous") || lower.contains("dossier")
                || lower.contains("administratif") || lower.contains("attestation")) {
            return "ADMINISTRATIF";
        }
        return "NORMAL";
    }

    /* ════════════════════════════════════════════════════════════════
       HTTP Helpers
    ════════════════════════════════════════════════════════════════ */
    private JSONObject postJson(String endpoint, JSONObject body) throws IOException {
        HttpURLConnection conn = openConnection(endpoint, "POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("[MlService] HTTP " + code + " sur " + endpoint);
            return null;
        }

        String raw = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        conn.disconnect();
        return new JSONObject(raw);
    }

    private HttpURLConnection openConnection(String endpoint, String method) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }
}
