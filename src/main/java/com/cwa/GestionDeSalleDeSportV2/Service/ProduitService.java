package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ProduitDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.ProduitRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
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

    public ProduitService(ProduitRepository produitRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, UserRepository userRepository, StockageDeFichierService stockageDeFichierService) {
        this.produitRepository = produitRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.userRepository = userRepository;
        this.stockageDeFichierService = stockageDeFichierService;
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
//        produit.setImageUrl(dto.getImageUrl());
        produit.setCategorie(dto.getCategorie());
        produit.setGym(gym);

        if (file != null && !file.isEmpty()){
            //produit.setPhoto(file.getBytes()); //  Conversion du MultipartFile en byte[]
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String savedFileName = stockageDeFichierService.saveFile(file, fileName);
            produit.setImageUrl(savedFileName); // ✅ juste le nom
        }
        return produitRepository.save(produit);
    }

//    public byte[] getPhotoProduit(Long id){
//        Produit produit = produitRepository.findById(id)
//                .orElseThrow(()->new RuntimeException("Produit non trouvé."));
//        return produit.getPhoto();
//    }

    public Produit modifierProduit(Long id, ProduitDTO dto, MultipartFile file) throws IOException {
        User currentUser = initializeAccess(true);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, produit.getGym(), "modifier un produit dans");
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrixUnitaire(dto.getPrixUnitaire());
        produit.setQuantiteEnStock(dto.getQuantiteEnStock());
//        produit.setImageUrl(dto.getImageUrl());
        produit.setCategorie(dto.getCategorie());

        if (file != null && !file.isEmpty()){
            // produit.setPhoto(file.getBytes()); //  Conversion du MultipartFile en byte[]

            // Supprimer ancienne image si elle existe
            if (produit.getImageUrl() != null){
                Path oldPath = Paths.get(produit.getImageUrl());
                Files.deleteIfExists(oldPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String savedFileName = stockageDeFichierService.saveFile(file, fileName);
            produit.setImageUrl(savedFileName); // ✅ juste le nom
        }

        return produitRepository.save(produit);
    }

    public void supprimerProduit(Long id) throws IOException {
        User currentUser = initializeAccess(true);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, produit.getGym(), "supprimer un produit dans");

        // Supprime le fichier associé s’il existe
        if (produit.getImageUrl() != null){
            Path oldPath = Paths.get(produit.getImageUrl());
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
                .orElseThrow(()->new RuntimeException("Produit introuvable"));
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

