package com.cardiolink.Controllers;

import com.cardiolink.Models.Comment;
import com.cardiolink.Models.Post;
import com.cardiolink.Services.ServiceComment;
import com.cardiolink.Services.ServicePost;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.List;

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

    private Comment selectedComment; // Pour stocker le commentaire en cours de modif
    private Post selectedPost;
    private final ServicePost servicePost = new ServicePost();
    private final ServiceComment serviceComment = new ServiceComment();
    private final int CURRENT_USER_ID = 2; // Simulé pour CardioLink

    @FXML
    public void initialize() {
        // Configuration du filtrage
        filterBox.getItems().clear();
        filterBox.getItems().addAll("Tout", "Titre", "Date", "24h", "7j");
        filterBox.setValue("Tout");
        filterBox.setOnAction(e -> filterPosts());

        // Recherche dynamique en temps réel
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                if (newV == null || newV.trim().isEmpty()) {
                    loadPosts();
                } else {
                    render(servicePost.searchByTitle(newV));
                }
            } catch (Exception e) { e.printStackTrace(); }
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
        // 1. Récupération des saisies
        String titre = titleField.getText().trim();
        String contenu = contentField.getText().trim();

        // 2. VALIDATION (Le garde-barrière du MVC)
        StringBuilder errors = new StringBuilder();
        if (titre.isEmpty()) {
            errors.append("- Le titre est obligatoire.\n");
        }
        if (contenu.isEmpty()) {
            errors.append("- Le contenu ne peut pas être vide.\n");
        }

        // Si des erreurs sont présentes, on affiche l'alerte et on arrête
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation");
            alert.setHeaderText("Champs manquants");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return; // On sort de la méthode, le service n'est pas appelé
        }

        // 3. LOGIQUE MÉTIER (Appel au Service)
        try {
            Post p = new Post();
            p.setTitle(titre);
            p.setContent(contenu);
            p.setUser_id(CURRENT_USER_ID);
            p.setCreated_at(java.time.LocalDateTime.now());

            servicePost.add(p);

            // 4. MISE À JOUR DE LA VUE
            titleField.clear();
            contentField.clear();
            loadPosts();
            loadStats();

            System.out.println("Publication réussie !");
        } catch (Exception e) {
            e.printStackTrace();
            // Optionnel : Alerte en cas d'erreur SQL
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement en base de données.").show();
        }
    }

    private void render(List<Post> posts) {
        postContainer.getChildren().clear();
        for (Post p : posts) {
            postContainer.getChildren().add(createPostCard(p));
        }
    }

    private VBox createPostCard(Post p) {

        VBox card = new VBox(15);

        card.setMaxWidth(540);

        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 18; " +

                "-fx-border-color: #e5e7eb; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);");

        // --- EN-TÊTE ---

        HBox header = new HBox(12);

        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane(new Circle(20, Color.web("#2F60F5")), new Text("U") {{ setFill(Color.WHITE); }});

        VBox infos = new VBox(new Label("Utilisateur " + p.getUser_id()) {{ setStyle("-fx-font-weight: bold;"); }},

                new Label("le " + p.getCreated_at()) {{ setStyle("-fx-font-size: 10; -fx-text-fill: gray;"); }});

        header.getChildren().addAll(avatar, infos);



        // --- CORPS ---

        VBox body = new VBox(8);

        if (p.getTitle() != null && !p.getTitle().isEmpty()) {

            body.getChildren().add(new Label(p.getTitle()) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 16;"); }});

        }

        body.getChildren().add(new Label(p.getContent()) {{ setWrapText(true); }});

        // --- ZONE COMMENTAIRES (Style Facebook) ---
        VBox commentArea = new VBox(10);
        commentArea.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 12;");

        VBox commentsList = new VBox(8);
        try {
            List<Comment> comments = serviceComment.getByPostId(p.getId());
            for (Comment c : comments) {
                commentsList.getChildren().add(createCommentItem(c));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Barre d'ajout de commentaire
        HBox inputBar = new HBox(8);
        TextField commInput = new TextField();
        commInput.setPromptText("Écrire un commentaire...");
        commInput.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 15;");
        HBox.setHgrow(commInput, Priority.ALWAYS);

        Button sendComm = new Button("➤");
        sendComm.setStyle("-fx-background-color: #2F60F5; -fx-text-fill: white; -fx-background-radius: 50; -fx-cursor: hand;");
        sendComm.setOnAction(e -> handleAddComment(p.getId(), commInput.getText()));

        inputBar.getChildren().addAll(commInput, sendComm);
        commentArea.getChildren().addAll(commentsList, inputBar);

        // --- ACTIONS DU POST (Propriétaire uniquement) ---
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (p.getUser_id() == CURRENT_USER_ID) {
            Button btnEdit = new Button("Modifier") {{ setOnAction(e -> openEditModal(p)); setStyle("-fx-background-color: #f3f4f6; -fx-cursor: hand;"); }};
            Button btnDel = new Button("Supprimer") {{ setOnAction(e -> handleDeletePost(p)); setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-cursor: hand;"); }};
            actions.getChildren().addAll(btnEdit, btnDel);
        }

        card.getChildren().addAll(header, body, commentArea, actions);
        return card;
    }

    // ================= GESTION DES COMMENTAIRES INDIVIDUELS =================

    private HBox createCommentItem(Comment c) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = new VBox(2);
        bubble.setStyle("-fx-background-color: #e4e6eb; -fx-padding: 8 12; -fx-background-radius: 15;");
        bubble.getChildren().addAll(
                new Label("User " + c.getUser_id()) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 11;"); }},
                new Label(c.getContent()) {{ setWrapText(true); setStyle("-fx-font-size: 13;"); }}
        );

        row.getChildren().add(bubble);

        // Menu 3 points pour modif/suppr (Si propriétaire du commentaire)
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

            // 2. VALIDATION
            StringBuilder errors = new StringBuilder();
            if (nouveauTitre.isEmpty()) {
                errors.append("- Le titre ne peut pas être vide.\n");
            }
            if (nouveauContenu.isEmpty()) {
                errors.append("- Le contenu ne peut pas être vide.\n");
            }

            if (errors.length() > 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Modification");
                alert.setHeaderText("Données invalides");
                alert.setContentText(errors.toString());
                alert.showAndWait();
                return; // On arrête tout
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
        String messageErreur = "";

        if (titre == null || titre.trim().isEmpty()) {
            messageErreur += "- Le titre est obligatoire.\n";
        }
        if (contenu == null || contenu.trim().isEmpty()) {
            messageErreur += "- Le contenu ne peut pas être vide.\n";
        }

        if (!messageErreur.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Champs invalides");
            alert.setContentText(messageErreur);
            alert.showAndWait();
            return false;
        }
        return true;
    }
}