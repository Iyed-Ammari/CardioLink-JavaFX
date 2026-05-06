package com.cardiolink.Services;

import com.cardiolink.Models.Intervention;

import java.util.ArrayList;
import java.util.List;

public class MaterielSuggestionService {

    public List<String> suggererMateriel(Intervention intervention) {
        List<String> materiels = new ArrayList<>();

        String type = intervention.getType() != null ? intervention.getType().toLowerCase() : "";
        String description = intervention.getDescription() != null ? intervention.getDescription().toLowerCase() : "";
        String statut = intervention.getStatut() != null ? intervention.getStatut().toLowerCase() : "";

        if (type.contains("urgence") || description.contains("urgence") || description.contains("cardiaque")) {
            materiels.add("Défibrillateur");
            materiels.add("ECG portable");
            materiels.add("Tensiomètre");
            materiels.add("Oxymètre");
            materiels.add("Trousse de premiers secours");
        }

        if (description.contains("spo2") || description.contains("oxygène") || description.contains("respir")) {
            ajouterSiAbsent(materiels, "Masque à oxygène");
            ajouterSiAbsent(materiels, "Bouteille d’oxygène");
            ajouterSiAbsent(materiels, "Oxymètre");
        }

        if (type.contains("consultation") || description.contains("température") || description.contains("fièvre")) {
            ajouterSiAbsent(materiels, "Thermomètre");
            ajouterSiAbsent(materiels, "Stéthoscope");
            ajouterSiAbsent(materiels, "Tensiomètre");
        }

        if (description.contains("fréquence") || description.contains("cardiaque") || description.contains("tachycardie")) {
            ajouterSiAbsent(materiels, "ECG portable");
            ajouterSiAbsent(materiels, "Moniteur cardiaque");
        }

        if (statut.contains("effectuée")) {
            ajouterSiAbsent(materiels, "Compte-rendu médical");
        }

        if (materiels.isEmpty()) {
            materiels.add("Stéthoscope");
            materiels.add("Tensiomètre");
            materiels.add("Gants médicaux");
            materiels.add("Trousse de consultation");
        }

        return materiels;
    }

    private void ajouterSiAbsent(List<String> liste, String materiel) {
        if (!liste.contains(materiel)) {
            liste.add(materiel);
        }
    }
}