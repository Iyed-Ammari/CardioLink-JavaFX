package com.cardiolink.Test;

import com.cardiolink.Models.Suivi;
import com.cardiolink.Models.Intervention;
import com.cardiolink.Services.SuiviService;
import com.cardiolink.Services.InterventionService;

public class Main {
    public static void main(String[] args) {
        SuiviService ss = new SuiviService();
        InterventionService is = new InterventionService();

        try {
            System.out.println("=== TEST SUIVI ===");
            // 1. Ajouter un suivi (patient_id 4)
            Suivi s = new Suivi(0, "Fréquence Cardiaque", 130.0f, "bpm", 4);
            ss.add(s);

            // On récupère le dernier suivi ajouté pour avoir son ID réel
            Suivi suiviEnBase = ss.getAll().get(ss.getAll().size() - 1);
            System.out.println("Suivi créé avec ID: " + suiviEnBase.getId());

            System.out.println("\n=== TEST INTERVENTION ===");
            // 2. Ajouter une intervention liée au suivi
            Intervention inter = new Intervention(0, "Alerte SOS", "Tachycardie détectée", 4); // medecin_id 4 par ex
            inter.setSuiviOrigine(suiviEnBase);
            is.add(inter);

            // 3. Affichage
            System.out.println("\nListe des interventions :");
            is.getAll().forEach(i -> System.out.println("ID: " + i.getId() + " | Type: " + i.getType() + " | Statut: " + i.getStatut()));

            // 4. Test Update Intervention
            if(!is.getAll().isEmpty()) {
                Intervention aModifier = is.getAll().get(0);
                aModifier.setStatut("En cours");
                is.update(aModifier);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
