package com.cardiolink.Services;

import com.cardiolink.Models.DossierMedical;

public class ImcRisqueService {

    // ── Calcul IMC ───────────────────────────────────────────
    public double calculerImc(double poids, double tailleCm) {
        double tailleM = tailleCm / 100.0;
        return poids / (tailleM * tailleM);
    }

    public String categorieImc(double imc) {
        if (imc < 18.5) return "Insuffisance pondérale";
        if (imc < 25.0) return "Poids normal ✅";
        if (imc < 30.0) return "Surpoids ⚠";
        if (imc < 35.0) return "Obésité modérée 🔴";
        return "Obésité sévère 🔴";
    }

    public String imcFormate(double imc) {
        return String.format("%.1f", imc);
    }

    // ── Calcul Risque Cardiaque ──────────────────────────────
    public String risqueCardiaque(DossierMedical d) {
        int score = 0;

        // IMC
        if (d.getPoids() != null && d.getTaille() != null) {
            double imc = calculerImc(d.getPoids(), d.getTaille());
            if (imc >= 30)      score += 2;
            else if (imc >= 25) score += 1;
        }

        // Tension systolique
        if (d.getTensionSystolique() != null) {
            if (d.getTensionSystolique() >= 140)      score += 2;
            else if (d.getTensionSystolique() >= 120) score += 1;
        }

        // Tension diastolique
        if (d.getTensionDiastolique() != null) {
            if (d.getTensionDiastolique() >= 90)     score += 2;
            else if (d.getTensionDiastolique() >= 80) score += 1;
        }

        // Fréquence cardiaque
        if (d.getFrequenceCardiaque() != null) {
            if (d.getFrequenceCardiaque() > 100) score += 1;
            if (d.getFrequenceCardiaque() < 50)  score += 1;
        }

        if (score >= 5) return "🔴 CRITIQUE";
        if (score >= 3) return "🟠 ÉLEVÉ";
        if (score >= 1) return "🟡 MODÉRÉ";
        return "🟢 NORMAL";
    }

    public String couleurRisque(DossierMedical d) {
        String risque = risqueCardiaque(d);
        if (risque.contains("CRITIQUE")) return "#E24B4A";
        if (risque.contains("ÉLEVÉ"))    return "#e67e22";
        if (risque.contains("MODÉRÉ"))   return "#f39c12";
        return "#27ae60";
    }
}