package com.cardiolink.WebSocket;

import com.cardiolink.Controllers.ChatControllerAdvanced;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class ChatWebSocketClientAdvanced extends WebSocketClient {

    private final ChatControllerAdvanced controller;

    public ChatWebSocketClientAdvanced(URI serverUri, ChatControllerAdvanced controller) {
        super(serverUri);
        this.controller = controller;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("[WS] Connecté au serveur WebSocket Ratchet (port 3001)");
    }

    @Override
    public void onMessage(String rawJson) {
        System.out.println("[WS] Message reçu : " + rawJson);

        javafx.application.Platform.runLater(() -> {
            try {
                JSONObject json = new JSONObject(rawJson);
                String type = json.optString("type", "message");

                switch (type) {
                    case "message":
                        controller.onWsMessageReceived(json);
                        break;
                    case "reaction":
                        System.out.println("[WS] Réaction reçue : " + json.toString());
                        controller.onWsReactionReceived(json);
                        break;
                    case "pin":
                        System.out.println("[WS] Épinglage : " + json.toString());
                        controller.onWsPinChanged(json);
                        break;
                    case "archive":
                        System.out.println("[WS] Archivage : " + json.toString());
                        controller.onWsArchiveChanged(json);
                        break;
                    case "read":
                        System.out.println("[WS] Lecture marquée : " + json.toString());
                        controller.onWsReadReceipt(json);
                        break;
                    case "typing":
                        System.out.println("[WS] Indicateur typing : " + json.toString());
                        controller.onWsTyping(json);
                        break;
                    case "edit":
                        System.out.println("[WS] Message modifié : " + json.toString());
                        controller.onWsMessageEdited(json);
                        break;
                    case "delete":
                        System.out.println("[WS] Message supprimé : " + json.toString());
                        controller.onWsMessageDeleted(json);
                        break;
                    default:
                        System.out.println("[WS] Type inconnu : " + type);
                        break;
                }

            } catch (Exception e) {
                System.err.println("[WS] JSON invalide : " + rawJson + " — " + e.getMessage());
            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WS] Déconnecté : " + reason + " (code=" + code + ", remote=" + remote + ")");
        javafx.application.Platform.runLater(controller::onWsDisconnected);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WS] Erreur : " + ex.getMessage());
        javafx.application.Platform.runLater(() -> controller.onWsError(ex.getMessage()));
    }

    public void sendJson(JSONObject json) {
        if (isOpen()) {
            send(json.toString());
        } else {
            System.err.println("[WS] Impossible d'envoyer : connexion fermée");
        }
    }

    public void sendMessage(int conversationId, int senderId, String content, String classification) {
        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("conversationId", conversationId);
        json.put("senderId", senderId);
        json.put("content", content);
        json.put("classification", classification);
        json.put("time", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        sendJson(json);
    }

    public void sendReaction(int messageId, int userId, String emoji, boolean added) {
        JSONObject json = new JSONObject();
        json.put("type", "reaction");
        json.put("messageId", messageId);
        json.put("userId", userId);
        json.put("emoji", emoji);
        json.put("added", added);
        sendJson(json);
    }

    public void sendPin(int messageId, boolean isPinned) {
        JSONObject json = new JSONObject();
        json.put("type", "pin");
        json.put("messageId", messageId);
        json.put("isPinned", isPinned);
        sendJson(json);
    }

    public void sendArchive(int messageId, boolean isArchived) {
        JSONObject json = new JSONObject();
        json.put("type", "archive");
        json.put("messageId", messageId);
        json.put("isArchived", isArchived);
        sendJson(json);
    }

    public void sendReadReceipt(int conversationId, int readerId) {
        JSONObject json = new JSONObject();
        json.put("type", "read");
        json.put("conversationId", conversationId);
        json.put("readerId", readerId);
        sendJson(json);
    }

    public void sendTyping(int conversationId, int senderId, boolean isTyping) {
        JSONObject json = new JSONObject();
        json.put("type", "typing");
        json.put("conversationId", conversationId);
        json.put("senderId", senderId);
        json.put("isTyping", isTyping);
        sendJson(json);
    }

    /* ── NOUVEAUTÉS : ENVOI DES ÉVÉNEMENTS ÉDITION / SUPPRESSION ── */

    public void sendEdit(int messageId, String content) {
        JSONObject json = new JSONObject();
        json.put("type", "edit");
        json.put("messageId", messageId);
        json.put("content", content);
        sendJson(json);
    }

    public void sendDelete(int messageId) {
        JSONObject json = new JSONObject();
        json.put("type", "delete");
        json.put("messageId", messageId);
        sendJson(json);
    }
}