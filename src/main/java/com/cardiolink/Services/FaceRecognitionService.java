package com.cardiolink.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.Base64;

public class FaceRecognitionService {

    private static final String HF_TOKEN =
            "hf_VOTRE_TOKEN_HUGGINGFACE";

    // ── URL correcte du modèle de comparaison de visages ─────
    private static final String HF_API_URL =
            "https://api-inference.huggingface.co/models/" +
                    "microsoft/resnet-50";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60,    java.util.concurrent.TimeUnit.SECONDS)
            .build();

    // ── Convertir image en Base64 ────────────────────────────
    public String imageToBase64(String imagePath) throws Exception {
        byte[] bytes = new FileInputStream(imagePath).readAllBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ── Extraire les features d'une image ────────────────────
    private float[] extractFeatures(String base64Image) throws Exception {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        RequestBody requestBody = RequestBody.create(
                imageBytes,
                MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(HF_API_URL)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ?
                        response.body().string() : "";
                throw new Exception("HuggingFace error: "
                        + response.code() + " " + body);
            }

            String responseBody = response.body().string();
            JSONArray arr = new JSONArray(responseBody);

            float[] features = new float[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                features[i] = (float) arr.getDouble(i);
            }
            return features;
        }
    }

    // ── Calculer similarité cosinus entre deux vecteurs ──────
    private double cosineSimilarity(float[] v1, float[] v2) {
        int len = Math.min(v1.length, v2.length);
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < len; i++) {
            dot   += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 == 0 || norm2 == 0) return 0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // ── Comparer deux images ──────────────────────────────────
    public double compareFaces(String base64Image1,
                               String base64Image2) throws Exception {
        float[] features1 = extractFeatures(base64Image1);
        float[] features2 = extractFeatures(base64Image2);
        return cosineSimilarity(features1, features2);
    }

    // ── Vérifier si même personne (seuil 0.85) ───────────────
    public boolean verifyFace(String base64Captured,
                              String base64Stored) throws Exception {
        double score = compareFaces(base64Captured, base64Stored);
        System.out.println("Score similarité visage : " + score);
        return score >= 0.85;
    }
}