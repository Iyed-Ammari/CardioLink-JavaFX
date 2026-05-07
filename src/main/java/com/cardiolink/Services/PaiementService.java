package com.cardiolink.Services;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.User;
import com.cardiolink.utils.StripeConfig;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PaiementService {

    private final CommandeService commandeService = new CommandeService();
    private final UserService     userService     = new UserService();
    private final EmailServiceKhadijaa emailService    = new EmailServiceKhadijaa();

    public void payerCommandeAvecStripe(
            Commande commande,
            Runnable onSuccess,
            Consumer<String> onInfo,
            Consumer<String> onError
    ) {
        try {
            if (commande == null || commande.getId() == null) {
                throw new IllegalArgumentException("Commande invalide.");
            }

            if (commande.getStatut() != Commande.Statut.EN_ATTENTE_PAIEMENT) {
                throw new IllegalStateException(
                        "La commande doit être EN_ATTENTE_PAIEMENT. Statut actuel : " + commande.getStatut()
                );
            }

            BigDecimal montant = commande.getMontantTotal();
            if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Montant de commande invalide.");
            }

            if (!Desktop.isDesktopSupported()) {
                throw new IllegalStateException("L'ouverture du navigateur n'est pas supportée sur cette machine.");
            }

            Stripe.apiKey = StripeConfig.SECRET_KEY;

            LocalCallbackServer callbackServer = new LocalCallbackServer(
                    commande.getId(),
                    onSuccess,
                    onInfo,
                    onError
            );
            callbackServer.start();

            String successUrl = callbackServer.getBaseUrl()
                    + StripeConfig.SUCCESS_PATH
                    + "?session_id={CHECKOUT_SESSION_ID}&commandeId=" + commande.getId();

            String cancelUrl = callbackServer.getBaseUrl()
                    + StripeConfig.CANCEL_PATH
                    + "?commandeId=" + commande.getId();

            long montantMinorUnit = montant.multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(String.valueOf(commande.getId()))
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(StripeConfig.STRIPE_CURRENCY)
                                                    .setUnitAmount(montantMinorUnit)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Commande CardioLink #" + commande.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            if (session == null || session.getUrl() == null || session.getUrl().isBlank()) {
                callbackServer.stop();
                throw new IllegalStateException("Stripe n'a pas renvoyé d'URL de paiement.");
            }

            if (onInfo != null) {
                Platform.runLater(() ->
                        onInfo.accept("🔗 Redirection vers Stripe Checkout en cours...")
                );
            }

            Desktop.getDesktop().browse(URI.create(session.getUrl()));

        } catch (Exception e) {
            if (onError != null) {
                Platform.runLater(() -> onError.accept("❌ " + e.getMessage()));
            }
        }
    }

    private void traiterRetourStripeSucces(
            String sessionId,
            int commandeId,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        try {
            Session session = Session.retrieve(sessionId);

            if (session == null) {
                throw new IllegalStateException("Session Stripe introuvable.");
            }

            if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                throw new IllegalStateException("Le paiement Stripe n'est pas confirmé.");
            }

            Commande commandeFraiche = commandeService.getById(commandeId);
            if (commandeFraiche == null) {
                throw new IllegalStateException("Commande introuvable après retour Stripe.");
            }

            if (commandeFraiche.getStatut() == Commande.Statut.PAYEE
                    || commandeFraiche.getStatut() == Commande.Statut.LIVREE) {
                if (onSuccess != null) {
                    Platform.runLater(onSuccess);
                }
                return;
            }

            commandeService.payer(commandeFraiche);

            // ✉️ Envoi email de confirmation au patient (non bloquant)
            try {
                User patient = userService.getUserById(commandeFraiche.getUserId());
                if (patient != null && patient.getEmail() != null) {
                    String nomComplet = (patient.getPrenom() != null ? patient.getPrenom() : "")
                            + " " + (patient.getNom() != null ? patient.getNom() : "");
                    emailService.envoyerConfirmationPaiement(
                            patient.getEmail(),
                            nomComplet.trim(),
                            commandeFraiche
                    );
                }
            } catch (Exception emailEx) {
                System.err.println("⚠ Email confirmation non envoyé : " + emailEx.getMessage());
            }

            if (onSuccess != null) {
                Platform.runLater(onSuccess);
            }

        } catch (StripeException e) {
            if (onError != null) {
                Platform.runLater(() -> onError.accept("❌ Erreur Stripe : " + e.getMessage()));
            }
        } catch (Exception e) {
            if (onError != null) {
                Platform.runLater(() -> onError.accept("❌ " + e.getMessage()));
            }
        }
    }

    private final class LocalCallbackServer {
        private final int commandeId;
        private final Runnable onSuccess;
        private final Consumer<String> onInfo;
        private final Consumer<String> onError;

        private HttpServer server;
        private int port;

        private LocalCallbackServer(
                int commandeId,
                Runnable onSuccess,
                Consumer<String> onInfo,
                Consumer<String> onError
        ) {
            this.commandeId = commandeId;
            this.onSuccess = onSuccess;
            this.onInfo = onInfo;
            this.onError = onError;
        }

        private void start() throws IOException {
            server = HttpServer.create(new InetSocketAddress(StripeConfig.LOCAL_HOST, 0), 0);
            port = server.getAddress().getPort();

            server.createContext(StripeConfig.SUCCESS_PATH, this::handleSuccess);
            server.createContext(StripeConfig.CANCEL_PATH, this::handleCancel);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        }

        private String getBaseUrl() {
            return "http://" + StripeConfig.LOCAL_HOST + ":" + port;
        }

        private void handleSuccess(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
                String sessionId = query.get("session_id");
                String commandeIdParam = query.get("commandeId");

                if (sessionId == null || sessionId.isBlank() || commandeIdParam == null || commandeIdParam.isBlank()) {
                    sendHtml(exchange, 400, StripeConfig.ERROR_HTML);
                    if (onError != null) {
                        Platform.runLater(() -> onError.accept("❌ Retour Stripe invalide."));
                    }
                    stop();
                    return;
                }

                int cmdId = Integer.parseInt(commandeIdParam);
                traiterRetourStripeSucces(sessionId, cmdId, onSuccess, onError);
                sendHtml(exchange, 200, StripeConfig.SUCCESS_HTML);

            } catch (Exception e) {
                sendHtml(exchange, 500, StripeConfig.ERROR_HTML);
                if (onError != null) {
                    Platform.runLater(() -> onError.accept("❌ Retour Stripe : " + e.getMessage()));
                }
            } finally {
                stop();
            }
        }

        private void handleCancel(HttpExchange exchange) throws IOException {
            try {
                sendHtml(exchange, 200, StripeConfig.CANCEL_HTML);
                if (onInfo != null) {
                    Platform.runLater(() ->
                            onInfo.accept("⚠️ Paiement Stripe annulé pour la commande #" + commandeId + ".")
                    );
                }
            } finally {
                stop();
            }
        }

        private void stop() {
            if (server != null) {
                server.stop(0);
            }
        }

        private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Map<String, String> parseQuery(String rawQuery) {
            Map<String, String> map = new HashMap<>();
            if (rawQuery == null || rawQuery.isBlank()) {
                return map;
            }

            String[] pairs = rawQuery.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1
                        ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                        : "";
                map.put(key, value);
            }
            return map;
        }
    }
}