package com.cardiolink.Controllers;

import com.cardiolink.Models.*;
import com.cardiolink.Services.*;
import com.cardiolink.WebSocket.ChatWebSocketClientAdvanced;
import com.cardiolink.utils.ManagerSession;
import jakarta.mail.Message;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.json.JSONObject;

// Imports pour l'envoi d'email
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ChatControllerAdvanced implements Initializable {

    private static final Map<String, List<String>> SUGGESTIONS = new LinkedHashMap<>();

    static {
        SUGGESTIONS.put("URGENT", Arrays.asList("Je vous contacte immédiatement.", "Je prends en charge votre urgence.", "Rendez-vous en consultation d'urgence."));
        SUGGESTIONS.put("NORMAL", Arrays.asList("Merci pour votre message.", "Votre demande est bien reçue.", "Je vous réponds dans les meilleurs délais."));
        SUGGESTIONS.put("ADMINISTRATIF", Arrays.asList("Votre document est bien enregistré.", "Je transmets votre dossier.", "Les données ont été mises à jour."));
        SUGGESTIONS.put("DEFAULT", Arrays.asList("Bonjour, comment puis-je vous aider ?", "Je vous recontacte rapidement.", "Prenez soin de vous."));
    }

    private static final List<String> EMOJI_LIST = Arrays.asList("👍", "❤️", "😂", "😮", "😢", "😡", "🙏", "👏");

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private ToggleButton sortOrderBtn;
    @FXML
    private ListView<Conversation> convList;
    @FXML
    private Label globalNotifBadge;
    @FXML
    private Button backButton;
    @FXML
    private Label contactAvatar;
    @FXML
    private Label contactNameLabel;
    @FXML
    private Label contactStatusLabel;
    @FXML
    private Label typingLabel;
    @FXML
    private Label convNotifCount;
    @FXML
    private Button filterAll;
    @FXML
    private Button filterPinned;
    @FXML
    private Button filterArchived;
    @FXML
    private Button filterUrgent;

    // Champ de recherche interne aux messages
    @FXML
    private TextField messageSearchField;

    @FXML
    private ListView<com.cardiolink.Models.Message> messageListView;
    @FXML
    private HBox suggestionBar;
    @FXML
    private Button suggestion1;
    @FXML
    private Button suggestion2;
    @FXML
    private Button suggestion3;
    @FXML
    private Button emojiPickerBtn;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private ComboBox<String> classificationCombo;

    private final MessageService messageService = new MessageService();
    private final ConversationService conversationService = new ConversationService();
    private final MessageReactionService reactionService = new MessageReactionService();
    private final NotificationService notificationService = new NotificationService();
    private final MlClassificationService mlService = new MlClassificationService();
    private final InterventionService interventionService = new InterventionService();

    private boolean mlAvailable = false;
    private int currentUserId = -1;
    private Conversation selectedConv = null;
    private String activeFilter = "all";
    private String sortOrder = "DESC";
    private com.cardiolink.Models.Message lastContextMessage = null;

    private com.cardiolink.Models.Message editingMessage = null;

    // Liste en mémoire des messages de la conversation active pour le filtrage
    private List<com.cardiolink.Models.Message> currentConversationMessages = new ArrayList<>();

    private ChatWebSocketClientAdvanced wsClient;
    private Popup emojiPopup;
    private javafx.animation.PauseTransition typingTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUserId = ManagerSession.getInstance().getCurrentUserId();
        if (currentUserId == -1) {
            System.err.println("[ChatAdv] Aucun utilisateur connecté !");
            return;
        }

        setupSortCombo();
        setupClassificationCombo();
        setupConvListCells();
        setupMessageListCells();
        buildEmojiPopup();
        setupTypingTimer();

        loadConversations(null, "updated", sortOrder);
        refreshGlobalNotifBadge();

        Task<Boolean> mlCheck = new Task<>() {
            @Override
            protected Boolean call() {
                return mlService.isAvailable();
            }
        };
        mlCheck.setOnSucceeded(e -> {
            mlAvailable = mlCheck.getValue();
            System.out.println("[ML] Service Flask : " + (mlAvailable ? "✅ disponible" : "⚠️ hors ligne (fallback statique)"));
        });
        new Thread(mlCheck, "ml-health-check").start();

        connectWebSocket();
    }

    private void loadConversations(String search, String sortBy, String order) {
        List<Conversation> convs = conversationService.findByUserWithSearchAndSort(
                currentUserId, search, sortBy, order);
        convList.getItems().setAll(convs);
    }

    @FXML
    private void handleNewConversation() {
        try {
            User currentUser = ManagerSession.getInstance().getCurrentUser();
            if (currentUser == null) return;

            boolean isDoctor = currentUser.getRoleClean().contains("MEDECIN");
            String targetRole = isDoctor ? "PATIENT" : "MEDECIN";

            List<User> allUsers = new UserService().getAll();
            Map<String, User> targetUsersMap = new HashMap<>();
            List<String> targetUserNames = new ArrayList<>();

            for (User u : allUsers) {
                if (u.getId() != currentUserId && u.getRoleClean().contains(targetRole)) {
                    String displayName = u.getPrenom() + " " + u.getNom() + " (" + u.getEmail() + ")";
                    targetUserNames.add(displayName);
                    targetUsersMap.put(displayName, u);
                }
            }

            if (targetUserNames.isEmpty()) {
                showAlert("Aucun " + (isDoctor ? "patient" : "médecin") + " trouvé dans la base de données.");
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(targetUserNames.get(0), targetUserNames);
            dialog.setTitle("Nouvelle conversation");
            dialog.setHeaderText("Commencer une discussion avec un " + (isDoctor ? "patient" : "médecin"));
            dialog.setContentText("Sélectionnez le contact :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                User selectedUser = targetUsersMap.get(result.get());

                List<Conversation> existingConvs = conversationService.getConversationsByUser(currentUserId);
                Conversation foundConv = null;
                for (Conversation c : existingConvs) {
                    int otherId = (c.getPatientId() == currentUserId) ? c.getMedecinId() : c.getPatientId();
                    if (otherId == selectedUser.getId()) {
                        foundConv = c;
                        break;
                    }
                }

                if (foundConv != null) {
                    for (Conversation c : convList.getItems()) {
                        if (c.getId() == foundConv.getId()) {
                            convList.getSelectionModel().select(c);
                            handleConvSelected();
                            break;
                        }
                    }
                } else {
                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    int patientId = isDoctor ? selectedUser.getId() : currentUserId;
                    int medecinId = isDoctor ? currentUserId : selectedUser.getId();

                    Conversation newConv = new Conversation(patientId, medecinId, now, now, true);
                    conversationService.add(newConv);

                    loadConversations(searchField.getText(), getSortByParam(), sortOrder);

                    for (Conversation c : convList.getItems()) {
                        int otherId = (c.getPatientId() == currentUserId) ? c.getMedecinId() : c.getPatientId();
                        if (otherId == selectedUser.getId()) {
                            convList.getSelectionModel().select(c);
                            handleConvSelected();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'initialisation de la conversation.");
        }
    }

    private void setupConvListCells() {
        convList.setCellFactory(lv -> new ListCell<>() {
            private final HBox cell = new HBox(10);
            private final Label avatar = new Label();
            private final VBox info = new VBox(2);
            private final Label name = new Label();
            private final Label preview = new Label();
            private final VBox right = new VBox(2);
            private final Label time = new Label();
            private final Label badge = new Label();

            {
                avatar.getStyleClass().addAll("conv-avatar", "avatar-patient");
                name.getStyleClass().add("conv-name");
                preview.getStyleClass().add("conv-preview");
                time.getStyleClass().add("conv-time");
                badge.getStyleClass().add("unread-badge");

                info.getChildren().addAll(name, preview);
                HBox.setHgrow(info, Priority.ALWAYS);
                right.getChildren().addAll(time, badge);
                right.setAlignment(Pos.TOP_RIGHT);

                cell.getChildren().addAll(avatar, info, right);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.getStyleClass().add("conv-cell");
                cell.setPadding(new Insets(8, 12, 8, 12));
            }

            @Override
            protected void updateItem(Conversation conv, boolean empty) {
                super.updateItem(conv, empty);
                if (empty || conv == null) {
                    setGraphic(null);
                    return;
                }

                boolean isPatient = conv.getPatientId() == currentUserId;
                int contactId = isPatient ? conv.getMedecinId() : conv.getPatientId();

                try {
                    User contact = new UserService().getUserById(contactId);
                    if (contact != null) {
                        String initials = getInitials(contact.getPrenom(), contact.getNom());
                        avatar.setText(initials);
                        avatar.getStyleClass().removeAll("avatar-patient", "avatar-medecin");
                        avatar.getStyleClass().add(isPatient ? "avatar-medecin" : "avatar-patient");
                        name.setText(contact.getPrenom() + " " + contact.getNom());
                    } else {
                        avatar.setText("?");
                        name.setText("Contact #" + contactId);
                    }
                } catch (Exception e) {
                    avatar.setText("?");
                    name.setText("Contact #" + contactId);
                }

                com.cardiolink.Models.Message last = messageService.getLastMessageByConversation(conv.getId());
                preview.setText(last != null ? truncate(last.getContent(), 35) : "Nouvelle conversation");
                time.setText(formatTime(conv.getUpdated_at()));

                int unread = messageService.countUnread(conv.getId(), currentUserId);
                badge.setText(String.valueOf(unread));
                badge.setVisible(unread > 0);
                badge.setManaged(unread > 0);

                cell.getStyleClass().removeAll("conv-cell-selected");
                if (selectedConv != null && conv.getId() == selectedConv.getId()) {
                    cell.getStyleClass().add("conv-cell-selected");
                }
                setGraphic(cell);
            }
        });
    }

    @FXML
    private void handleConvSelected() {
        Conversation conv = convList.getSelectionModel().getSelectedItem();
        if (conv == null || (selectedConv != null && conv.getId() == selectedConv.getId())) return;

        selectedConv = conv;
        activeFilter = "all";
        resetFilterButtons();
        messageSearchField.clear();

        updateChatHeader(conv);

        messageService.markAllAsReadByConversation(conv.getId(), currentUserId);
        notificationService.markAllAsReadByConversation(currentUserId, conv.getId());

        loadMessages("all");

        refreshGlobalNotifBadge();
        convList.refresh();

        if (wsClient != null && wsClient.isOpen()) {
            wsClient.sendReadReceipt(conv.getId(), currentUserId);
        }
    }

    private void updateChatHeader(Conversation conv) {
        boolean isPatient = conv.getPatientId() == currentUserId;
        int contactId = isPatient ? conv.getMedecinId() : conv.getPatientId();

        try {
            User contact = new UserService().getUserById(contactId);
            if (contact != null) {
                contactNameLabel.setText(contact.getPrenom() + " " + contact.getNom());
                contactStatusLabel.setText(
                        isPatient ? "Médecin" : "Patient"
                                + " · " + (conv.isActive() ? "Actif" : "Inactif"));
                contactAvatar.setText(getInitials(contact.getPrenom(), contact.getNom()));
                // Nouvelles couleurs CardioLink
                contactAvatar.setStyle(isPatient
                        ? "-fx-background-color:#E24B4A; -fx-background-radius:21px; -fx-min-width:42px; -fx-min-height:42px; -fx-max-width:42px; -fx-max-height:42px; -fx-alignment:center; -fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:white;"
                        : "-fx-background-color:#7F77DD; -fx-background-radius:21px; -fx-min-width:42px; -fx-min-height:42px; -fx-max-width:42px; -fx-max-height:42px; -fx-alignment:center; -fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:white;");
            }
        } catch (Exception e) {
            contactNameLabel.setText("Contact #" + contactId);
        }

        int unread = notificationService.countUnread(currentUserId);
        convNotifCount.setText(String.valueOf(unread));
        convNotifCount.setVisible(unread > 0);
        convNotifCount.setManaged(unread > 0);
    }

    private void loadMessages(String filter) {
        if (selectedConv == null) return;
        currentConversationMessages = messageService.filterByType(selectedConv.getId(), filter);

        applyMessageSearch();

        if (!currentConversationMessages.isEmpty()) {
            lastContextMessage = currentConversationMessages.get(currentConversationMessages.size() - 1);
            updateSuggestions(lastContextMessage);
        }
        scrollToBottom();
    }

    @FXML
    private void handleMessageSearch() {
        applyMessageSearch();
    }

    private void applyMessageSearch() {
        if (currentConversationMessages == null) return;

        String query = messageSearchField.getText().toLowerCase().trim();

        if (query.isEmpty()) {
            messageListView.getItems().setAll(currentConversationMessages);
        } else {
            List<com.cardiolink.Models.Message> filtered = currentConversationMessages.stream()
                    .filter(m -> m.getContent() != null && m.getContent().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            messageListView.getItems().setAll(filtered);
        }
    }

    private void setupMessageListCells() {
        messageListView.setCellFactory(lv -> new ListCell<>() {

            private final VBox msgBox = new VBox(4);
            private final HBox row = new HBox(8);
            private final VBox bubbleBox = new VBox(3);
            private final Label bubble = new Label();
            private final HBox badges = new HBox(4);
            private final Label pinBadge = new Label("📌");
            private final Label urgBadge = new Label("🔴 URGENT");
            private final Label arcBadge = new Label("🗄");
            private final HBox meta = new HBox(6);
            private final Label timeLabel = new Label();
            private final Label readLabel = new Label();

            private final HBox actions = new HBox(4);
            private final Button pinBtn = new Button("📌");
            private final Button arcBtn = new Button("🗄");
            private final Button editBtn = new Button("✏️");
            private final Button delBtn = new Button("🗑️");

            private final HBox reactionBar = new HBox(4);

            {
                bubble.setWrapText(true);
                bubble.setMaxWidth(320);

                pinBadge.getStyleClass().add("badge-pinned");
                urgBadge.getStyleClass().add("badge-urgent");
                arcBadge.getStyleClass().add("badge-archived");
                badges.getChildren().addAll(pinBadge, urgBadge, arcBadge);

                timeLabel.getStyleClass().add("msg-time");
                readLabel.getStyleClass().add("msg-time");
                meta.getChildren().addAll(timeLabel, readLabel);

                pinBtn.getStyleClass().add("msg-action-btn");
                arcBtn.getStyleClass().add("msg-action-btn");
                editBtn.getStyleClass().add("msg-action-btn");
                delBtn.getStyleClass().add("msg-action-btn");

                actions.setVisible(false);
                reactionBar.getStyleClass().add("reaction-bar");
                reactionBar.setSpacing(4);

                bubbleBox.getChildren().addAll(badges, bubble, reactionBar, meta, actions);
                row.setAlignment(Pos.CENTER);
                msgBox.getChildren().add(row);
                msgBox.getStyleClass().add("msg-cell");
                msgBox.setPadding(new Insets(2, 0, 2, 0));

                msgBox.setOnMouseEntered(e -> actions.setVisible(true));
                msgBox.setOnMouseExited(e -> actions.setVisible(false));
            }

            @Override
            protected void updateItem(com.cardiolink.Models.Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                    return;
                }

                boolean isMe = msg.getSenderId() == currentUserId;

                bubble.setText(msg.getContent());
                bubble.getStyleClass().removeAll("bubble-me", "bubble-other");
                bubble.getStyleClass().add(isMe ? "bubble-me" : "bubble-other");

                pinBadge.setVisible(msg.isPinned());
                pinBadge.setManaged(msg.isPinned());
                urgBadge.setVisible("URGENT".equals(msg.getClassification()));
                urgBadge.setManaged("URGENT".equals(msg.getClassification()));
                arcBadge.setVisible(msg.isArchived());
                arcBadge.setManaged(msg.isArchived());

                timeLabel.setText(formatTime(msg.getDate()));
                readLabel.setText(isMe ? (msg.isRead() ? "✓✓" : "✓") : "");

                bubble.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) showEmojiPickerFor(msg);
                });

                pinBtn.setText(msg.isPinned() ? "📍" : "📌");
                pinBtn.setOnAction(e -> {
                    boolean newPin = messageService.togglePin(msg.getId());
                    msg.setPinned(newPin);
                    if (wsClient != null && wsClient.isOpen())
                        wsClient.sendPin(msg.getId(), newPin);
                    messageListView.refresh();
                });

                arcBtn.setText(msg.isArchived() ? "📂" : "🗄");
                arcBtn.setOnAction(e -> {
                    boolean newArc = messageService.toggleArchive(msg.getId());
                    msg.setArchived(newArc);
                    if (wsClient != null && wsClient.isOpen())
                        wsClient.sendArchive(msg.getId(), newArc);
                    messageListView.refresh();
                });

                actions.getChildren().clear();
                actions.getChildren().addAll(pinBtn, arcBtn);

                if (isMe) {
                    actions.getChildren().addAll(editBtn, delBtn);

                    editBtn.setOnAction(e -> {
                        messageInput.setText(msg.getContent());
                        editingMessage = msg;
                        messageInput.requestFocus();
                    });

                    delBtn.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce message ?", ButtonType.YES, ButtonType.NO);
                        confirm.showAndWait();
                        if (confirm.getResult() == ButtonType.YES) {
                            messageService.deleteById(msg.getId());
                            if (wsClient != null && wsClient.isOpen()) {
                                wsClient.sendDelete(msg.getId());
                            }
                            currentConversationMessages.remove(msg);
                            applyMessageSearch();
                        }
                    });
                }

                row.getChildren().clear();
                if (isMe) {
                    row.setAlignment(Pos.CENTER_RIGHT);
                    bubbleBox.setAlignment(Pos.CENTER_RIGHT);
                    reactionBar.setAlignment(Pos.CENTER_RIGHT);
                    row.getChildren().add(bubbleBox);
                } else {
                    row.setAlignment(Pos.CENTER_LEFT);
                    bubbleBox.setAlignment(Pos.CENTER_LEFT);
                    reactionBar.setAlignment(Pos.CENTER_LEFT);
                    row.getChildren().add(bubbleBox);
                }

                reactionBar.getChildren().clear();
                List<ReactionSummary> summaries = reactionService.findReactionsSummary(msg.getId());
                for (ReactionSummary rs : summaries) {
                    boolean mine = reactionService.findReaction(msg.getId(), currentUserId, rs.getEmoji()).isPresent();
                    Button chip = new Button(rs.getLabel());
                    chip.getStyleClass().add(mine ? "reaction-chip-mine" : "reaction-chip");
                    chip.setOnAction(e -> {
                        reactionService.toggleReaction(msg.getId(), currentUserId, rs.getEmoji());
                        if (wsClient != null && wsClient.isOpen())
                            wsClient.sendReaction(msg.getId(), currentUserId, rs.getEmoji(), mine);
                        messageListView.refresh();
                    });
                    reactionBar.getChildren().add(chip);
                }
                setGraphic(msgBox);
            }
        });
    }

    @FXML
    private void handleFilterAll() {
        setFilter("all");
    }

    @FXML
    private void handleFilterPinned() {
        setFilter("pinned");
    }

    @FXML
    private void handleFilterArchived() {
        setFilter("archived");
    }

    @FXML
    private void handleFilterUrgent() {
        setFilter("urgent");
    }

    private void setFilter(String type) {
        activeFilter = type;
        resetFilterButtons();

        Button active = switch (type) {
            case "pinned" -> filterPinned;
            case "archived" -> filterArchived;
            case "urgent" -> filterUrgent;
            default -> filterAll;
        };
        active.getStyleClass().removeAll("filter-btn");
        active.getStyleClass().add("filter-btn-active");

        loadMessages(type);
    }

    private void resetFilterButtons() {
        for (Button b : List.of(filterAll, filterPinned, filterArchived, filterUrgent)) {
            b.getStyleClass().removeAll("filter-btn-active");
            b.getStyleClass().add("filter-btn");
        }
    }

    // NOUVEAUTÉ : Méthode d'envoi d'email
    // NOUVEAUTÉ : Méthode d'envoi d'email mise à jour avec le patient et le message
    private void sendUrgentEmailAlert(String toEmail, String nomMedecin, String nomPatient, String contenuMessage) {
        String fromEmail = "cardiolinkpidev@gmail.com";
        String password = "qpmn qsel rmbg nfny"; // REMPLACER ICI

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(fromEmail));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            emailMessage.setSubject("🚨 URGENT : Alerte SOS de " + nomPatient + " (CardioLink)");

            String corpsMessage = "Bonjour Dr. " + nomMedecin + ",\n\n"
                    + "Le modèle d'IA de CardioLink vient de détecter une urgence.\n"
                    + "Votre patient(e) " + nomPatient + " a envoyé un message classé URGENT dans la messagerie.\n\n"
                    + "💬 Message du patient :\n"
                    + "« " + contenuMessage + " »\n\n"
                    + "Une intervention 'Alerte SOS' a été automatiquement créée dans votre planning.\n"
                    + "Veuillez vous connecter à la plateforme immédiatement pour prendre en charge ce patient.\n\n"
                    + "L'équipe CardioLink.";

            emailMessage.setText(corpsMessage);

            Transport.send(emailMessage);
            System.out.println("[Alerte SOS] Email d'urgence envoyé avec succès au Dr. " + nomMedecin + " (" + toEmail + ")");

        } catch (MessagingException e) {
            System.err.println("[Alerte SOS] Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    @FXML
    public void handleSendMessage() {
        if (selectedConv == null) {
            showAlert("Aucune conversation sélectionnée.");
            return;
        }

        String content = messageInput.getText().trim();

        if (content.isEmpty()) {
            editingMessage = null;
            return;
        }

        // Vérification de la toxicité (bloquante)
        if (!mlAvailable) {
            // Si le service était hors ligne au démarrage, on re-tente une vérification rapide
            mlAvailable = mlService.isAvailable();
        }

        if (mlAvailable && mlService.checkToxicity(content)) {
            showAlert("Votre message contient des propos inappropriés et n'a pas pu être envoyé.");
            return;
        }

        if (editingMessage != null) {
            messageService.updateContent(editingMessage.getId(), content);
            editingMessage.setContent(content);
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.sendEdit(editingMessage.getId(), content);
            }
            messageListView.refresh();
            editingMessage = null;
            messageInput.clear();
            return;
        }

        String comboValue = classificationCombo.getValue();
        String classification;
        if (comboValue == null || comboValue.startsWith("AUTO")) {
            MlClassificationService.MlResult mlResult = mlService.analyzeMessage(content);
            classification = mlResult.getClassification();
            System.out.println("[ML] Auto-classifié : " + mlResult.getLabel());
        } else {
            classification = comboValue;
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // NOUVEAUTÉ : Traitement de l'Alerte SOS si classé URGENT
        if ("URGENT".equals(classification)) {
            try {
                // 1. Création de l'intervention
                Intervention intervention = new Intervention();
                intervention.setMedecinId(selectedConv.getMedecinId());
                intervention.setType("Alerte SOS");
                intervention.setDescription("message classé URGENT");
                intervention.setStatut("En attente");
                intervention.setDatePlanifiee(LocalDateTime.now());
                intervention.setDateCompletion(LocalDateTime.now().plusHours(1));

                interventionService.add(intervention);

                // ...
// 2. Récupération du patient actuel (l'expéditeur)
                User sender = ManagerSession.getInstance().getCurrentUser();
                String nomCompletPatient = (sender != null) ? sender.getPrenom() + " " + sender.getNom() : "Un patient";

// 3. Envoi de l'email au médecin de la conversation
                User medecin = new UserService().getUserById(selectedConv.getMedecinId());
                if (medecin != null && medecin.getEmail() != null) {
                    new Thread(() -> {
                        // CORRECTION ICI : On passe bien les 4 paramètres !
                        sendUrgentEmailAlert(medecin.getEmail(), medecin.getNom(), nomCompletPatient, content);
                    }).start();
                }
// ...
            } catch (Exception e) {
                System.err.println("[Alerte SOS] Erreur critique lors du traitement de l'urgence : " + e.getMessage());
            }
        }

        com.cardiolink.Models.Message msg = new com.cardiolink.Models.Message();
        msg.setConversationId(selectedConv.getId());
        msg.setSenderId(currentUserId);
        msg.setContent(content);
        msg.setDate(now);
        msg.setRead(false);
        msg.setClassification(classification);
        msg.setPinned(false);
        msg.setArchived(false);

        try {
            messageService.add(msg);

            int recipientId = selectedConv.getPatientId() == currentUserId
                    ? selectedConv.getMedecinId()
                    : selectedConv.getPatientId();

            Notification notif = new Notification(
                    truncate(content, 80), false, now,
                    recipientId, currentUserId,
                    selectedConv.getId(), msg.getId()
            );
            notificationService.createNotification(notif);

            if (wsClient != null && wsClient.isOpen()) {
                wsClient.sendMessage(selectedConv.getId(), currentUserId, content, classification);
            }

            currentConversationMessages.add(msg);
            applyMessageSearch();

            lastContextMessage = msg;
            messageInput.clear();
            scrollToBottom();
            updateSuggestions(msg);

            selectedConv.setUpdated_at(now);
            conversationService.update(selectedConv);
            convList.refresh();

        } catch (SQLDataException e) {
            System.err.println("[ChatAdv] Erreur envoi : " + e.getMessage());
        }
    }

    private void updateSuggestions(com.cardiolink.Models.Message lastMsg) {
        if (lastMsg == null || lastMsg.getSenderId() == currentUserId) {
            hideSuggestions();
            return;
        }

        if (mlAvailable) {
            String content = lastMsg.getContent();
            Task<List<String>> task = new Task<>() {
                @Override
                protected List<String> call() {
                    return mlService.getSuggestedReplies(content);
                }
            };
            task.setOnSucceeded(e -> Platform.runLater(() -> showSuggestions(task.getValue())));
            task.setOnFailed(e -> Platform.runLater(() -> showStaticSuggestions(lastMsg.getClassification())));
            new Thread(task, "ml-suggestions").start();
        } else {
            showStaticSuggestions(lastMsg.getClassification());
        }
    }

    private void showSuggestions(List<String> pool) {
        if (pool == null || pool.isEmpty()) {
            hideSuggestions();
            return;
        }
        Button[] btns = {suggestion1, suggestion2, suggestion3};
        for (int i = 0; i < btns.length; i++) {
            if (i < pool.size()) {
                btns[i].setText(pool.get(i));
                btns[i].setVisible(true);
                btns[i].setManaged(true);
            } else {
                btns[i].setVisible(false);
                btns[i].setManaged(false);
            }
        }
        suggestionBar.setVisible(true);
        suggestionBar.setManaged(true);
    }

    private void showStaticSuggestions(String classification) {
        List<String> pool = SUGGESTIONS.getOrDefault(
                classification, SUGGESTIONS.get("DEFAULT"));
        showSuggestions(pool);
    }

    private void hideSuggestions() {
        suggestionBar.setVisible(false);
        suggestionBar.setManaged(false);
    }

    @FXML
    private void handleSuggestion1() {
        applySuggestion(suggestion1.getText());
    }

    @FXML
    private void handleSuggestion2() {
        applySuggestion(suggestion2.getText());
    }

    @FXML
    private void handleSuggestion3() {
        applySuggestion(suggestion3.getText());
    }

    private void applySuggestion(String text) {
        messageInput.setText(text);
        messageInput.positionCaret(text.length());
        hideSuggestions();
        handleSendMessage();
    }

    @FXML
    private void handleSearch() {
        String search = searchField.getText();
        String sortBy = getSortByParam();
        loadConversations(search, sortBy, sortOrder);
    }

    @FXML
    private void handleSort() {
        handleSearch();
    }

    @FXML
    private void handleSortOrder() {
        sortOrder = sortOrderBtn.isSelected() ? "ASC" : "DESC";
        sortOrderBtn.setText(sortOrderBtn.isSelected() ? "↑" : "↓");
        handleSearch();
    }

    private String getSortByParam() {
        String selected = sortCombo.getValue();
        if (selected == null) return "updated";
        return switch (selected) {
            case "Créé" -> "created";
            case "Contact" -> "contact";
            case "Statut" -> "status";
            default -> "updated";
        };
    }

    private void buildEmojiPopup() {
        emojiPopup = new Popup();
        HBox box = new HBox(4);
        box.getStyleClass().add("emoji-picker-popup");
        box.setPadding(new Insets(8));

        for (String emoji : EMOJI_LIST) {
            Button btn = new Button(emoji);
            btn.getStyleClass().add("emoji-btn");
            btn.setOnAction(e -> {
                emojiPopup.hide();
                if (selectedEmojiTargetMessage != null) {
                    reactionService.toggleReaction(
                            selectedEmojiTargetMessage.getId(), currentUserId, emoji);
                    if (wsClient != null && wsClient.isOpen()) {
                        wsClient.sendReaction(selectedEmojiTargetMessage.getId(),
                                currentUserId, emoji, true);
                    }
                    messageListView.refresh();
                    selectedEmojiTargetMessage = null;
                } else {
                    messageInput.insertText(messageInput.getCaretPosition(), emoji);
                }
            });
            box.getChildren().add(btn);
        }
        emojiPopup.getContent().add(box);
        emojiPopup.setAutoHide(true);
    }

    private com.cardiolink.Models.Message selectedEmojiTargetMessage = null;

    @FXML
    private void handleEmojiPickerToggle() {
        selectedEmojiTargetMessage = null;
        if (emojiPopup.isShowing()) {
            emojiPopup.hide();
        } else {
            var bounds = emojiPickerBtn.localToScreen(emojiPickerBtn.getBoundsInLocal());
            emojiPopup.show(emojiPickerBtn, bounds.getMinX(), bounds.getMinY() - 60);
        }
    }

    private void showEmojiPickerFor(com.cardiolink.Models.Message msg) {
        selectedEmojiTargetMessage = msg;
        var bounds = emojiPickerBtn.localToScreen(emojiPickerBtn.getBoundsInLocal());
        emojiPopup.show(emojiPickerBtn, bounds.getMinX(), bounds.getMinY() - 60);
    }

    public void onWsMessageReceived(JSONObject json) {
        int convId = json.optInt("conversationId", -1);
        int senderId = json.optInt("senderId", -1);
        String content = json.optString("content", "");
        String classif = json.optString("classification", "NORMAL");

        if (convId < 0 || senderId == currentUserId) return;

        if (selectedConv == null || convId != selectedConv.getId()) {
            refreshGlobalNotifBadge();
            convList.refresh();
            return;
        }

        com.cardiolink.Models.Message received = new com.cardiolink.Models.Message();
        received.setConversationId(convId);
        received.setSenderId(senderId);
        received.setContent(content);
        received.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        received.setRead(false);
        received.setClassification(classif);
        received.setPinned(false);
        received.setArchived(false);

        try {
            messageService.add(received);
            System.out.println("[ChatAdv] Message sauvegardé en BD : " + received.getId());
        } catch (Exception e) {
            System.err.println("[ChatAdv] Erreur lors de la sauvegarde du message : " + e.getMessage());
        }

        String receivedDate = received.getDate();
        int insertIndex = currentConversationMessages.size();
        for (int i = 0; i < currentConversationMessages.size(); i++) {
            if (receivedDate.compareTo(currentConversationMessages.get(i).getDate()) < 0) {
                insertIndex = i;
                break;
            }
        }

        currentConversationMessages.add(insertIndex, received);
        applyMessageSearch();

        lastContextMessage = received;
        updateSuggestions(received);
        scrollToBottom();

        messageService.markAllAsReadByConversation(convId, currentUserId);
    }

    public void onWsMessageEdited(JSONObject json) {
        int msgId = json.optInt("messageId", -1);
        String content = json.optString("content", "");
        if (msgId < 0) return;

        currentConversationMessages.stream()
                .filter(m -> m.getId() == msgId)
                .findFirst()
                .ifPresent(m -> m.setContent(content));

        applyMessageSearch();
    }

    public void onWsMessageDeleted(JSONObject json) {
        int msgId = json.optInt("messageId", -1);
        if (msgId < 0) return;

        currentConversationMessages.removeIf(m -> m.getId() == msgId);
        applyMessageSearch();
    }

    public void onWsReactionReceived(JSONObject json) {
        int msgId = json.optInt("messageId", -1);
        if (msgId < 0) return;
        messageListView.refresh();
    }

    public void onWsPinChanged(JSONObject json) {
        int msgId = json.optInt("messageId", -1);
        boolean pin = json.optBoolean("isPinned", false);
        if (msgId < 0) return;

        messageService.setPinned(msgId, pin);

        currentConversationMessages.stream()
                .filter(m -> m.getId() == msgId)
                .findFirst()
                .ifPresent(m -> m.setPinned(pin));

        applyMessageSearch();
    }

    public void onWsArchiveChanged(JSONObject json) {
        int msgId = json.optInt("messageId", -1);
        boolean archived = json.optBoolean("isArchived", false);
        if (msgId < 0) return;

        messageService.setArchived(msgId, archived);

        currentConversationMessages.stream()
                .filter(m -> m.getId() == msgId)
                .findFirst()
                .ifPresent(m -> m.setArchived(archived));

        applyMessageSearch();
    }

    public void onWsReadReceipt(JSONObject json) {
        messageListView.refresh();
    }

    public void onWsTyping(JSONObject json) {
        boolean isTyping = json.optBoolean("isTyping", false);
        typingLabel.setVisible(isTyping);
        typingLabel.setManaged(isTyping);
        typingLabel.setText("✏️ En train d'écrire...");
    }

    public void onWsDisconnected() {
        contactStatusLabel.setText("⚠️ Connexion temps réel perdue");
        System.out.println("[ChatAdv] WebSocket déconnecté - reconnexion dans 3s...");

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        pause.setOnFinished(event -> {
            connectWebSocket();
            if (selectedConv != null) {
                loadMessages(activeFilter);
                contactStatusLabel.setText("✅ Connecté");
            }
        });
        pause.play();
    }

    public void onWsError(String error) {
        System.err.println("[ChatAdv] WS Error : " + error);
    }

    private void refreshGlobalNotifBadge() {
        int total = notificationService.countUnread(currentUserId);
        globalNotifBadge.setText(String.valueOf(total));
        globalNotifBadge.setVisible(total > 0);
        globalNotifBadge.setManaged(total > 0);
    }

    private void setupTypingTimer() {
        typingTimer = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
        typingTimer.setOnFinished(e -> {
            if (wsClient != null && wsClient.isOpen() && selectedConv != null)
                wsClient.sendTyping(selectedConv.getId(), currentUserId, false);
        });
    }

    @FXML
    private void handleTyping() {
        if (wsClient != null && wsClient.isOpen() && selectedConv != null) {
            wsClient.sendTyping(selectedConv.getId(), currentUserId, true);
            typingTimer.playFromStart();
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            User user = ManagerSession.getInstance().getCurrentUser();
            String fxml = (user != null && user.getRoleClean().contains("MEDECIN"))
                    ? "/dashboard_admin.fxml"
                    : "/dashboard_patient.fxml";
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxml))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectWebSocket() {
        try {
            URI uri = new URI("ws://localhost:3001");
            wsClient = new ChatWebSocketClientAdvanced(uri, this);
            wsClient.connect();
        } catch (Exception e) {
            System.err.println("[ChatAdv] Connexion WS échouée : " + e.getMessage());
        }
    }

    private void setupSortCombo() {
        sortCombo.getSelectionModel().selectFirst();
    }

    private void setupClassificationCombo() {
        classificationCombo.getSelectionModel().selectFirst();
    }

    private void scrollToBottom() {
        if (!messageListView.getItems().isEmpty()) {
            messageListView.scrollTo(messageListView.getItems().size() - 1);
        }
    }

    private String getInitials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isEmpty()) ? prenom.substring(0, 1).toUpperCase() : "";
        String n = (nom != null && !nom.isEmpty()) ? nom.substring(0, 1).toUpperCase() : "";
        return p + n;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "…" : text;
    }

    private String formatTime(String datetime) {
        if (datetime == null || datetime.isEmpty()) return "";
        try {
            DateTimeFormatter in = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter out = DateTimeFormatter.ofPattern("HH:mm");
            return LocalDateTime.parse(datetime.length() > 19
                    ? datetime.substring(0, 19) : datetime, in).format(out);
        } catch (Exception e) {
            return datetime.length() > 10 ? datetime.substring(11, 16) : datetime;
        }
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }
}