package com.cardiolink.Controllers;

import com.cardiolink.Models.Comment;
import com.cardiolink.Models.Post;
import com.cardiolink.Services.ServiceComment;
import com.cardiolink.Services.ServicePost;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.Services.AIService;
import java.sql.SQLException;
public class ForumController {

    @FXML private VBox postContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;
    @FXML private TextField titleField;
    @FXML private TextArea contentField;
    @FXML private VBox statsBox;

    // Éléments de la Modale de modification
    @FXML private VBox editBox;
    @FXML private TextField editTitleField;
    @FXML private TextArea editContentField;
    @FXML private VBox editCommentBox;
    @FXML private TextArea editCommentField;

    @FXML
    private Label imageLabel;
    private Comment selectedComment; // Pour stocker le commentaire en cours de modif
    private Post selectedPost;
    private final ServicePost servicePost = new ServicePost();
    private  UserService userService = new UserService();
    private final ServiceComment serviceComment = new ServiceComment();
    private int CURRENT_USER_ID;
    private User currentUser;
    AIService aiService = new AIService();


    //private final int CURRENT_USER_ID = 7; // Simulé pour CardioLink
    private String selectedImagePath = null;

    private String savedImageName = null;

    @FXML
    public void initialize() {

        // 🔥 USER CONNECTÉ (UNE SEULE FOIS)
        CURRENT_USER_ID = ManagerSession.getInstance().getCurrentUserId();

        try {
            currentUser = userService.getUserById(CURRENT_USER_ID);
            System.out.println("User connecté: " + currentUser);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // ================= FILTER =================
        filterBox.getItems().clear();
        filterBox.getItems().addAll("Tout", "Titre", "Date", "24h", "7j");
        filterBox.setValue("Tout");
        filterBox.setOnAction(e -> filterPosts());

        // ================= SEARCH =================
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                if (newV == null || newV.trim().isEmpty()) {
                    loadPosts();
                } else {
                    render(servicePost.searchByTitle(newV));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loadPosts();
        loadStats();
    }

    // ================= GESTION DES PUBLICATIONS (POSTS) =================

    @FXML
    public void loadPosts() {
        try {
            render(servicePost.getAll());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void addPost() {

        String titre = titleField.getText().trim();
        String contenu = contentField.getText().trim();

        if (!estSaisieValide(titre, contenu)) {
            return;
        }

        try {
            Post p = new Post();
            p.setTitle(titre);
            p.setContent(contenu);
            p.setUser_id(CURRENT_USER_ID);
            p.setCreated_at(java.time.LocalDateTime.now());

            // ⭐ IMAGE PROPRE MVC
            if (selectedImagePath != null) {
                String imageName = saveImageToUploads(selectedImagePath);
                p.setImage(imageName);
            } else {
                p.setImage(null);
            }

            servicePost.add(p);

            // reset UI
            titleField.clear();
            contentField.clear();
            selectedImagePath = null;
            imageLabel.setText("Aucune image sélectionnée");

            loadPosts();
            loadStats();

            System.out.println("Publication réussie !");

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur lors de l'enregistrement en base de données."
            ).show();
        }
    }

    private void render(List<Post> posts) {

        postContainer.getChildren().clear();

        for (Post p : posts) {
            try {
                List<Comment> comments = serviceComment.getByPostId(p.getId());
                postContainer.getChildren().add(
                        createPostCard(p, comments)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createPostCard(Post p, List<Comment> comments) {

        VBox card = new VBox(15);
        card.setMaxWidth(540);

        card.setStyle(
                "-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 18; " +
                        "-fx-border-color: #e5e7eb; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);"
        );

        // ================= HEADER =================

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane(
                new Circle(20, Color.web("#2F60F5")),
                new Text("U") {{
                    setFill(Color.WHITE);
                }}
        );

        User postUser = null;

        try {
            postUser = userService.getUserById(p.getUser_id());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fullName = "Utilisateur inconnu";

        if (postUser != null) {
            fullName = postUser.getNom()  + " " + postUser.getPrenom();
        }

        Label nameLabel = new Label(fullName);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label dateLabel = new Label("le " + p.getCreated_at());
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");

        VBox infos = new VBox(nameLabel, dateLabel);

        header.getChildren().addAll(avatar, infos);

// ===== FLAME =====
        int flames = 0;
        try {
            flames = servicePost.getUserFlames(p.getUser_id());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (flames > 0) {
            Label flameLabel = new Label("🔥 " + flames);
            flameLabel.setStyle("-fx-text-fill: #ff6b00; -fx-font-weight: bold;");
            header.getChildren().add(flameLabel);
        }


        // ================= BODY =================
        VBox body = new VBox(8);

        if (p.getTitle() != null && !p.getTitle().isEmpty()) {
            body.getChildren().add(new Label(p.getTitle()) {{
                setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            }});
        }

        body.getChildren().add(new Label(p.getContent()) {{
            setWrapText(true);
        }});
        // --- LOGIQUE RÉSUMÉ IA ---
        String[] words = p.getContent().split("\\s+");
        if (words.length > 200) {
            Button aiBtn = new Button("✨ Résumé IA");
            Label aiResult = new Label();
            aiResult.setWrapText(true);
            aiResult.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic; -fx-padding: 5 0;");

            aiBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");

            // L'ACTION DU BOUTON
            aiBtn.setOnAction(e -> {
                aiBtn.setDisable(true);
                aiBtn.setText("⏳ Analyse IA...");

                // On lance l'IA dans un thread séparé pour ne pas bloquer l'écran
                javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
                    @Override
                    protected String call() {
                        AIService aiService = new AIService();
                        return aiService.getSummary(p.getContent());
                    }
                };

                task.setOnSucceeded(event -> {
                    aiResult.setText("Résumé IA : " + task.getValue());
                    aiBtn.setText("✨ Résumé IA");
                    aiBtn.setDisable(false);
                });

                task.setOnFailed(event -> {
                    aiResult.setText("❌ Erreur lors du résumé.");
                    aiBtn.setDisable(false);
                });

                new Thread(task).start();
            });

            body.getChildren().addAll(aiBtn, aiResult);
        }
        // --- IMAGE (optionnelle) ---
        if (p.getImage() != null && !p.getImage().isEmpty()) {

            File file = new File("uploads", p.getImage());

            System.out.println("IMAGE PATH = " + file.getAbsolutePath());
            System.out.println("EXISTS = " + file.exists());

            if (file.exists()) {

                Image image = new Image(file.toURI().toString(), false);

                ImageView img = new ImageView(image);

                img.setFitWidth(400);
                img.setPreserveRatio(true);
                img.setSmooth(true);
                img.setCache(true);

                body.getChildren().add(img);

            } else {
                System.out.println("❌ Image introuvable sur disque");
            }
        }
        // ================= LIKE / DISLIKE =================
        HBox reactionBox = new HBox(15);
        reactionBox.setAlignment(Pos.CENTER_LEFT);

        int postId = p.getId();

// -------- LIKE --------
        Button likeBtn = new Button("👍 Like");
        likeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        Label likeCount = new Label();

        try {
            likeCount.setText(String.valueOf(servicePost.countLikes(postId)));

            boolean isLiked = servicePost.isLikedByUser(postId, CURRENT_USER_ID);

            if (isLiked) {
                likeBtn.setText("❤️ Liked");
                likeBtn.setStyle("-fx-text-fill: #ef4444; -fx-background-color: transparent;");
            }

        } catch (Exception e) {
            likeCount.setText("0");
        }

        likeBtn.setOnAction(e -> handleLike(p));


// -------- DISLIKE --------
        Button dislikeBtn = new Button("👎 Dislike");
        dislikeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        Label dislikeCount = new Label();

        try {
            dislikeCount.setText(String.valueOf(servicePost.countDislikes(postId)));

            boolean isDisliked = servicePost.isDislikedByUser(postId, CURRENT_USER_ID);

            if (isDisliked) {
                dislikeBtn.setText("💔 Disliked");
                dislikeBtn.setStyle("-fx-text-fill: gray; -fx-background-color: transparent;");
            }

        } catch (Exception e) {
            dislikeCount.setText("0");
        }

        dislikeBtn.setOnAction(e -> handleDislike(p));

// add to box
        reactionBox.getChildren().addAll(
                likeBtn, likeCount,
                dislikeBtn, dislikeCount
        );
        // ================= COMMENTS =================
        VBox commentArea = new VBox(10);
        commentArea.setStyle(
                "-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 12;"
        );

        VBox commentsList = new VBox(8);

        for (Comment c : comments) {
            commentsList.getChildren().add(createCommentItem(c));
        }

        // input comment
        HBox inputBar = new HBox(8);

        TextField commInput = new TextField();
        commInput.setPromptText("Écrire un commentaire...");
        HBox.setHgrow(commInput, Priority.ALWAYS);

        Button sendComm = new Button("➤");
        sendComm.setStyle(
                "-fx-background-color: #2F60F5; -fx-text-fill: white; " +
                        "-fx-background-radius: 50; -fx-cursor: hand;"
        );

        sendComm.setOnAction(e ->
                handleAddComment(p.getId(), commInput.getText())
        );

        inputBar.getChildren().addAll(commInput, sendComm);
        commentArea.getChildren().addAll(commentsList, inputBar);

        // ================= ACTIONS =================
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (p.getUser_id() == CURRENT_USER_ID) {

            Button btnEdit = new Button("Modifier");
            btnEdit.setOnAction(e -> openEditModal(p));
            btnEdit.setStyle("-fx-background-color: #f3f4f6; -fx-cursor: hand;");

            Button btnDel = new Button("Supprimer");
            btnDel.setOnAction(e -> handleDeletePost(p));
            btnDel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-cursor: hand;");

            actions.getChildren().addAll(btnEdit, btnDel);
        }

        // ================= FINAL CARD =================
        card.getChildren().addAll(header, body, reactionBox, commentArea, actions);
        return card;
    }
    // ================= GESTION DES COMMENTAIRES INDIVIDUELS =================

    private HBox createCommentItem(Comment c) {

        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        // 🔥 récupérer user
        User user = null;

        try {
            user = userService.getUserById(c.getUser_id());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fullName = "Utilisateur inconnu";

        if (user != null) {
            fullName = user.getNom() + " " + user.getPrenom();
        }

        VBox bubble = new VBox(2);
        bubble.setStyle("-fx-background-color: #e4e6eb; -fx-padding: 8 12; -fx-background-radius: 15;");

        Label nameLabel = new Label(fullName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");

        Label contentLabel = new Label(c.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13;");

        bubble.getChildren().addAll(nameLabel, contentLabel);

        row.getChildren().add(bubble);

        // ================= MENU ACTIONS =================
        if (c.getUser_id() == CURRENT_USER_ID) {

            MenuButton options = new MenuButton("⋮");
            options.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #65676b;");

            MenuItem editItem = new MenuItem("Modifier");
            MenuItem deleteItem = new MenuItem("Supprimer");

            editItem.setOnAction(e -> handleEditComment(c));
            deleteItem.setOnAction(e -> handleDeleteComment(c));

            options.getItems().addAll(editItem, deleteItem);
            row.getChildren().add(options);
        }

        return row;
    }

    // ================= LOGIQUE MODALE & ACTIONS SERVICES =================

    private void openEditModal(Post p) {
        this.selectedPost = p;
        editTitleField.setText(p.getTitle());
        editContentField.setText(p.getContent());
        editBox.setVisible(true);
        editBox.setManaged(true);
        postContainer.setOpacity(0.3);
    }

    @FXML
    public void updatePost() {
        if (selectedPost != null) {

            // 1. Récupération des saisies dans la modale
            String nouveauTitre = editTitleField.getText().trim();
            String nouveauContenu = editContentField.getText().trim();

            // 2. Validation centralisée
            if (!estSaisieValide(nouveauTitre, nouveauContenu)) {
                return; // arrêt si invalide
            }


            // 3. MISE À JOUR (Service)
            try {
                selectedPost.setTitle(nouveauTitre);
                selectedPost.setContent(nouveauContenu);

                servicePost.update(selectedPost);

                // 4. FERMETURE ET RAFRAÎCHISSEMENT
                cancelEdit(); // Ferme la modale et remet l'opacité à 1.0
                loadPosts();  // Recharge la liste pour voir les modifs

            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour.").show();
            }
        }
    }

    @FXML
    public void cancelEdit() {
        editBox.setVisible(false);
        editBox.setManaged(false);
        postContainer.setOpacity(1.0);
        selectedPost = null;
    }

    private void handleAddComment(int postId, String text) {
        // 1. Validation de la saisie (Contrôle obligatoire)
        if (text == null || text.trim().isEmpty()) {
            // Affichage d'un message d'erreur clair
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText(null);
            alert.setContentText("Le commentaire ne peut pas être vide !");
            alert.showAndWait();
            return; // On arrête l'exécution ici
        }

        try {
            // 2. Préparation de l'objet (Modèle)
            Comment c = new Comment();
            c.setContent(text.trim()); // .trim() enlève les espaces inutiles au début/fin
            c.setPost_id(postId);
            c.setUser_id(CURRENT_USER_ID); // ID de l'utilisateur connecté

            // 3. Appel du Service (Logique métier / SQL)
            serviceComment.add(c);

            // 4. Mise à jour de la Vue
            loadPosts(); // On recharge les posts pour afficher le nouveau commentaire

        } catch (Exception e) {
            // Message d'erreur technique si le service échoue
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de l'ajout du commentaire en base de données.");
            alert.show();
        }
    }
    private void handleEditComment(Comment c) {
        this.selectedComment = c;
        editCommentField.setText(c.getContent());

        editCommentBox.setVisible(true);
        editCommentBox.setManaged(true);
        postContainer.setOpacity(0.3); // Effet visuel de focus
    }

    private void handleDeleteComment(Comment c) {
        try {
            serviceComment.delete(c);
            loadPosts();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDeletePost(Post p) {
        try {
            servicePost.delete(p);
            loadPosts();
            loadStats();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void filterPosts() {
        try {
            String choice = filterBox.getValue();
            if (choice == null || choice.equals("Tout")) loadPosts();
            else if (choice.equals("Titre")) render(servicePost.sortPosts("title"));
            else if (choice.equals("Date")) render(servicePost.sortPosts("date"));
            else render(servicePost.filterByPeriod(choice));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStats() {
        try {
            if (statsBox != null) {
                statsBox.getChildren().clear();
                int count = servicePost.countPostsByUser(CURRENT_USER_ID);
                VBox statCard = new VBox(
                        new Label("Mes Publications"),
                        new Label(String.valueOf(count)) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: #2F60F5;"); }}
                );
                statCard.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 15; -fx-alignment: center; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
                statsBox.getChildren().add(statCard);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    public void updateComment() {
        if (selectedComment != null) {
            String nouveauContenu = editCommentField.getText().trim();

            // VALIDATION
            if (nouveauContenu.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Le contenu du commentaire est obligatoire.");
                alert.showAndWait();
                return; // On empêche la fermeture et l'envoi au service
            }

            try {
                selectedComment.setContent(nouveauContenu);
                serviceComment.update(selectedComment);

                // On ferme la modale de commentaire uniquement si c'est OK
                cancelEditComment();
                loadPosts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void cancelEditComment() {
        editCommentBox.setVisible(false);
        editCommentBox.setManaged(false);
        postContainer.setOpacity(1.0);
        selectedComment = null;
    }
    private boolean estSaisieValide(String titre, String contenu) {
        StringBuilder messageErreur = new StringBuilder();

        // ===== TITRE =====
        if (titre == null || titre.trim().isEmpty()) {
            messageErreur.append("- Le titre est obligatoire.\n");
        } else {
            titre = titre.trim();

            if (titre.length() < 3) {
                messageErreur.append("- Le titre doit contenir au moins 3 caractères.\n");
            }

            if (titre.length() > 100) {
                messageErreur.append("- Le titre ne doit pas dépasser 100 caractères.\n");
            }

            if (!titre.matches(".*[a-zA-ZÀ-ÿ].*")) {
                messageErreur.append("- Le titre doit contenir au moins des lettres.\n");
            }

            if (titre.matches("(.)\\1{4,}")) {
                messageErreur.append("- Le titre contient une répétition invalide.\n");
            }
        }

        // ===== CONTENU =====
        if (contenu == null || contenu.trim().isEmpty()) {
            messageErreur.append("- Le contenu ne peut pas être vide.\n");
        } else {
            contenu = contenu.trim();

            if (contenu.length() < 10) {
                messageErreur.append("- Le contenu doit contenir au moins 10 caractères.\n");
            }

            if (contenu.length() > 2000) {
                messageErreur.append("- Le contenu ne doit pas dépasser 2000 caractères.\n");
            }

            if (!contenu.matches(".*[a-zA-ZÀ-ÿ].*")) {
                messageErreur.append("- Le contenu doit contenir au moins des lettres.\n");
            }

            if (contenu.matches("(.)\\1{6,}")) {
                messageErreur.append("- Le contenu contient une répétition abusive.\n");
            }
        }

        // ===== AFFICHAGE ERREUR =====
        if (messageErreur.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Post invalide");
            alert.setContentText(messageErreur.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }
    @FXML
    private Button homeButton;
    @FXML
    private void goHome(ActionEvent event) {
        try {
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/dashboard_patient.fxml"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private Button dossierButton;
    @FXML
    private void goDossier(ActionEvent event) {
        try {
            Stage stage = (Stage) dossierButton.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/dossier_medical.fxml"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLike(Post p) {
        try {
            System.out.println("CLICK LIKE");
            System.out.println("POST ID = " + p.getId());
            System.out.println("USER ID = " + CURRENT_USER_ID);

            boolean liked = servicePost.toggleLike(p.getId(), CURRENT_USER_ID);

            System.out.println(liked ? "Liked 👍" : "Unliked 👎");

            loadPosts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void chooseImage() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            imageLabel.setText(file.getName());
        }
    }
    private String saveImageToUploads(String path) {

        try {
            File source = new File(path);

            String extension = path.substring(path.lastIndexOf("."));
            String imageName = java.util.UUID.randomUUID() + extension;

            File dest = new File("uploads/" + imageName);

            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return imageName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @FXML
    public void handleDislike(Post p) {
        try {
            boolean disliked = servicePost.toggleDislike(p.getId(), CURRENT_USER_ID);

            System.out.println(disliked ? "Disliked 👎" : "Undisliked 👍");

            loadPosts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}