package com.cwa.GestionDeSalleDeSportV2.Service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StockageDeFichierService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String fileName) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Fichier vide");
        };

        //  Créer le dossier s'il n'existe pas
        Path Directory = Paths.get(uploadDir);
        if (!Files.exists(Directory)) {
            Files.createDirectories(Directory);
        };

        //  Définir le chemin complete du fichier
        Path filePath = Directory.resolve(fileName);

        //  Sauvegader (écrse si déjà existant)
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // ✅ Retourne uniquement le nom du fichier
        return fileName;
    }
}

