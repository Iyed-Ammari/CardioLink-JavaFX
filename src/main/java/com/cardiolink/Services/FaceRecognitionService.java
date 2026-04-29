package com.cardiolink.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.Base64;

public class FaceRecognitionService {

    private static final String HF_TOKEN =
            "hf_njJzAsEDpuviUPXwDZZrDOmmfjHlJmkbJuE";
    private static final String HF_API_URL =
            "https://api-inference.huggingface.co/models/" +
                    "deepinsight/insightface";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60,    java.util.concurrent.TimeUnit.SECONDS)
            .build();

    // ── Convertir fichier image en Base64 ────────────────────
    public String imageToBase64(String imagePath) throws Exception {
        File file = new File(imagePath);
        byte[] bytes = new FileInputStream(file).readAllBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ── Comparer deux images et retourner score similarité ───
    public double compareFaces(String base64Image1,
                               String base64Image2) throws Exception {
        JSONObject body = new JSONObject();
        body.put("source_image", base64Image1);
        body.put("target_image", base64Image2);

        RequestBody requestBody = RequestBody.create(
                body.toString(),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(HF_API_URL)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new Exception("HuggingFace error: "
                        + response.code() + " " + response.message());

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (json.has("similarity"))
                return json.getDouble("similarity");
            if (json.has("score"))
                return json.getDouble("score");

            // Parfois retourné en tableau
            if (responseBody.startsWith("[")) {
                JSONArray arr = new JSONArray(responseBody);
                if (arr.length() > 0 && arr.getJSONObject(0).has("score"))
                    return arr.getJSONObject(0).getDouble("score");
            }

            throw new Exception("Réponse inattendue : " + responseBody);
        }
    }

    // ── Vérifier si deux visages correspondent (seuil 0.6) ───
    public boolean verifyFace(String base64Captured,
                              String base64Stored) throws Exception {
        double score = compareFaces(base64Captured, base64Stored);
        return score >= 0.6;
    }
}