package com.cardiolink.Test;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Services.ServiceOrdonnance;
import com.cardiolink.Services.ServiceRendezvous;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServiceRendezvous serviceRV = new ServiceRendezvous();
        ServiceOrdonnance serviceOrd = new ServiceOrdonnance();

        try {
            // ==========================================
            // --- PARTIE 1 : TESTS RENDEZ-VOUS ---
            // ==========================================
            System.out.println("--- Test Ajout Rendez-vous ---");
            Rendezvous rv = new Rendezvous(
                    LocalDateTime.now(),
                    "Confirmé",
                    "Présentiel",
                    null,
                    "Patient doit être à jeun",
                    4, // ID patient valide
                    1  // ID medecin valide
            );
            serviceRV.add(rv);

            // --- Test Mise à jour RDV ---
            System.out.println("\n--- Test Mise à jour Rendez-vous ---");
            // On récupère le dernier ajouté pour le modifier
            List<Rendezvous> allRVs = serviceRV.getAll();
            if (!allRVs.isEmpty()) {
                Rendezvous rvAmodifier = allRVs.get(allRVs.size() - 1);
                rvAmodifier.setStatut("Terminé");
                rvAmodifier.setRemarques("Le patient est venu, tout s'est bien passé.");
                serviceRV.update(rvAmodifier);
                System.out.println("Rendez-vous ID " + rvAmodifier.getId() + " mis à jour !");
            }

            // --- Test Suppression RDV ---
            // Attention : On ne supprime que l'ID 1 s'il existe vraiment
            System.out.println("\n--- Test Suppression Rendez-vous ID 1 ---");
            Rendezvous rvAsupprimer = new Rendezvous();
            rvAsupprimer.setId(1);
            serviceRV.delete(rvAsupprimer);

            // --- AFFICHAGE FINAL RDV ---
            System.out.println("\n--- Liste finale des rendez-vous ---");
            List<Rendezvous> listeRV = serviceRV.getAll();
            for (Rendezvous r : listeRV) {
                System.out.println(r);
            }

            // ==========================================
            // --- PARTIE 2 : TESTS ORDONNANCES ---
            // ==========================================
            // ==========================================
            // --- PARTIE 2 : TESTS ORDONNANCES ---
            // ==========================================
            System.out.println("\n--- Test Ajout Ordonnance ---");

            if (!listeRV.isEmpty()) {
                // On utilise le dernier RDV de la liste pour être sûr qu'il existe
                int rdvIdValide = listeRV.get(listeRV.size() - 1).getId();

                Ordonnance newOrd = new Ordonnance(
                        "ORD-" + System.currentTimeMillis(),
                        LocalDateTime.now(),
                        rdvIdValide,
                        "Hypertension légère",
                        "Prendre un cachet chaque matin",
                        "Dr. House",
                        "Jean Dupont"
                );
                serviceOrd.add(newOrd);

                // --- MISE À JOUR ---
                System.out.println("\n--- Test Mise à jour Ordonnance ---");
                // On récupère la liste pour avoir l'ID généré par la base
                List<Ordonnance> ords = serviceOrd.getAll();
                if (!ords.isEmpty()) {
                    // On prend la toute dernière ordonnance ajoutée
                    Ordonnance ordAModifier = ords.get(ords.size() - 1);
                    ordAModifier.setDiagnostic("Grippe Sévère");
                    ordAModifier.setNotes("Repos strict 1 semaine");

                    serviceOrd.update(ordAModifier);
                    System.out.println("Ordonnance ID " + ordAModifier.getId() + " mise à jour avec succès !");
                }

                // --- SUPPRESSION ---
                System.out.println("\n--- Test Suppression Ordonnance ---");
                List<Ordonnance> ordsApresUpdate = serviceOrd.getAll();
                if (!ordsApresUpdate.isEmpty()) {
                    // On supprime celle qu'on vient de créer/modifier
                    Ordonnance ordASupprimer = ordsApresUpdate.get(ordsApresUpdate.size() - 1);
                    serviceOrd.delete(ordASupprimer);
                    System.out.println("Ordonnance ID " + ordASupprimer.getId() + " supprimée avec succès !");
                }

            } else {
                System.out.println("Erreur : Aucun rendez-vous disponible pour l'ordonnance.");
            }

            // --- AFFICHAGE FINAL ---
            System.out.println("\n--- Liste finale des ordonnances ---");
            List<Ordonnance> listeOrdFinal = serviceOrd.getAll();
            for (Ordonnance o : listeOrdFinal) {
                System.out.println(o);
            }

        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors des tests :");
            e.printStackTrace();
        }
    }
}