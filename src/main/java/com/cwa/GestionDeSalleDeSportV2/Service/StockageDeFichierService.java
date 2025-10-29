package com.cwa.GestionDeSalleDeSportV2.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StockageDeFichierService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file, String subPath) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Fichier vide");
        }

        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : ".jpg";
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Créer le dossier s'il n'existe pas
        Path directory = Paths.get(uploadDir, subPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Définir le chemin complet du fichier
        Path filePath = directory.resolve(uniqueFileName);

        // Sauvegarder (écrase si déjà existant)
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retourne uniquement le nom du fichier
        return uniqueFileName;
    }
}