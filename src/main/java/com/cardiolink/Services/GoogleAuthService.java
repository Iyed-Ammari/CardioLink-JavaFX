package com.cardiolink.Services;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GoogleAuthService {

    private static final String CLIENT_ID     =
            "887169143043-qm7jr1bc80lp80r0jn4edtc1m3g1355c.apps.googleusercontent.com";
    private static final String CLIENT_SECRET =
            "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";
    private static final String REDIRECT_URI  =
            "http://localhost:8085/callback";
    private static final String TOKEN_URL     =
            "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL  =
            "https://www.googleapis.com/oauth2/v2/userinfo";

    // ── 1. Générer l'URL d'autorisation ──────────────────────
    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/auth"
                + "?client_id="     + encode(CLIENT_ID)
                + "&redirect_uri="  + encode(REDIRECT_URI)
                + "&response_type=code"
                + "&scope="         + encode("openid email profile")
                + "&access_type=offline"
                + "&prompt=select_account";
    }

    // ── 2. Lancer serveur local et capturer le code auto ─────
    public String waitForAuthCode() throws Exception {
        CountDownLatch latch     = new CountDownLatch(1);
        AtomicReference<String> codeRef = new AtomicReference<>();

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8085), 0);

        server.createContext("/callback", exchange -> {
            // Récupérer le code depuis l'URL
            String query = exchange.getRequestURI().getQuery();
            String code  = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        code = URLDecoder.decode(
                                param.substring(5),
                                StandardCharsets.UTF_8);
                        break;
                    }
                }
            }

            // Répondre au navigateur
            String htmlResponse = code != null
                    ? "<html><body style='font-family:Arial;text-align:center;padding:60px'>" +
                    "<h2 style='color:#7F77DD'>✅ Connexion réussie !</h2>" +
                    "<p>Vous pouvez fermer cet onglet et retourner sur CardioLink.</p>" +
                    "</body></html>"
                    : "<html><body style='font-family:Arial;text-align:center;padding:60px'>" +
                    "<h2 style='color:#E24B4A'>❌ Erreur de connexion</h2>" +
                    "<p>Veuillez réessayer.</p>" +
                    "</body></html>";

            byte[] bytes = htmlResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            codeRef.set(code);
            latch.countDown();
        });

        server.start();

        try {
            // Attendre max 120 secondes
            latch.await(120, java.util.concurrent.TimeUnit.SECONDS);
        } finally {
            server.stop(0);
        }

        String code = codeRef.get();
        if (code == null) throw new Exception("Timeout: aucun code reçu.");
        return code;
    }

    // ── 3. Échanger le code contre access_token ──────────────
    public String exchangeCodeForToken(String authCode) throws Exception {
        String params =
                "code="           + encode(authCode)      +
                        "&client_id="     + encode(CLIENT_ID)     +
                        "&client_secret=" + encode(CLIENT_SECRET) +
                        "&redirect_uri="  + encode(REDIRECT_URI)  +
                        "&grant_type=authorization_code";

        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);

        if (json.has("error")) {
            throw new Exception("Erreur token : "
                    + json.getString("error")
                    + " - " + json.optString("error_description"));
        }

        return json.getString("access_token");
    }

    // ── 4. Récupérer les infos utilisateur Google ────────────
    public JSONObject getUserInfo(String accessToken) throws Exception {
        URL url = new URL(USERINFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        String response = readResponse(conn);
        return new JSONObject(response);
    }

    // ── Helper : lire réponse HTTP ───────────────────────────
    private String readResponse(HttpURLConnection conn) throws Exception {
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        if (is == null) return "{}";
        BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    // ── Helper : encoder URL ─────────────────────────────────
    private String encode(String value) {
        try {
            return URLEncoder.encode(value,
                    StandardCharsets.UTF_8.toString());
        } catch (Exception e) { return value; }
    }
}