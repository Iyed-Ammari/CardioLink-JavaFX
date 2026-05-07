package com.cardiolink.Services;

import com.cardiolink.Controllers.PredictionDayPeak;
import com.cardiolink.Controllers.PredictionResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PredictionService {

    private static final String API_URL = "http://127.0.0.1:8000/predict?month=";

    public PredictionResponse predict(String month) {
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("Le mois est obligatoire.");
        }

        try {
            URL url = new URL(API_URL + month);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Réponse API invalide : HTTP " + status);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            return parsePredictionResponse(response.toString());

        } catch (Exception e) {
            throw new RuntimeException("Impossible de contacter le service IA : " + e.getMessage(), e);
        }
    }

    private PredictionResponse parsePredictionResponse(String json) {
        PredictionResponse result = new PredictionResponse();

        String mois = extractString(json, "\"mois\":\"", "\"");
        result.setMois(mois);

        List<PredictionDayPeak> jours = new ArrayList<>();

        String key = "\"top_jours_pic\":[";
        int start = json.indexOf(key);
        if (start != -1) {
            start += key.length();
            int end = json.indexOf("]", start);
            if (end != -1) {
                String arrayContent = json.substring(start, end);

                String[] items = arrayContent.split("\\},\\{");
                for (String item : items) {
                    String cleaned = item.replace("{", "").replace("}", "");
                    String date = extractString(cleaned, "\"date\":\"", "\"");
                    double prediction = extractDouble(cleaned, "\"prediction\":");

                    if (date != null && !date.isBlank()) {
                        jours.add(new PredictionDayPeak(date, prediction));
                    }
                }
            }
        }

        result.setTopJoursPic(jours);
        return result;
    }

    private String extractString(String source, String startToken, String endToken) {
        int start = source.indexOf(startToken);
        if (start == -1) return "";
        start += startToken.length();

        int end = source.indexOf(endToken, start);
        if (end == -1) return "";

        return source.substring(start, end);
    }

    private double extractDouble(String source, String token) {
        int start = source.indexOf(token);
        if (start == -1) return 0.0;

        start += token.length();
        int end = start;

        while (end < source.length() && "0123456789.-".indexOf(source.charAt(end)) >= 0) {
            end++;
        }

        try {
            return Double.parseDouble(source.substring(start, end));
        } catch (Exception e) {
            return 0.0;
        }
    }
}