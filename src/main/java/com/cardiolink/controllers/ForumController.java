package com.cardiolink.controllers;

import com.cardiolink.Models.Post;
import com.cardiolink.Models.Comment;
import com.cardiolink.Services.ServicePost;
import com.cardiolink.Services.ServiceComment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.List;

public class ForumController {

    // ================= VIEW =================
    @FXML private VBox postContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;

    @FXML private TextField titleField;
    @FXML private TextField contentField;
    @FXML
    private VBox statsBox;
    // ================= SERVICE =================
    private final ServicePost servicePost = new ServicePost();
    private final ServiceComment serviceComment = new ServiceComment();

    private final int CURRENT_USER_ID = 2;

    // ================= INIT =================
    @FXML
    public void initialize() {

        filterBox.getItems().addAll(
                "All",
                "Title",
                "Date",
                "24h",
                "7d",
                "30d"
        );

        filterBox.setOnAction(e -> filterPosts());

        loadPosts();
        loadStats();

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                if (newV == null || newV.isEmpty()) {
                    loadPosts();
                } else {
                    render(servicePost.searchByTitle(newV));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ================= LOAD POSTS =================
    @FXML
    public void loadPosts() {
        try {
            render(servicePost.getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD POST =================
    @FXML
    public void addPost() {
        try {
            Post p = new Post();
            p.setTitle(titleField.getText());
            p.setContent(contentField.getText());
            p.setUser_id(CURRENT_USER_ID);
            p.setCreated_at(java.time.LocalDateTime.now());

            servicePost.add(p);

            titleField.clear();
            contentField.clear();

            loadPosts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FILTER =================
    @FXML
    public void filterPosts() {
        try {

            String choice = filterBox.getValue();

            if (choice == null || choice.equals("All")) {
                loadPosts();

            } else if (choice.equals("Title")) {
                render(servicePost.sortPosts("title"));

            } else if (choice.equals("Date")) {
                render(servicePost.sortPosts("date"));

            } else {
                render(servicePost.filterByPeriod(choice));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= RENDER =================
    private void render(List<Post> posts) {

        postContainer.getChildren().clear();

        for (Post p : posts) {

            VBox card = new VBox();
            card.setSpacing(8);
            card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:15;");

            // ================= POST INFO =================
            Label title = new Label(p.getTitle());
            Label content = new Label(p.getContent());
            Label date = new Label(String.valueOf(p.getCreated_at()));
            Label user = new Label("User " + p.getUser_id());

            card.getChildren().addAll(title, content, date, user);

            // ================= LOAD COMMENTS =================
            try {
                List<Comment> comments = serviceComment.getByPostId(p.getId());

                for (Comment c : comments) {

                    VBox commentBox = new VBox();
                    commentBox.setSpacing(5);
                    commentBox.setStyle("-fx-background-color:#f5f6fa; -fx-padding:10; -fx-background-radius:10;");

                    Label cUser = new Label("User " + c.getUser_id());
                    Label cContent = new Label(c.getContent());

                    commentBox.getChildren().addAll(cUser, cContent);

                    // ================= ACTIONS (OWNER ONLY) =================
                    if (c.getUser_id() == CURRENT_USER_ID) {

                        HBox actions = new HBox();
                        actions.setSpacing(10);

                        Button editBtn = new Button("Modifier");
                        Button delBtn = new Button("Delete");

                        // ===== DELETE =====
                        delBtn.setOnAction(e -> {
                            try {
                                serviceComment.delete(c);
                                loadPosts();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });

                        // ===== EDIT =====
                        editBtn.setOnAction(e -> {
                            TextInputDialog dialog = new TextInputDialog(c.getContent());
                            dialog.setHeaderText("Modifier commentaire");

                            dialog.showAndWait().ifPresent(newText -> {
                                try {

                                    if (newText == null || newText.trim().isEmpty()) return;

                                    c.setContent(newText);
                                    serviceComment.update(c);

                                    loadPosts(); // refresh UI

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                        });

                        actions.getChildren().addAll(editBtn, delBtn);
                        commentBox.getChildren().add(actions);
                    }

                    card.getChildren().add(commentBox);
                }

// ================= ADD COMMENT =================
                TextField commentInput = new TextField();
                commentInput.setPromptText("Write a comment...");

                Button send = new Button("Send");

                send.setOnAction(e -> {
                    try {

                        String text = commentInput.getText();

                        if (text == null || text.trim().isEmpty()) {
                            return;
                        }

                        Comment c = new Comment();
                        c.setContent(text);
                        c.setPost_id(p.getId());
                        c.setUser_id(CURRENT_USER_ID);
                        c.setCreated_at(java.time.LocalDateTime.now());

                        serviceComment.add(c);

                        loadPosts();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                card.getChildren().addAll(commentInput, send);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // ================= POST ACTIONS =================
            if (p.getUser_id() == CURRENT_USER_ID) {

                Button editBtn = new Button("Modifier");
                Button deleteBtn = new Button("Supprimer");

                editBtn.setOnAction(e -> editPost(p));
                deleteBtn.setOnAction(e -> deletePost(p));

                card.getChildren().addAll(editBtn, deleteBtn);
            }

            postContainer.getChildren().add(card);
        }
    }

    // ================= DELETE POST =================
    private void deletePost(Post p) {
        try {
            servicePost.delete(p);
            loadPosts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= EDIT POST =================
    private void editPost(Post p) {
        try {

            TextInputDialog d1 = new TextInputDialog(p.getTitle());
            d1.setHeaderText("Modifier titre");

            d1.showAndWait().ifPresent(title -> {

                TextInputDialog d2 = new TextInputDialog(p.getContent());
                d2.setHeaderText("Modifier contenu");

                d2.showAndWait().ifPresent(content -> {

                    try {
                        p.setTitle(title);
                        p.setContent(content);

                        servicePost.update(p);
                        loadPosts();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadStats() {

        try {
            statsBox.getChildren().clear();

            int myPosts = servicePost.countPostsByUser(CURRENT_USER_ID);
            int recentPosts = servicePost.getRecentPosts().size();

            VBox card1 = createStatCard("Mes Posts", String.valueOf(myPosts));
            VBox card2 = createStatCard("Posts récents (7j)", String.valueOf(recentPosts));

            statsBox.getChildren().addAll(card1, card2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private VBox createStatCard(String title, String value) {

        VBox card = new VBox();
        card.setSpacing(5);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2f6bff;");

        card.getChildren().addAll(titleLabel, valueLabel);

        return card;
    }
}
