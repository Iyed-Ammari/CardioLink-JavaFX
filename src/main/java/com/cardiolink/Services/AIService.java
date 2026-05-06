package com.cardiolink.Services;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class AIService {

    // On reprend tes chemins exacts de la partie Web
    private final String pythonBinary = "C:\\Users\\Mon Pc\\CardioLink\\ml_env\\Scripts\\python.exe";
    private final String pythonScript = "C:\\Users\\Mon Pc\\CardioLink\\ml\\summarizer.py";

    public String getSummary(String content) {
        try {
            // Configuration du processus (comme proc_open)
            ProcessBuilder pb = new ProcessBuilder(pythonBinary, pythonScript);
            Process process = pb.start();

            // 1. ENVOYER le texte au script Python (Stdin)
            try (OutputStream os = process.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(content);
                writer.flush();
            }

            // 2. LIRE la réponse du script Python (Stdout)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            // Attendre la fin du processus
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return output.toString().trim();
            } else {
                return "Erreur lors de l'exécution du script (Code: " + exitCode + ")";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de connexion avec le modèle IA.";
        }
    }
}