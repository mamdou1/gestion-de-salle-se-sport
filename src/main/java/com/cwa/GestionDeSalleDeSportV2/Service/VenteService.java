package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.VenteDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.VenteManuelDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Repository.PanierRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.ProduitRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.VenteRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.ListePaimentRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VenteService {

    private static final Logger logger = LoggerFactory.getLogger(VenteService.class);

    private final VenteRepository venteRepository;
    private final PanierRepository panierRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ProduitRepository produitRepository;
    private final GestionStockService gestionStockService;
    private final ListePaimentRepository listePaimentRepository;

    public VenteService(VenteRepository venteRepository,
                        PanierRepository panierRepository,
                        UtilisateurActuellementConnecter utilisateurActuellementConnecter,
                        NotificationService notificationService,
                        UserRepository userRepository,
                        ProduitRepository produitRepository,
                        GestionStockService gestionStockService,
                        ListePaimentRepository listePaimentRepository) {
        this.venteRepository = venteRepository;
        this.panierRepository = panierRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.produitRepository = produitRepository;
        this.gestionStockService = gestionStockService;
        this.listePaimentRepository = listePaimentRepository;
    }
    @Transactional
    public Vente validerPanierEtCreerVente(Long panierId, VenteDTO dto) throws AccessDeniedException, MessagingException {
        User currentUser = initializeAccess(true);
        Gym gym = currentUser.getGym();
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable."));
        User membre = panier.getMembre();

        // Créer la vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setMembre(membre);
        vente.setStaff(currentUser);
        vente.setModeDePaiement(dto.getModeDePaiement());
        vente.setGym_id(gym.getId());

        // Créer une nouvelle liste de lignes pour éviter les références partagées
        List<LigneVente> lignesPourVente = new ArrayList<>();
        for (LigneVente ligne : panier.getLignes()) {
            // Déduire le stock une seule fois ici
            if (ligne.getProduit() != null && ligne.getQuantite() != null) {
                logger.debug("Déduisant {} unités du stock pour le produit ID: {}", ligne.getQuantite(), ligne.getProduit().getId());
                gestionStockService.deduireStock(ligne.getProduit().getId(), ligne.getQuantite());
            } else {
                logger.error("Produit ou quantité null pour une ligne dans le panier ID: {}", panierId);
                throw new RuntimeException("Données invalides dans le panier.");
            }

            LigneVente nouvelleLigne = new LigneVente();
            nouvelleLigne.setVente(vente);
            nouvelleLigne.setProduit(ligne.getProduit());
            nouvelleLigne.setQuantite(ligne.getQuantite());
            nouvelleLigne.setPrixUnitaire(ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire() : BigDecimal.ZERO);
            nouvelleLigne.calculerPrixTotal(); // Calcul du prix total seulement
            lignesPourVente.add(nouvelleLigne);
        }

        vente.setLignes(lignesPourVente);

        // Calculer le montant total de la vente
        BigDecimal montantTotal = lignesPourVente.stream()
                .map(LigneVente::getPrixTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vente.setMontantTotal(montantTotal);

        // Sauvegarde de la vente
        Vente savedVente = venteRepository.save(vente);

        // Création de l'entrée dans liste_paiment
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.VENTE);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(montantTotal);
        paiement.setModeDePaiement(dto.getModeDePaiement());
        paiement.setReferenceId(savedVente.getId());
        paiement.setAcheteur(membre);
        paiement.setStaffEnregistreur(currentUser);
        paiement.setGym(gym);
        paiement.setDetails("Vente: Panier ID " + panierId);
        listePaimentRepository.save(paiement);
        logger.info("Created ListePaiment for vente: {}", paiement);

        panier.setStatut(StatutPanier.VALIDE);
        panierRepository.save(panier);

        // Notification au staff
        notificationService.notifyGymAndMember(
                gym,
                membre,
                "Demande de validation d'inscription",
                "Vous avez reçu une demande une nouvelle demande d'inscription au près de votre salle de sport",
                "Validation",
                TypeNotification.VALIDATION_INSCRIPTION,
                false
        );

        // Notification au membre
        notificationService.notification(
                membre,
                "Panier validé",
                "Votre panier " + panierId + " a été validé. Montant total: " + montantTotal + " FCFA. Merci pour votre achat !",
                "Vente",
                TypeNotification.VENTE,
                true
        );

        return savedVente;
    }

    /**
     * Enregistre une vente manuelle (hors panier)
     * @param dto DTO contenant les informations de la vente manuelle
     * @return La vente créée
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     * @throws MessagingException En cas d'erreur d'envoi de notification
     */
    @Transactional
    public Vente enregistrerVenteManuelle(VenteManuelDTO dto) throws AccessDeniedException, MessagingException {
        // Vérifier si le staff est autorisé
        User currentUser = initializeAccess(true);

        // Créer ou récupérer l'acheteur (optionnel)
        User acheteur = gererAcheteur(dto, currentUser);

        // Créer la vente
        Vente vente = creerVente(currentUser, acheteur, dto.getModeDePaiement());

        // Gérer les lignes de vente et le stock
        List<LigneVente> lignes = new ArrayList<>();
        BigDecimal montantTotal = BigDecimal.ZERO;

        for (int i = 0; i < dto.getProduitIds().size(); i++) {
            Long produitId = dto.getProduitIds().get(i);
            Integer quantite = dto.getQuantites().get(i);

            // Déduire le stock AVANT de créer la ligne de vente
            gestionStockService.deduireStock(produitId, quantite);

            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new RuntimeException("Produit introuvable."));

            LigneVente ligne = creerLigneVente(vente, produit, quantite);
            lignes.add(ligne);

            montantTotal = montantTotal.add(ligne.getPrixTotal());
        }

        vente.setLignes(lignes);
        vente.setMontantTotal(montantTotal);

        // Sauvegarde de la vente
        Vente savedVente = venteRepository.save(vente);

        // Création de l'entrée dans liste_paiment
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.VENTE);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(montantTotal);
        paiement.setModeDePaiement(dto.getModeDePaiement());
        paiement.setReferenceId(savedVente.getId());
        paiement.setAcheteur(acheteur);
        paiement.setStaffEnregistreur(currentUser);
        paiement.setGym(currentUser.getGym());
        paiement.setDetails("Vente manuelle: " + (acheteur != null ? acheteur.getNom() + " " + acheteur.getPrenom() : "Client inconnu"));
        listePaimentRepository.save(paiement);
        logger.info("Created ListePaiment for vente manuelle: {}", paiement);

        // Envoyer les notifications
        envoyerNotificationsVente(currentUser, acheteur, savedVente);

        return savedVente;
    }

    /**
     * Gère la création ou récupération de l'acheteur (optionnel)
     * @param dto DTO contenant les informations de l'acheteur
     * @param currentUser Utilisateur courant (staff)
     * @return L'acheteur (membre existant, nouveau membre temporaire, ou null si non spécifié)
     */
    private User gererAcheteur(VenteManuelDTO dto, User currentUser) {
        if (dto.getAcheteurId() != null) {
            // Acheteur membre existant
            User acheteur = userRepository.findById(dto.getAcheteurId())
                    .orElseThrow(() -> new RuntimeException("Membre introuvable."));

            // Vérifier si l'acheteur appartient au gym du staff
            if (!currentUser.getGyms().contains(acheteur.getGym())) {
                throw new RuntimeException("L'acheteur ne fait pas partie de votre gym.");
            }
            return acheteur;
        } else if (dto.getPrenomAcheteur() != null && dto.getNomAcheteur() != null) {
            // Créer un utilisateur temporaire pour un non-membre
            User acheteur = new User();
            acheteur.setNom(dto.getNomAcheteur());
            acheteur.setPrenom(dto.getPrenomAcheteur());
            acheteur.setTelephone(dto.getTelephoneAcheteur());
            acheteur.setRole(Role.MEMBRE_TEMPORAIRE);
            acheteur.setGenre(dto.getGenre());
            Gym staffGym = currentUser.getGym();
            acheteur.setGym(staffGym);
            return userRepository.save(acheteur);
        }
        // Si aucune information n'est fournie, retourner null
        return null;
    }

    /**
     * Crée une nouvelle vente
     * @param staff Staff qui enregistre la vente
     * @param acheteur Acheteur (membre, peut être null)
     * @param modePaiement Mode de paiement
     * @return La vente créée
     */
    private Vente creerVente(User staff, User acheteur, ModeDePaiement modePaiement) {
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setMembre(acheteur); // Peut être null
        vente.setStaff(staff);
        vente.setGym_id(staff.getGym().getId());
        vente.setModeDePaiement(modePaiement);
        return vente;
    }

    /**
     * Crée une ligne de vente
     * @param vente Vente parente
     * @param produit Produit vendu
     * @param quantite Quantité vendue
     * @return La ligne de vente créée
     */
    private LigneVente creerLigneVente(Vente vente, Produit produit, Integer quantite) {
        LigneVente ligne = new LigneVente();
        ligne.setVente(vente);
        ligne.setProduit(produit);
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(produit.getPrixUnitaire() != null ? produit.getPrixUnitaire() : BigDecimal.ZERO);
        ligne.calculerPrixTotal(); // Calcul du prix total seulement
        return ligne;
    }

    /**
     * Envoie les notifications après une vente
     * @param staff Staff qui a enregistré la vente
     * @param acheteur Acheteur (peut être null)
     * @param vente Vente enregistrée
     */
    private void envoyerNotificationsVente(User staff, User acheteur, Vente vente) {
        try {
            // Notification au staff
            String acheteurInfo = (acheteur != null) ? acheteur.getNom() + " " + acheteur.getPrenom() : "un client inconnu";
            notificationService.notification(
                    staff,
                    "Vente enregistrée",
                    "Une vente manuelle a été enregistrée pour " + acheteurInfo +
                            ". Montant total: " + vente.getMontantTotal() + " FCFA",
                    "Vente",
                    TypeNotification.VENTE,
                    true
            );

            // Notification à l'acheteur uniquement s'il existe et a un email
            if (acheteur != null && acheteur.getEmail() != null) {
                notificationService.notification(
                        acheteur,
                        "Vente enregistrée",
                        "Une vente a été enregistrée pour vous. Montant total: " + vente.getMontantTotal() + " FCFA",
                        "Vente",
                        TypeNotification.VENTE,
                        true
                );
            }
        } catch (MessagingException e) {
            System.out.println("Échec de l'envoi des notifications: " + e.getMessage());
        }
    }

    /**
     * Liste toutes les ventes du gym de l'utilisateur courant
     * @return Liste des ventes
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public List<Vente> listerVentes() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "lister des ventes dans");
        return venteRepository.findByProductGymId(userGym.getId());
    }

    /**
     * Consulte les détails d'une vente spécifique
     * @param venteId ID de la vente
     * @return La vente avec ses détails
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public Vente consulterDetailVente(Long venteId) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym gym = currentUser.getGym();
        verificationAccesGym(currentUser, gym, "consulter les détails d'une vente dans");

        Vente vente = venteRepository.findById(venteId)
                .orElseThrow(() -> new RuntimeException("Vente introuvable"));

        // Vérifier que la vente appartient au gym de l'utilisateur
        if (!vente.getStaff().getGym().equals(gym)) {
            throw new AccessDeniedException("Accès refusé: cette vente ne fait pas partie de votre gym");
        }

        return vente;
    }

    // ==================== MÉTHODES DE STATISTIQUES ====================

    /**
     * Récupère le nombre de ventes journalières
     * @return Nombre de ventes du jour
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreVentesJournalieres() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes journalières dans");

        LocalDate today = LocalDate.now();
        return venteRepository.countByGymIdAndDate(userGym.getId(), today);
    }

    /**
     * Récupère le montant total des ventes journalières
     * @return Montant total des ventes du jour
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalJournalier() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total journalier dans");

        LocalDate today = LocalDate.now();
        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> v.getDateVente().equals(today))
                .toList();

        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère le nombre de ventes hebdomadaires
     * @return Nombre de ventes de la semaine
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreVentesHebdomadaires() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes hebdomadaires dans");

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        return venteRepository.countByGymIdAndWeek(userGym.getId(), startOfWeek, endOfWeek);
    }

    /**
     * Récupère le montant total des ventes hebdomadaires
     * @return Montant total des ventes de la semaine
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalHebdomadaire() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total hebdomadaire dans");

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> !v.getDateVente().isBefore(startOfWeek) && !v.getDateVente().isAfter(endOfWeek))
                .toList();

        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère le nombre de ventes mensuelles
     * @return Nombre de ventes du mois
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreVentesMensuelles() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes mensuelles dans");

        LocalDate today = LocalDate.now();
        return venteRepository.countByGymIdAndMonth(userGym.getId(), today);
    }

    /**
     * Récupère le montant total des ventes mensuelles
     * @return Montant total des ventes du mois
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalMensuel() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total mensuel dans");

        LocalDate today = LocalDate.now();
        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> v.getDateVente().getYear() == today.getYear() && v.getDateVente().getMonth() == today.getMonth())
                .toList();

        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère le nombre de ventes annuelles
     * @return Nombre de ventes de l'année
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreVentesAnnuelles() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les ventes annuelles dans");

        LocalDate today = LocalDate.now();
        return venteRepository.countByGymIdAndYear(userGym.getId(), today);
    }

    /**
     * Récupère le montant total des ventes annuelles
     * @return Montant total des ventes de l'année
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalAnnuel() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total annuel dans");

        LocalDate today = LocalDate.now();
        List<Vente> ventes = venteRepository.findByProductGymId(userGym.getId()).stream()
                .filter(v -> v.getDateVente().getYear() == today.getYear())
                .toList();

        return ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== MÉTHODES D'ACCÈS ET VÉRIFICATION ====================

    /**
     * Initialise et vérifie l'accès de l'utilisateur
     * @param requireStaff Si true, vérifie que l'utilisateur est un staff
     * @return L'utilisateur courant
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        if (requireStaff && currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }

        if (currentUser.getGym() == null && requireStaff) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        return currentUser;
    }

    /**
     * Vérifie que l'utilisateur a accès au gym spécifié
     * @param staff Utilisateur staff
     * @param gym Gym à vérifier
     * @param action Action en cours (pour le message d'erreur)
     * @throws AccessDeniedException Si l'utilisateur n'a pas accès
     */
    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }
}