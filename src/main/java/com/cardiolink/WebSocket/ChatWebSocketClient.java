package com.cardiolink.WebSocket;

import com.cardiolink.Controllers.ChatController;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ChatWebSocketClient extends WebSocketClient {

    private ChatController controller;

    public ChatWebSocketClient(URI serverUri, ChatController controller) {
        super(serverUri);
        this.controller = controller;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connecté au serveur WebSocket Ratchet !");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Message reçu du serveur : " + message);

        // TRÈS IMPORTANT : Les messages arrivent sur un thread différent.
        // Pour modifier l'interface graphique JavaFX, on DOIT utiliser Platform.runLater
        Platform.runLater(() -> {
            controller.receiveMessage(message);
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Déconnecté du serveur WebSocket : " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Erreur WebSocket : " + ex.getMessage());
    }
}