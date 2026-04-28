package com.cardiolink.Controllers;

import com.cardiolink.Models.Message;
import com.cardiolink.Services.MessageService;
import com.cardiolink.WebSocket.ChatWebSocketClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private ListView<Message> messageListView; // Utilise l'objet Message pour les bulles
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;

    private MessageService messageService;
    private ChatWebSocketClient webSocketClient;

    // TODO: Ces IDs devront être récupérés depuis la session de l'utilisateur connecté
    private final int currentConversationId = 3;
    private final int currentUserId = 33; // Ton ID (Médecin ou Patient)


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageService = new MessageService();

        // 1. Configurer l'affichage des bulles de chat (CellFactory)
        setupMessageBubbles();

        // 2. Charger l'historique depuis la base de données
        loadMessagesFromDatabase();

        // 3. Lancer la connexion temps réel vers Ratchet
        connectWebSocket();
    }

    /**
     * Définit comment chaque message doit être dessiné (gauche ou droite, couleur, etc.)
     */
    /**
     * Définit comment chaque message doit être dessiné (gauche ou droite, couleur, etc.)
     */
    private void setupMessageBubbles() {
        messageListView.setCellFactory(param -> new ListCell<Message>() {

            // On crée les éléments UI une seule fois (meilleur pour les performances)
            private final HBox container = new HBox();
            private final Label bubble = new Label();

            // Bloc d'initialisation
            {
                bubble.setWrapText(true);
                bubble.setMaxWidth(280);

                // LA LIGNE QUI MANQUAIT : On met la bulle dans le conteneur !
                container.getChildren().add(bubble);
            }

            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null || msg.getContent() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // 1. Mettre le texte du message
                    bubble.setText(msg.getContent());

                    // 2. Réinitialiser les styles (très important quand on scrolle de haut en bas)
                    bubble.getStyleClass().removeAll("message-bubble", "message-me", "message-other");
                    bubble.getStyleClass().add("message-bubble");

                    // 3. Aligner et colorer selon l'expéditeur
                    if (msg.getSenderId() == currentUserId) {
                        container.setAlignment(Pos.CENTER_RIGHT);
                        bubble.getStyleClass().add("message-me");
                    } else {
                        container.setAlignment(Pos.CENTER_LEFT);
                        bubble.getStyleClass().add("message-other");
                    }

                    // 4. Afficher le tout
                    setGraphic(container);
                    setText(null);
                }
            }
        });
    }
    private void loadMessagesFromDatabase() {
        try {
            messageListView.getItems().clear();
            // Récupère tous les messages (on pourrait filtrer par conversation en SQL plus tard)
            List<Message> history = messageService.getAll();

            for (Message msg : history) {
                if (msg.getConversationId() == currentConversationId) {
                    messageListView.getItems().add(msg);
                }
            }
            scrollToBottom();
        } catch (SQLDataException e) {
            System.err.println("Erreur chargement SQL : " + e.getMessage());
        }
    }

    private void connectWebSocket() {
        try {
            URI serverUri = new URI("ws://localhost:3001");
            webSocketClient = new ChatWebSocketClient(serverUri, this);
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            System.err.println("Erreur URI WebSocket : " + e.getMessage());
        }
    }

    @FXML
    private Button backButton;
    @FXML private Label  avatarLabel;
    @FXML private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dashboard_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Profil");
            stage.setScene(scene);
            stage.show();
            ProfilPatientController ctrl = loader.getController();
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML
    public void handleSendMessage() {
        String content = messageInput.getText().trim();

        if (!content.isEmpty()) {
            // A. Création du modèle Message
            Message newMessage = new Message();
            newMessage.setConversationId(currentConversationId);
            newMessage.setSenderId(currentUserId);
            newMessage.setContent(content);
            newMessage.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            newMessage.setRead(false);
            newMessage.setClassification("NORMAL");
            newMessage.setPinned(false);
            newMessage.setArchived(false);

            try {
                // 1. Sauvegarde MySQL
                messageService.add(newMessage);

                // 2. Envoi WebSocket en format JSON (CamelCase comme le Web)
                if (webSocketClient != null && webSocketClient.isOpen()) {
                    JSONObject json = new JSONObject();
                    json.put("conversationId", currentConversationId);
                    json.put("senderId", currentUserId);
                    json.put("content", content);
                    json.put("classification", "NORMAL");
                    json.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

                    webSocketClient.send(json.toString());
                }

                // 3. Mise à jour UI locale
                messageListView.getItems().add(newMessage);
                messageInput.clear();
                scrollToBottom();

            } catch (SQLDataException e) {
                System.err.println("Erreur SQL lors de l'envoi : " + e.getMessage());
            }
        }
    }

    /**
     * Méthode appelée par ChatWebSocketClient lors de la réception d'un message JSON
     */
    public void receiveMessage(String jsonRaw) {
        Platform.runLater(() -> {
            try {
                JSONObject json = new JSONObject(jsonRaw);
                int convId = json.getInt("conversationId");
                int senderId = json.getInt("senderId");
                String content = json.getString("content");

                if (convId == currentConversationId) {
                    Message receivedMsg = new Message();
                    receivedMsg.setConversationId(convId);
                    receivedMsg.setSenderId(senderId);
                    receivedMsg.setContent(content);
                    // (On peut aussi extraire la date/heure du JSON si besoin)

                    messageListView.getItems().add(receivedMsg);
                    scrollToBottom();
                }
            } catch (Exception e) {
                System.err.println("JSON invalide reçu : " + jsonRaw);
            }
        });
    }

    private void scrollToBottom() {
        if (!messageListView.getItems().isEmpty()) {
            messageListView.scrollTo(messageListView.getItems().size() - 1);
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/dashboard_patient.fxml"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}