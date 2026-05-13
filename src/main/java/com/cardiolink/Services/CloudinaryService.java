package com.cardiolink.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.util.Map;

public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "diizmcf0f",
                "api_key",    "327697358775321",
                "api_secret", "K2HdlGUGMYIfLJRrV7g1sZm8Mso"
        ));
    }

    /**
     * Upload une image de manière générique et retourne son URL sécurisée.
     *
     * @param imageFile Fichier image à uploader
     * @param folder Le dossier cible sur Cloudinary (ex: "cardiolink/forum")
     * @param transformation La transformation à appliquer (ex: "w_500,c_fit", ou null)
     * @return URL publique de l'image
     * @throws Exception si l'upload échoue
     */
    public String uploadImage(File imageFile, String folder, String transformation) throws Exception {
        Map<String, Object> params = new java.util.HashMap<>();
        if (folder != null && !folder.isEmpty()) {
            params.put("folder", folder);
        }
        if (transformation != null && !transformation.isEmpty()) {
            params.put("transformation", transformation);
        }

        Map<?, ?> result = cloudinary.uploader().upload(imageFile, params);
        return (String) result.get("secure_url");
    }

    /**
     * Upload une image et retourne son URL sécurisée.
     * L'image est redimensionnée à 200x200, crop fill avec gravité sur le visage.
     * (Gardée pour compatibilité avec le profil patient)
     *
     * @param imageFile Fichier image à uploader
     * @return URL publique de l'image transformée
     * @throws Exception si l'upload échoue
     */
    public String uploadImage(File imageFile) throws Exception {
        return uploadImage(imageFile, "cardiolink/profiles", "w_200,h_200,c_fill,g_face");
    }

    /**
     * Supprime une image à partir de son public_id (généralement le chemin complet sans extension).
     * Exemple : "cardiolink/profiles/nom_image"
     *
     * @param publicId Identifiant public de l'image sur Cloudinary
     * @throws Exception si la suppression échoue
     */
    public void deleteImage(String publicId) throws Exception {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}