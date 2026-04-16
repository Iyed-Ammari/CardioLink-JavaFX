package com.cardiolink.Controllers;

import com.cardiolink.Models.Produit;
import com.cardiolink.Services.ProduitService;
import com.cardiolink.utils.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ProduitFormAdminController implements Initializable {

    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label formSectionTitleLabel;
    @FXML private Button saveButton;
    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private ComboBox<String> categorieBox;
    @FXML private TextField imageUrlField;
    @FXML private TextArea descriptionArea;
    @FXML private Label messageLabel;
    @FXML private Label imageHintLabel;

    private final ProduitService produitService = new ProduitService();
    private Produit produitEnCours = null;

    private static final Pattern NOM_PATTERN = Pattern.compile(
            "^[A-Za-zÀ-ÿ0-9][A-Za-zÀ-ÿ0-9\\s''()\\-+°%/]{1,254}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://.+|file:[/\\\\]{1,3}.+|/uploads/.+)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final String STYLE_NORMAL =
            "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E5E7EB;" +
                    " -fx-border-width: 1.5; -fx-padding: 10 12; -fx-font-size: 14px;";

    private static final String STYLE_ERROR =
            "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #F82239;" +
                    " -fx-border-width: 2; -fx-padding: 10 12; -fx-font-size: 14px;" +
                    " -fx-background-color: rgba(248,34,57,0.04);";

    private static final String COMBO_NORMAL =
            "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1.5;";

    private static final String COMBO_ERROR =
            "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #F82239; -fx-border-width: 2;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<String> cats = new ArrayList<>(produitService.findExistingCategories());
        for (String fixed : List.of("MEDICAL", "ACCESSOIRE", "AUTRE")) {
            if (!cats.contains(fixed)) cats.add(fixed);
        }

        cats = cats.stream().distinct().sorted().collect(java.util.stream.Collectors.toList());
        categorieBox.setItems(FXCollections.observableArrayList(cats));

        appliquerModeAjout();
        resetStyles();
        configurerTextFormatters();
    }

    private void configurerTextFormatters() {
        prixField.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            return t.matches("\\d{0,8}(\\.\\d{0,2})?") ? change : null;
        }));

        stockField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,6}") ? change : null
        ));

        descriptionArea.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 500 ? change : null
        ));

        nomField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 255 ? change : null
        ));
    }

    public void setProduit(Produit produit) {
        this.produitEnCours = produit;
        if (produit == null) return;

        appliquerModeModification();
        nomField.setText(produit.getNom() != null ? produit.getNom() : "");
        prixField.setText(produit.getPrix() != null ? produit.getPrix().toPlainString() : "");
        stockField.setText(produit.getStock() != null ? String.valueOf(produit.getStock()) : "");
        categorieBox.setValue(produit.getCategorie());
        imageUrlField.setText(produit.getImageUrl() != null ? produit.getImageUrl() : "");
        descriptionArea.setText(produit.getDescription() != null ? produit.getDescription() : "");

        if (imageHintLabel != null) {
            imageHintLabel.setText("Modification du produit ID = " + produit.getId());
        }
    }

    private void appliquerModeAjout() {
        if (pageTitleLabel != null) pageTitleLabel.setText("Ajouter un produit");
        if (pageSubtitleLabel != null) pageSubtitleLabel.setText("Créer un nouveau produit pour le catalogue CardioLink");
        if (formSectionTitleLabel != null) formSectionTitleLabel.setText("Informations du produit");
        if (saveButton != null) saveButton.setText("Enregistrer");
    }

    private void appliquerModeModification() {
        if (pageTitleLabel != null) pageTitleLabel.setText("Modifier le produit");
        if (pageSubtitleLabel != null) pageSubtitleLabel.setText("Mettre à jour les informations du produit");
        if (formSectionTitleLabel != null) formSectionTitleLabel.setText("Modification du produit");
        if (saveButton != null) saveButton.setText("Mettre à jour");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image produit");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (PNG, JPG, JPEG, WEBP)", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        Stage stage = (Stage) nomField.getScene().getWindow();
        File f = fc.showOpenDialog(stage);

        if (f != null) {
            imageUrlField.setText(f.toURI().toString());
            if (imageHintLabel != null) imageHintLabel.setText("✅ Image : " + f.getName());
            imageUrlField.setStyle(STYLE_NORMAL);
        }
    }

    @FXML
    private void handleSave() {
        resetStyles();

        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        String desc = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";
        String prixTxt = prixField.getText() != null ? prixField.getText().trim() : "";
        String stockTxt = stockField.getText() != null ? stockField.getText().trim() : "";
        String categorie = categorieBox.getValue();
        String imageUrl = imageUrlField.getText() != null ? imageUrlField.getText().trim() : "";

        boolean valid = true;

        if (nom.isEmpty()) {
            erreur(nomField, "Le nom est obligatoire.");
            valid = false;
        } else if (nom.length() < 2) {
            erreur(nomField, "Le nom doit contenir au moins 2 caractères.");
            valid = false;
        } else if (!NOM_PATTERN.matcher(nom).matches()) {
            erreur(nomField, "Nom invalide : lettres, chiffres, espaces autorisés.");
            valid = false;
        }

        BigDecimal prix = null;
        if (prixTxt.isEmpty()) {
            erreur(prixField, "Le prix est obligatoire.");
            valid = false;
        } else {
            try {
                prix = new BigDecimal(prixTxt);
                if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                    erreur(prixField, "Le prix doit être > 0.");
                    valid = false;
                } else if (prix.compareTo(new BigDecimal("99999.99")) > 0) {
                    erreur(prixField, "Maximum 99 999.99 DT.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                erreur(prixField, "Format invalide (ex : 29.99).");
                valid = false;
            }
        }

        int stock = 0;
        if (stockTxt.isEmpty()) {
            erreur(stockField, "Le stock est obligatoire.");
            valid = false;
        } else {
            try {
                stock = Integer.parseInt(stockTxt);
                if (stock < 0) {
                    erreur(stockField, "Le stock doit être >= 0.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                erreur(stockField, "Entier valide requis.");
                valid = false;
            }
        }

        if (categorie == null || categorie.trim().isEmpty()) {
            categorieBox.setStyle(COMBO_ERROR);
            showError("La catégorie est obligatoire.");
            valid = false;
        }

        if (!imageUrl.isEmpty() && !URL_PATTERN.matcher(imageUrl).matches()) {
            erreur(imageUrlField, "URL invalide : http(s) ou fichier local requis.");
            valid = false;
        }

        if (!valid) return;

        try {
            if (produitEnCours == null) {
                if (produitService.existsByNom(nom)) {
                    erreur(nomField, "Un produit avec ce nom existe déjà.");
                    return;
                }

                Produit p = new Produit(
                        nom,
                        desc.isEmpty() ? null : desc,
                        prix,
                        stock,
                        imageUrl.isEmpty() ? null : imageUrl,
                        categorie
                );

                produitService.add2(p);
                showSuccess("✅ Produit « " + nom + " » créé (ID=" + p.getId() + ").");

            } else {
                if (produitService.existsByNomExcludingId(nom, produitEnCours.getId())) {
                    erreur(nomField, "Un autre produit avec ce nom existe déjà.");
                    return;
                }

                produitEnCours.setNom(nom);
                produitEnCours.setDescription(desc.isEmpty() ? null : desc);
                produitEnCours.setPrix(prix);
                produitEnCours.setStock(stock);
                produitEnCours.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
                produitEnCours.setCategorie(categorie);

                produitService.update(produitEnCours);
                showSuccess("✅ Produit « " + nom + " » mis à jour.");
            }

            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(900));

            pause.setOnFinished(e -> {
                try {
                    Stage stage = (Stage) nomField.getScene().getWindow();
                    NavigationUtil.navigate(stage, "/fxml/admin/produit-list-admin.fxml");
                } catch (IOException ex) {
                    System.err.println("Navigation : " + ex.getMessage());
                }
            });

            pause.play();

        } catch (Exception e) {
            showError("❌ " + (e.getMessage() != null ? e.getMessage() : "Erreur inconnue."));
        }
    }

    @FXML
    private void handleReset() {
        resetStyles();

        if (produitEnCours == null) {
            nomField.clear();
            prixField.clear();
            stockField.clear();
            imageUrlField.clear();
            descriptionArea.clear();
            categorieBox.setValue(null);

            if (imageHintLabel != null) {
                imageHintLabel.setText("Formats : PNG, JPG, JPEG, WEBP");
            }

            appliquerModeAjout();
        } else {
            setProduit(produitEnCours);
        }

        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    private void goToProduits() {
        try {
            NavigationUtil.navigate((Stage) nomField.getScene().getWindow(), "/fxml/admin/produit-list-admin.fxml");
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToCommandes() {
        try {
            NavigationUtil.navigate((Stage) nomField.getScene().getWindow(), "/fxml/admin/commande-list-admin.fxml");
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    private void erreur(TextField f, String msg) {
        f.setStyle(STYLE_ERROR);
        showError("⚠ " + msg);
    }

    private void resetStyles() {
        nomField.setStyle(STYLE_NORMAL);
        prixField.setStyle(STYLE_NORMAL);
        stockField.setStyle(STYLE_NORMAL);
        imageUrlField.setStyle(STYLE_NORMAL);

        if (descriptionArea != null) {
            descriptionArea.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1.5;");
        }

        categorieBox.setStyle(COMBO_NORMAL);
    }

    private void showError(String msg) {
        if (messageLabel == null) return;
        messageLabel.setStyle(
                "-fx-text-fill: #B42318; -fx-font-size: 13px; -fx-font-weight: 700;" +
                        " -fx-background-color: rgba(248,34,57,0.06); -fx-background-radius: 8; -fx-padding: 8 12;"
        );
        messageLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        if (messageLabel == null) return;
        messageLabel.setStyle(
                "-fx-text-fill: #065F46; -fx-font-size: 13px; -fx-font-weight: 700;" +
                        " -fx-background-color: rgba(16,185,129,0.10); -fx-background-radius: 8; -fx-padding: 8 12;"
        );
        messageLabel.setText(msg);
    }
}