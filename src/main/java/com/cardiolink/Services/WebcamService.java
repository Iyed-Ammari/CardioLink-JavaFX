package com.cardiolink.Services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class WebcamService {

    static {
        // Charger OpenCV
        nu.pattern.OpenCV.loadLocally();
    }

    private VideoCapture camera;

    // ── Ouvrir la webcam ─────────────────────────────────────
    public boolean openCamera() {
        camera = new VideoCapture(0);
        return camera.isOpened();
    }

    // ── Capturer une frame et la sauvegarder ─────────────────
    public String captureAndSave(String outputPath) throws Exception {
        if (camera == null || !camera.isOpened())
            throw new Exception("Webcam non disponible !");

        Mat frame = new Mat();
        boolean captured = false;

        // Essayer plusieurs fois pour éviter frame noire
        for (int i = 0; i < 5; i++) {
            camera.read(frame);
            if (!frame.empty()) { captured = true; break; }
            Thread.sleep(100);
        }

        if (!captured || frame.empty())
            throw new Exception("Impossible de capturer la photo !");

        // Sauvegarder en fichier temporaire
        Imgcodecs.imwrite(outputPath, frame);
        return outputPath;
    }

    // ── Capturer et retourner en Base64 ──────────────────────
    public String captureAsBase64() throws Exception {
        String tmpPath = System.getProperty("java.io.tmpdir")
                + File.separator + "face_capture.jpg";
        captureAndSave(tmpPath);
        byte[] bytes = Files.readAllBytes(new File(tmpPath).toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ── Obtenir une frame pour affichage JavaFX ──────────────
    public Mat getCurrentFrame() {
        if (camera == null || !camera.isOpened()) return null;
        Mat frame = new Mat();
        camera.read(frame);
        return frame.empty() ? null : frame;
    }

    // ── Convertir Mat OpenCV en Image JavaFX ─────────────────
    public javafx.scene.image.Image matToFxImage(Mat mat) {
        Mat rgb = new Mat();
        Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);

        int width  = rgb.cols();
        int height = rgb.rows();
        int channels = rgb.channels();
        byte[] data = new byte[width * height * channels];
        rgb.get(0, 0, data);

        javafx.scene.image.WritableImage fxImage =
                new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter pw = fxImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = (y * width + x) * channels;
                int r = data[idx]     & 0xFF;
                int g = data[idx + 1] & 0xFF;
                int b = data[idx + 2] & 0xFF;
                pw.setColor(x, y, javafx.scene.paint.Color.rgb(r, g, b));
            }
        }
        return fxImage;
    }

    // ── Fermer la webcam ─────────────────────────────────────
    public void closeCamera() {
        if (camera != null && camera.isOpened())
            camera.release();
    }
}
