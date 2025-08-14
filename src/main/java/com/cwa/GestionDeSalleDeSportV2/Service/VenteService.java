package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.VenteDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.VenteManuelDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutPanier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Repository.PanierRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.ProduitRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.VenteRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenteService {

    private final VenteRepository venteRepository;
    private final PanierRepository panierRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ProduitRepository produitRepository;

    public VenteService(VenteRepository venteRepository, PanierRepository panierRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, NotificationService notificationService, UserRepository userRepository, ProduitRepository produitRepository) {
        this.venteRepository = venteRepository;
        this.panierRepository = panierRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.produitRepository = produitRepository;
    }

    public Vente validerPanierEtCreerVente(Long panierId, VenteDTO dto) throws AccessDeniedException, MessagingException {
        // Vérifier si l'utilisateur est autorisé
        User currentUser = initializeAccess(true);

        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable."));
        User membre = panier.getMembre();


        Gym userGym = currentUser.getGym();
        if (userGym == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        if (!membre.getGyms().contains(userGym)) {
            throw new AccessDeniedException("L'utilisateur courant n'est pas autorisé à acceder à cette gym");
        }

        // Vérifier si le panier est en attente
        if (panier.getStatut() != StatutPanier.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Le panier n'est pas en attente de validation.");
        }

        // Créer la vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setMembre(membre);
        vente.setStaff(currentUser);
        vente.setModeDePaiement(dto.getModeDePaiement()); // Exemple, ajustez selon votre logique

        // Créer une nouvelle liste de lignes pour éviter les références partagées
        List<LigneVente> lignesPourVente = new ArrayList<>();
        for (LigneVente ligne : panier.getLignes()) {
            LigneVente nouvelleLigne = new LigneVente();
            nouvelleLigne.setVente(vente);
            nouvelleLigne.setProduit(ligne.getProduit());
            nouvelleLigne.setPrixTotal(ligne.getPrixTotal());
            lignesPourVente.add(nouvelleLigne);
        }


        // Mettre à jour le statut du panier
        panier.setStatut(StatutPanier.VALIDE);

        venteRepository.save(vente);
        panierRepository.save(panier);

        // Notification au staff
        notificationService.notification(
                currentUser,
                "Panier validé",
                "Le panier " + panierId + " a été validé après paiement.",
                "Vente",
                TypeNotification.VENTE,
                true
        );

        // Notification au membre
        notificationService.notification(
                membre,
                "Panier validé",
                "Votre panier " + panierId + " a été validé. Merci pour votre achat !",
                "Vente",
                TypeNotification.VENTE,
                true
        );

        return vente;
    }

    // A revoir au plus vite

    @Transactional
    public Vente enregistrerVenteManuelle(VenteManuelDTO dto) throws AccessDeniedException, MessagingException {
        // Vérifier si le staff est autorisé
        User currentUser = initializeAccess(true);

        // Créer ou récupérer l'acheteur
        User acheteur;
        if (dto.getAcheteurId() != null) {
            acheteur = userRepository.findById(dto.getAcheteurId())
                    .orElseThrow(() -> new RuntimeException("Membre introuvable."));
        } else {
            // Créer un utilisateur temporaire pour un non-membre
            acheteur = new User();
            acheteur.setNom(dto.getNomAcheteur());
            acheteur.setPrenom(dto.getPrenomAcheteur());
            acheteur.setTelephone(dto.getTelephoneAcheteur());
            acheteur.setRole(Role.MEMBRE_TEMPORAIRE); // Marquer comme invité
            acheteur.setGenre(dto.getGenre());
            Gym staffGym = currentUser.getGym();
            acheteur.setGym(staffGym);
            acheteur = userRepository.save(acheteur);
        }

        // Créer la vente
        Vente vente = new Vente();
        LocalDate dateVente = LocalDate.now();
        vente.setDateVente(dateVente);
        System.out.println("Date de vente : " + dateVente);
        vente.setMembre(acheteur);
        vente.setStaff(currentUser);
        vente.setModeDePaiement(dto.getModeDePaiement());

        // Gérer les lignes de vente
        List<LigneVente> lignes = new ArrayList<>();
        BigDecimal montantTotal = BigDecimal.ZERO;

        for (int i = 0; i < dto.getProduitIds().size(); i++) {
            Long produitId = dto.getProduitIds().get(i);
            Integer quantite = dto.getQuantites().get(i);

            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new RuntimeException("Produit introuvable."));
            LigneVente ligne = new LigneVente();
            ligne.setVente(vente);
            ligne.setProduit(produit);
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(produit.getPrixUnitaire() != null ? produit.getPrixUnitaire() : BigDecimal.ZERO);
            ligne.calculerPrixTotal(); // Assure que prixTotal = prixUnitaire * quantite

            BigDecimal prixTotalLigne = ligne.getPrixTotal() != null ? ligne.getPrixTotal() : BigDecimal.ZERO;
            montantTotal = montantTotal.add(prixTotalLigne);

            System.out.printf("Ligne %d: Produit = %s, Quantité = %d, Prix Unitaire = %.2f, Prix Total = %.2f%n",
                    i, produit.getNom(), quantite, ligne.getPrixUnitaire(), prixTotalLigne);

            lignes.add(ligne);
        }

        vente.setLignes(lignes);
        vente.setMontantTotal(montantTotal);

        Vente savedVente = venteRepository.save(vente);

        // Notification au staff
        notificationService.notification(
                currentUser,
                "Vente enregistrée",
                "Une vente manuelle a été enregistrée pour " + acheteur.getNom() + " " + acheteur.getPrenom(),
                "Vente",
                TypeNotification.VENTE,
                true
        );

        // Notification à l'acheteur uniquement s'il a un email
        if ((acheteur.getRole() == Role.MEMBRE || acheteur.getTelephone() != null) && acheteur.getEmail() != null) {
            try {
                notificationService.notification(
                        acheteur,
                        "Vente enregistrée",
                        "Une vente a été enregistrée pour vous. Montant total : " + savedVente.getMontantTotal(),
                        "Vente",
                        TypeNotification.VENTE,
                        true
                );
            } catch (MessagingException e) {
                System.out.println("Échec de la notification à l'acheteur : " + e.getMessage());
            }
        } else {
            System.out.println("Aucune notification envoyée à l'acheteur (email absent) : " + acheteur.getTelephone());
        }

        return savedVente;
    }

    public List<Vente> listerVentes() throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "lister des ventes dans");
        return venteRepository.findByProductGymId(currentUser.getGym().getId());
    }

    public Vente consulterDetailVente(Long venteId) throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Gym gym = currentUser.getGym();
        verificationAccesGym(currentUser, gym, "les details d'un produit de");
        Vente vente = venteRepository.findById(venteId)
                .orElseThrow(()->new RuntimeException("Vente introuvable"));
        return vente;
    }

    public long getNombreVentesJournalieres() throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes journalières dans");
        LocalDate today = LocalDate.now(); // 16 août 2025
        return venteRepository.countByGymIdAndDate(userGym.getId(), today);
    }

    public BigDecimal getMontantTotalJournalier() throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total journalier dans");
        LocalDate today = LocalDate.now(); // 16 août 2025

        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> v.getDateVente().equals(today))
                .toList();
        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getNombreVentesHebdomadaires() throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes hebdomadaires dans");
        LocalDate today = LocalDate.now(); // 16 août 2025, samedi
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY); // Lundi 11 août 2025
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);  // Dimanche 17 août 2025
        return venteRepository.countByGymIdAndWeek(userGym.getId(), startOfWeek, endOfWeek);
    }

    public BigDecimal getMontantTotalHebdomadaire() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total hebdomadaire dans");
        LocalDate today = LocalDate.now(); // 16 août 2025, samedi
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY); // Lundi 11 août 2025
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);  // Dimanche 17 août 2025
        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> !v.getDateVente().isBefore(startOfWeek) && !v.getDateVente().isAfter(endOfWeek))
                .toList();
        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getNombreVentesMensuelles() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes mensuelles dans");
        LocalDate today = LocalDate.now(); // 16 août 2025
        return venteRepository.countByGymIdAndMonth(userGym.getId(), today);
    }

    public BigDecimal getMontantTotalMensuel() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total mensuel dans");
        LocalDate today = LocalDate.now(); // 16 août 2025
        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> v.getDateVente().getYear() == today.getYear() && v.getDateVente().getMonth() == today.getMonth())
                .toList();
        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getNombreVentesAnnuelles() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes annuelles dans");
        LocalDate today = LocalDate.now(); // 16 août 2025
        return venteRepository.countByGymIdAndYear(userGym.getId(), today);
    }

    public BigDecimal getMontantTotalAnnuel() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total annuel dans");
        LocalDate today = LocalDate.now(); // 16 août 2025
        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> v.getDateVente().getYear() == today.getYear())
                .toList();
        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) { // Vérification du gym uniquement pour staff
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }

    private void verificationAccesGym(User staff,
                                      Gym gym, String action) throws AccessDeniedException {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }
}