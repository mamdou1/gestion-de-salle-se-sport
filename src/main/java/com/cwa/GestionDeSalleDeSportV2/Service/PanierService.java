package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutLigne;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutPanier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PanierService {

    private final PanierRepository panierRepository;
    private final GymRepository gymRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final ProduitRepository produitRepository;
    private final UserRepository userRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final NotificationService notificationService;

    public PanierService(PanierRepository panierRepository, GymRepository gymRepository, LigneVenteRepository ligneVenteRepository, ProduitRepository produitRepository, UserRepository userRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, NotificationService notificationService) {
        this.panierRepository = panierRepository;
        this.gymRepository = gymRepository;
        this.ligneVenteRepository = ligneVenteRepository;
        this.produitRepository = produitRepository;
        this.userRepository = userRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
    }

    private void checkStaffAccess() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;
        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
    }

    private void verificationAccesGym(User staff, Gym gym, String action) {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }

    // 1. Créer un panier
    public Panier creerPanier() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        System.out.println("🛒 Création panier pour user: " + currentUser.getId());

        Gym gym = currentUser.getGym();
        if (gym == null) {
            System.out.println("❌ Aucun gym associé");
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        System.out.println("🏋️‍♂️ Gym: " + gym.getId() + " - " + gym.getNom());

        Panier panier = new Panier();
        panier.setMembre(currentUser);
        Panier savedPanier = panierRepository.save(panier);

        System.out.println("✅ Panier créé avec ID: " + savedPanier.getId());
        return savedPanier;
    }

    // 2. Ajouter un produit à un panier
    public LigneVente ajouterProduit(Long panierId, Long produitId, int quantite) {
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        if (produit.getGym() == null || !produit.getGym().equals(gym)) {
            throw new AccessDeniedException("Le produit ne appartient pas au gym de l'utilisateur.");
        }
        verificationAccesGym(currentUser, gym, "ajouter un produit dans");

        LigneVente ligne = new LigneVente();
        ligne.setPanier(panier);
        ligne.setProduit(produit);
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(produit.getPrixUnitaire());
        ligne.setStatut(StatutLigne.PANIER);
        ligne.calculerPrixTotal();

        return ligneVenteRepository.save(ligne);
    }

    // 3. Modifier la quantité
    public LigneVente modifierPanier(Long ligneId, int quantite) {
        LigneVente ligne = ligneVenteRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne de vente introuvable"));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        if (ligne.getProduit().getGym() == null || !ligne.getProduit().getGym().equals(gym)) {
            throw new AccessDeniedException("La ligne ne concerne pas le gym de l'utilisateur.");
        }
        verificationAccesGym(currentUser, gym, "modifier une ligne dans");
        ligne.setQuantite(quantite);
        ligne.calculerPrixTotal();
        return ligneVenteRepository.save(ligne);
    }

    // 4. Supprimer une ligne de vente
    public void supprimerLigne(Long ligneId) {
        LigneVente ligne = ligneVenteRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne de vente introuvable"));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        if (ligne.getProduit().getGym() == null || !ligne.getProduit().getGym().equals(gym)) {
            throw new AccessDeniedException("La ligne ne concerne pas le gym de l'utilisateur.");
        }
        verificationAccesGym(currentUser, gym, "supprimer une ligne dans");
        ligneVenteRepository.delete(ligne);
    }

    // 5. Supprimer un panier
    public void supprimerPanier(Long panierId) {
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        boolean allFromSameGym = panier.getLignes().stream()
                .allMatch(ligne -> ligne.getProduit().getGym().equals(gym));
        if (!allFromSameGym) {
            throw new AccessDeniedException("Le panier contient des produits de différents gyms.");
        }

        verificationAccesGym(currentUser, gym, "supprimer un panier dans");
        panierRepository.delete(panier);
    }

    // 6. Envoyer le panier - CORRECTION APPLIQUÉE
    // 6. Envoyer le panier - VERSION 100% FONCTIONNELLE
    public Panier envoyerPanier(Long panierId) throws MessagingException {
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable."));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        boolean allFromSameGym = panier.getLignes().stream()
                .allMatch(ligne -> ligne.getProduit().getGym().equals(gym));
        if (!allFromSameGym) {
            throw new AccessDeniedException("Le panier contient des produits de différents gyms.");
        }

        verificationAccesGym(currentUser, gym, "envoyer un panier dans");
        panier.setStatut(StatutPanier.EN_ATTENTE_VALIDATION);
        panier.getLignes().forEach(l -> l.setStatut(StatutLigne.VENTE_VALIDEE));

        // CALCUL + SAUVEGARDE DU MONTANT TOTAL
        BigDecimal montantTotal = calculerMontantTotal(panier);
        panier.setMontantTotal(montantTotal); // LA LIGNE MAGIQUE QUI CHANGE TOUT

        // Notifications (déjà parfaites)
        notificationService.notification(
                currentUser,
                "Panier envoyé",
                "Le panier " + panierId + " a été reçu. Montant total: " + montantTotal + " FCFA",
                "Vente",
                TypeNotification.VENTE,
                true
        );

        notificationService.notificationAdminAGym(
                gym,
                "Demande de validation de panier #" + panierId,
                "Le membre " + currentUser.getNom() + " " + currentUser.getPrenom() +
                        " a envoyé un panier pour validation.\n" +
                        "Panier ID: " + panierId + "\n" +
                        "Montant total: " + montantTotal + " FCFA\n" +
                        "Nombre d'articles: " + panier.getLignes().size(),
                "VALIDATION_PANIER",
                TypeNotification.VALIDATION_PANIER,
                false
        );

        return panierRepository.save(panier);
    }

    // Méthode pour calculer le montant total
    private BigDecimal calculerMontantTotal(Panier panier) {
        return panier.getLignes().stream()
                .map(ligne -> ligne.getPrixUnitaire()
                        .multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 8. VALIDER UN PANIER PAR LE STAFF
    public Panier validerPanier(Long panierId) throws MessagingException {
        checkStaffAccess(); // Seuls ADMIN/RECEPTIONNISTE/GERANT

        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));

        if (panier.getStatut() != StatutPanier.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Ce panier n'est pas en attente de validation");
        }

        // CHANGEMENT DE STATUT + RECALCUL DU MONTANT (au cas où)
        panier.setStatut(StatutPanier.VALIDE);
        BigDecimal montantTotal = calculerMontantTotal(panier);
        panier.setMontantTotal(montantTotal);

        // Toutes les lignes passent en VENTE_VALIDEE (déjà fait à l'envoi, mais on sécurise)
        panier.getLignes().forEach(ligne -> ligne.setStatut(StatutLigne.VENTE_VALIDEE));
        System.out.println("TYPE NOTIF ENVOYÉ AU MEMBRE : '" + TypeNotification.VENTE + "'");
        System.out.println("TYPE NOTIF ENVOYÉ AU STAFF   : '" + TypeNotification.INFO + "'");

        // Notification au membre
        notificationService.notification(
                panier.getMembre(),
                "Panier validé !",
                "Votre panier #" + panierId + " de " + montantTotal + " FCFA a été validé.\n" +
                        "Vous pouvez passer à la réception pour le règlement.",
                "Vente",
                TypeNotification.VENTE,
                true
        );

        // Notification au staff (optionnel)
        notificationService.notificationAdminAGym(
                panier.getMembre().getGym(),
                "Panier #" + panierId + " validé",
                "Le panier de " + panier.getMembre().getNom() + " " + panier.getMembre().getPrenom() +
                        " (" + montantTotal + " FCFA) a été validé.",
                "INFO",
                TypeNotification.INFO,
                false
        );

        return panierRepository.save(panier); // SAUVEGARDE TOUT : statut + montant
    }

    // 9. REJETER UN PANIER PAR LE STAFF
    public Panier rejeterPanier(Long panierId) throws MessagingException {
        checkStaffAccess(); // Seuls ADMIN/RECEPTIONNISTE/GERANT

        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));

        if (panier.getStatut() != StatutPanier.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Ce panier n'est pas en attente de validation");
        }

        // CHANGEMENT DE STATUT
        panier.setStatut(StatutPanier.ANNULE);

        BigDecimal montantTotal = calculerMontantTotal(panier);

        // Notification au membre : REJET CLAIR ET PRO
        notificationService.notification(
                panier.getMembre(),
                "Panier rejeté",
                "Votre panier #" + panierId + " de " + montantTotal + " FCFA a été rejeté.\n\n" +
                        "Raison possible : stock insuffisant, erreur de prix, ou vérification nécessaire.\n" +
                        "Veuillez contacter la réception pour plus d'informations.",
                "Vente",
                TypeNotification.VENTE,
                true
        );

        // Optionnel : notifier le staff qu’il a rejeté
        notificationService.notificationAdminAGym(
                panier.getMembre().getGym(),
                "Panier #" + panierId + " rejeté",
                "Le panier de " + panier.getMembre().getNom() + " " + panier.getMembre().getPrenom() +
                        " (" + montantTotal + " FCFA) a été rejeté.",
                "INFO",
                TypeNotification.INFO,
                false
        );

        return panierRepository.save(panier);
    }





    // 7. Consulter tous les paniers en attente de validation
    public List<Panier> ConsulterPanierEnAttente() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        checkStaffAccess();
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        verificationAccesGym(currentUser, gym, "consulter les paniers en attente dans");
        return panierRepository.findByStatutAndProductGym(StatutPanier.EN_ATTENTE_VALIDATION.name(), gym.getId());
    }
}