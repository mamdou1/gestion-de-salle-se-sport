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

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final UserRepository userRepository;

    public ProduitService(ProduitRepository produitRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, UserRepository userRepository) {
        this.produitRepository = produitRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.userRepository = userRepository;
    }

    public Produit ajouterProduit(ProduitDTO dto) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym gym = currentUser.getGym();
        verificationAccesGym(currentUser, gym, "ajouter un produit dans");
        Produit produit = new Produit();
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrixUnitaire(dto.getPrixUnitaire());
        produit.setQuantiteEnStock(dto.getQuantiteEnStock());
        produit.setImageUrl(dto.getImageUrl());
        produit.setCategorie(dto.getCategorie());
        produit.setGym(gym);
        return produitRepository.save(produit);
    }

    public Produit modifierProduit(Long id, ProduitDTO dto) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, produit.getGym(), "modifier un produit dans");
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrixUnitaire(dto.getPrixUnitaire());
        produit.setQuantiteEnStock(dto.getQuantiteEnStock());
        produit.setImageUrl(dto.getImageUrl());
        produit.setCategorie(dto.getCategorie());
        return produitRepository.save(produit);
    }

    public void supprimerProduit(Long id) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, produit.getGym(), "supprimer un produit dans");
        produitRepository.delete(produit);
    }

    public List<Produit> listerProduits() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        Gym gym = currentUser.getGym();
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
        if (requireStaff && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) { // Vérification du gym uniquement pour staff
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }

    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }
}