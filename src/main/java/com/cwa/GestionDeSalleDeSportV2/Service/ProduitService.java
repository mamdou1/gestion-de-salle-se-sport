package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ProduitDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.ProduitRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final UserRepository userRepository;
    private final StockageDeFichierService stockageDeFichierService;
    private final Path fileStorageLocation;

    public ProduitService(ProduitRepository produitRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter,
                          UserRepository userRepository, StockageDeFichierService stockageDeFichierService,
                          @Value("${file.upload-dir:uploads/}") String uploadDir) {
        this.produitRepository = produitRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.userRepository = userRepository;
        this.stockageDeFichierService = stockageDeFichierService;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create file storage directory: " + this.fileStorageLocation, e);
        }
    }

    public Produit ajouterProduit(ProduitDTO dto, MultipartFile file) throws IOException {
        User currentUser = initializeAccess(true);
        Gym gym = currentUser.getGym();

        verificationAccesGym(currentUser, gym, "ajouter un produit dans");

        Produit produit = new Produit();
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrixUnitaire(dto.getPrixUnitaire());
        produit.setQuantiteEnStock(dto.getQuantiteEnStock());
        produit.setCategorie(dto.getCategorie());
        produit.setGym(gym);

        // Save product first to get ID
        Produit savedProduit = produitRepository.save(produit);

        if (file != null && !file.isEmpty()) {
            String fileName = stockageDeFichierService.store(file, "produits/" + savedProduit.getId());
            savedProduit.setImageUrl("/uploads/produits/" + savedProduit.getId() + "/" + fileName);
            produitRepository.save(savedProduit); // Update produit with imageUrl
        }

        return savedProduit;
    }

    public byte[] getPhotoProduit(Long id) throws IOException {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé."));
        if (produit.getImageUrl() == null) {
            throw new RuntimeException("No photo found for produit ID: " + id);
        }
        Path filePath = fileStorageLocation.resolve(produit.getImageUrl().replace("/uploads/", "")).normalize();
        return Files.readAllBytes(filePath);
    }

    public Produit modifierProduit(Long id, ProduitDTO dto, MultipartFile file) throws IOException {
        User currentUser = initializeAccess(true);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, produit.getGym(), "modifier un produit dans");
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrixUnitaire(dto.getPrixUnitaire());
        produit.setQuantiteEnStock(dto.getQuantiteEnStock());
        produit.setCategorie(dto.getCategorie());

        if (file != null && !file.isEmpty()) {
            // Delete old image if it exists
            if (produit.getImageUrl() != null) {
                // Line 97: Replaced uploadDir with fileStorageLocation
                Path oldImagePath = fileStorageLocation.resolve(produit.getImageUrl().replace("/uploads/", "")).normalize();
                Files.deleteIfExists(oldImagePath);
            }
            String fileName = stockageDeFichierService.store(file, "produits/" + id);
            produit.setImageUrl("/uploads/produits/" + id + "/" + fileName);
        }

        return produitRepository.save(produit);
    }

    public void supprimerProduit(Long id) throws IOException {
        User currentUser = initializeAccess(true);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, produit.getGym(), "supprimer un produit dans");

        // Supprime le fichier associé s’il existe
        if (produit.getImageUrl() != null) {
            Path oldPath = fileStorageLocation.resolve(produit.getImageUrl().replace("/uploads/", "")).normalize();
            Files.deleteIfExists(oldPath);
        }

        produitRepository.delete(produit);
    }

    public List<Produit> listerProduits() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        Gym gym = currentUser.getGym();
        // Ajoutez cette vérification
        if (gym == null) {
            throw new RuntimeException("Aucun gym associé à l'utilisateur courant");
        }
        verificationAccesGym(currentUser, gym, "consulter la liste les produits dans");
        return produitRepository.findByGym(gym);
    }

    public Produit consulterDetailProd(Long produitId) throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        Gym gym = currentUser.getGym();
        verificationAccesGym(currentUser, gym, "consulterla details des produits dans");
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        return produit;
    }

    public User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        if (currentUser == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        if (requireStaff) {
            boolean isStaff = currentUser.getRole() == Role.ADMIN ||
                    currentUser.getRole() == Role.RECEPTIONNISTE ||
                    currentUser.getRole() == Role.GERANT;

            if (!isStaff) {
                throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
            }

            // Vérification du gym uniquement pour staff
            if (currentUser.getGym() == null) {
                throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
            }
        }

        return currentUser;
    }

    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        // Vérifiez d'abord que le staff et le gym ne sont pas null
        if (staff == null || gym == null) {
            throw new AccessDeniedException("Staff ou gym non défini");
        }

        // Vérifiez l'accès
        if (!userRepository.existsById(staff.getId()) ||
                (staff.getGyms() != null && !staff.getGyms().contains(gym))) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }
}
