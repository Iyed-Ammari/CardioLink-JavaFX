package com.cardiolink.Services;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Models.User;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    // --- Singleton ---
    private static ReminderScheduler instance;

    public static synchronized ReminderScheduler getInstance() {
        if (instance == null) {
            instance = new ReminderScheduler();
        }
        return instance;
    }

    private ReminderScheduler() {} // private constructor
    // -----------------

    private static final String SENT_REMINDERS_FILE = "sent_reminders.txt";
    private ScheduledExecutorService scheduler;
    private ServiceRendezvous serviceRendezvous = new ServiceRendezvous();
    private UserService userService = new UserService();
    private EmailService emailService = new EmailService();

    private Set<Integer> sentReminders = new HashSet<>();

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            // Already running — do not start a second instance
            return;
        }
        loadSentReminders();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Exécute la vérification toutes les heures (ou minutes pour le test)
        // Pour les tests, on peut le mettre à chaque minute, mais en production, une fois par heure (1, TimeUnit.HOURS)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[ReminderScheduler] Vérification des rendez-vous de demain...");
                List<Rendezvous> rvs = serviceRendezvous.getAppointmentsForTomorrow();
                for (Rendezvous rv : rvs) {
                    if (!sentReminders.contains(rv.getId())) {
                        sendReminderFor(rv);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur dans le scheduler de rappels : " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void sendReminderFor(Rendezvous rv) {
        try {
            User patient = userService.getUserById(rv.getPatientId());
            User medecin = userService.getUserById(rv.getMedecinId());

            if (patient != null && medecin != null && patient.getEmail() != null) {
                String patientName = patient.getPrenom() + " " + patient.getNom();
                String doctorName = "Dr. " + medecin.getNom();
                
                boolean success = emailService.sendReminder(patient.getEmail(), patientName, doctorName, rv.getDateHeure(), rv.getLienVisio());
                
                if (success) {
                    // Marquer comme envoyé uniquement si l'envoi a réussi
                    sentReminders.add(rv.getId());
                    saveSentReminder(rv.getId());
                } else {
                    System.err.println("Échec de l'envoi pour le RDV " + rv.getId() + ", réessai plus tard.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du rappel pour RDV " + rv.getId() + " : " + e.getMessage());
        }
    }

    private void loadSentReminders() {
        File file = new File(SENT_REMINDERS_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        sentReminders.add(Integer.parseInt(line.trim()));
                    } catch (NumberFormatException ignored) {}
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de " + SENT_REMINDERS_FILE);
            }
        }
    }

    private void saveSentReminder(int id) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SENT_REMINDERS_FILE, true))) {
            bw.write(String.valueOf(id));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du rappel " + id);
        }
    }
}
