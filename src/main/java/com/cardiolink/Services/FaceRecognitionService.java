package com.cardiolink.Services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;

public class FaceRecognitionService {

    private CascadeClassifier faceDetector;

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public FaceRecognitionService() {
        loadFaceDetector();
    }

    // ── Charger le détecteur haar cascade ────────────────────
    private void loadFaceDetector() {
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream(
                            "haarcascade_frontalface_default.xml");
            if (is == null)
                throw new Exception("Fichier cascade introuvable !");

            Path tmp = Files.createTempFile("cascade", ".xml");
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            is.close();

            faceDetector = new CascadeClassifier(tmp.toString());
            if (faceDetector.empty())
                System.err.println("Cascade vide !");
            else
                System.out.println("✅ Cascade chargée avec succès");

        } catch (Exception e) {
            System.err.println("Erreur cascade : " + e.getMessage());
        }
    }

    // ── Base64 → Mat OpenCV ──────────────────────────────────
    private Mat base64ToMat(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        MatOfByte mob = new MatOfByte(bytes);
        Mat img = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        if (img.empty()) throw new Exception("Image invalide ou corrompue !");
        return img;
    }

    // ── Extraire région du visage ────────────────────────────
    private Mat extractFace(Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);

        if (faceDetector != null && !faceDetector.empty()) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(
                    gray, faces,
                    1.1, 4, 0,
                    new Size(50, 50),
                    new Size(400, 400));

            Rect[] arr = faces.toArray();
            if (arr.length > 0) {
                // Prendre le plus grand visage
                Rect biggest = arr[0];
                for (Rect r : arr) {
                    if (r.area() > biggest.area()) biggest = r;
                }
                System.out.println("✅ Visage détecté : " + biggest);
                return new Mat(gray, biggest);
            } else {
                System.out.println("⚠ Aucun visage détecté, utilisation image complète");
            }
        }
        return gray;
    }

    // ── Redimensionner et convertir en vecteur float ─────────
    private float[] toVector(Mat face) {
        Mat resized = new Mat();
        Imgproc.resize(face, resized, new Size(64, 64));

        Mat norm = new Mat();
        Core.normalize(resized, norm, 0, 1,
                Core.NORM_MINMAX, CvType.CV_32F);

        float[] data = new float[(int)(norm.total())];
        norm.get(0, 0, data);
        return data;
    }

    // ── Similarité cosinus ────────────────────────────────────
    private double cosine(float[] v1, float[] v2) {
        int len = Math.min(v1.length, v2.length);
        double dot = 0, n1 = 0, n2 = 0;
        for (int i = 0; i < len; i++) {
            dot += v1[i] * v2[i];
            n1  += v1[i] * v1[i];
            n2  += v2[i] * v2[i];
        }
        if (n1 == 0 || n2 == 0) return 0;
        return dot / (Math.sqrt(n1) * Math.sqrt(n2));
    }

    // ── Comparer deux images Base64 ───────────────────────────
    public double compareFaces(String b64_1, String b64_2) throws Exception {
        Mat img1 = base64ToMat(b64_1);
        Mat img2 = base64ToMat(b64_2);

        Mat face1 = extractFace(img1);
        Mat face2 = extractFace(img2);

        float[] v1 = toVector(face1);
        float[] v2 = toVector(face2);

        double score = cosine(v1, v2);
        System.out.println("Score similarité : " + score);
        return score;
    }

    // ── Vérifier si même personne ────────────────────────────
    public boolean verifyFace(String captured, String stored) throws Exception {
        double score = compareFaces(captured, stored);
        // Seuil 0.82 — ajustez si besoin
        return score >= 0.82;
    }
}