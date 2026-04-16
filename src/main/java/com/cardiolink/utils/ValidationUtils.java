package com.cardiolink.utils;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Models.Rendezvous;
import java.time.LocalDateTime;

public class ValidationUtils {

    // --- CONTROLE POUR RENDEZ-VOUS ---
    public static void validerRendezvous(Rendezvous rv) throws Exception {
        if (rv.getDateHeure() == null) {
            throw new Exception("La date du rendez-vous est obligatoire.");
        }
        if (rv.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new Exception("La date du rendez-vous ne peut pas être dans le passé.");
        }
        if (rv.getStatut() == null || rv.getStatut().isEmpty()) {
            throw new Exception("Le statut est obligatoire.");
        }
        if (rv.getPatientId() <= 0 || rv.getMedecinId() <= 0) {
            throw new Exception("ID Patient et ID Médecin doivent être valides.");
        }
    }

    // --- CONTROLE POUR ORDONNANCE ---
    public static void validerOrdonnance(Ordonnance ord) throws Exception {
        if (ord.getReference() == null || ord.getReference().trim().isEmpty()) {
            throw new Exception("La référence de l'ordonnance est obligatoire.");
        }
        if (ord.getDiagnostic() == null || ord.getDiagnostic().length() < 3) {
            throw new Exception("Le diagnostic est trop court ou vide.");
        }
        if (ord.getPatientNom() == null || ord.getPatientNom().isEmpty()) {
            throw new Exception("Le nom du patient est obligatoire.");
        }
    }
}