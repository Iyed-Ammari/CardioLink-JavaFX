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
     * Upload une image et retourne son URL sécurisée.
     * L'image est redimensionnée à 200x200, crop fill avec gravité sur le visage.
     *
     * @param imageFile Fichier image à uploader
     * @return URL publique de l'image transformée
     * @throws Exception si l'upload échoue
     */
    public String uploadImage(File imageFile) throws Exception {
        Map<?, ?> result = cloudinary.uploader().upload(imageFile,
                ObjectUtils.asMap(
                        "folder", "cardiolink/profiles",
                        "transformation", "w_200,h_200,c_fill,g_face"   // ✅ correction
                ));
        return (String) result.get("secure_url");
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