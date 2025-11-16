package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.ListePaimentRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FamilleAbonnementService {

    private static final Logger logger = LoggerFactory.getLogger(FamilleAbonnementService.class);

    private final AbonnementRepository abonnementRepository;
    private final FamilleRepository familleRepository;
    private final FactureCollectiveService factureCollectiveService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final GymRepository gymRepository;
    private final ListePaimentRepository listePaimentRepository;

    public FamilleAbonnementService(
            AbonnementRepository abonnementRepository,
            FamilleRepository familleRepository,
            FactureCollectiveService factureCollectiveService,
            EmailService emailService,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            UserService userService,
            UtilisateurActuellementConnecter utilisateurActuellementConnecter,
            GymRepository gymRepository,
            ListePaimentRepository listePaimentRepository) {
        this.abonnementRepository = abonnementRepository;
        this.familleRepository = familleRepository;
        this.factureCollectiveService = factureCollectiveService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userService = userService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.gymRepository = gymRepository;
        this.listePaimentRepository = listePaimentRepository;
    }

    // === MÉTHODES UTILITAIRES POUR CORRIGER LES ERREURS ===

    /**
     * Méthode utilitaire pour remplacer findByFamilleAndStatut manquant
     */
    private List<Abonnement> findByFamilleAndStatut(Famille famille, StatutAbonnement statut) {
        return abonnementRepository.findByFamilleId(famille.getId()).stream()
                .filter(abonnement -> abonnement.getStatut() == statut)
                .collect(Collectors.toList());
    }

    /**
     * Méthode utilitaire pour trouver le premier abonnement actif d'une famille
     */
    private Abonnement findFirstByFamilleAndStatutActif(Famille famille) {
        return abonnementRepository.findByFamilleId(famille.getId()).stream()
                .filter(abonnement -> abonnement.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElse(null);
    }

    // === NOUVELLES MÉTHODES DTO POUR ÉVITER STACKOVERFLOW ===

    /**
     * Récupère tous les abonnements familiaux en DTO (sans StackOverflow)
     */
    public List<AbonnementDTO> getAllAbonnementsFamiliauxDTO() {
        try {
            logger.info("🔍 Récupération de tous les abonnements familiaux via DTO");

            List<AbonnementDTO> dtos = abonnementRepository.findAllAbonnementsFamiliauxDTO();

            List<AbonnementDTO> abonnementsEnrichis = dtos.stream()
                    .map(this::enrichirAvecDetailsFamiliaux)
                    .collect(Collectors.toList());

            logger.info("✅ {} abonnements familiaux récupérés via DTO", abonnementsEnrichis.size());
            return abonnementsEnrichis;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements familiaux via DTO", e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements familiaux");
        }
    }

    /**
     * Récupère les abonnements familiaux actifs en DTO
     */
    public List<AbonnementDTO> getAbonnementsFamiliauxActifsDTO() {
        try {
            logger.info("🔍 Récupération abonnements familiaux actifs via DTO");

            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsFamiliauxActifsDTO();

            List<AbonnementDTO> abonnementsEnrichis = dtos.stream()
                    .map(this::enrichirAvecDetailsFamiliaux)
                    .collect(Collectors.toList());

            logger.info("✅ {} abonnements familiaux actifs récupérés via DTO", abonnementsEnrichis.size());
            return abonnementsEnrichis;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements familiaux actifs via DTO", e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements familiaux actifs");
        }
    }

    /**
     * Récupère les abonnements familiaux par gym en DTO
     */
    public List<AbonnementDTO> getAbonnementsFamiliauxDTOByGymId(Long gymId) {
        try {
            logger.info("🔍 Récupération abonnements familiaux par gym {} via DTO", gymId);

            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsFamiliauxDTOByGymId(gymId);

            List<AbonnementDTO> abonnementsEnrichis = dtos.stream()
                    .map(this::enrichirAvecDetailsFamiliaux)
                    .collect(Collectors.toList());

            logger.info("✅ {} abonnements familiaux récupérés pour le gym {} via DTO", abonnementsEnrichis.size(), gymId);
            return abonnementsEnrichis;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements familiaux par gym via DTO", e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements familiaux par gym");
        }
    }

    /**
     * Enrichit les DTO familiaux avec les détails manquants
     */
    private AbonnementDTO enrichirAvecDetailsFamiliaux(AbonnementDTO dto) {
        try {
            // Récupérer l'entité complète pour les détails manquants
            Abonnement abonnement = abonnementRepository.findById(dto.getId()).orElse(null);

            if (abonnement != null) {
                // Informations du membre
                if (abonnement.getMembre() != null) {
                    dto.setMembreId(abonnement.getMembre().getId());
                    dto.setNomMembre(abonnement.getMembre().getNom() + " " + abonnement.getMembre().getPrenom());
                }

                // Informations de la famille
                if (abonnement.getFamille() != null) {
                    dto.setFamilleId(abonnement.getFamille().getId());
                    dto.setNomFamille(abonnement.getFamille().getNom());
                }

                // Informations du gym
                if (abonnement.getGym() != null) {
                    dto.setGymId(abonnement.getGym().getId());
                    dto.setNomGym(abonnement.getGym().getNom());
                }

                // Informations du type de service
                if (abonnement.getTypeDeService() != null) {
                    dto.setNomTypeDeService(abonnement.getTypeDeService().getNom());
                }

                // Dates supplémentaires
                dto.setDateMiseEnPause(abonnement.getDatePauseAbonnement());
                dto.setDateResiliation(abonnement.getDateResiliation());
            }
        } catch (Exception e) {
            logger.warn("⚠️ Impossible d'enrichir les détails pour l'abonnement familial {}: {}", dto.getId(), e.getMessage());
        }

        return dto;
    }

    // === MÉTHODES EXISTANTES CORRIGÉES ===

    private List<com.cwa.GestionDeSalleDeSportV2.Entity.Gym> getUserGyms(User user) {
        List<com.cwa.GestionDeSalleDeSportV2.Entity.Gym> result = new ArrayList<>();

        if (user.getGyms() != null && !user.getGyms().isEmpty()) {
            result.addAll(user.getGyms());
        }
        else if (user.getGym() != null) {
            result.add(user.getGym());
        }

        return result;
    }

    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }

    // === MÉTHODES POUR LA GESTION DES STATUTS ===

    private void mettreAJourStatutAbonnement(Abonnement abonnement) {
        LocalDate aujourdHui = LocalDate.now();
        StatutAbonnement ancienStatut = abonnement.getStatut();

        if (ancienStatut == StatutAbonnement.EN_PAUSE ||
                ancienStatut == StatutAbonnement.RESILIE) {
            return;
        }

        StatutAbonnement nouveauStatut = ancienStatut;

        if (aujourdHui.isAfter(abonnement.getDateFinAbonnement())) {
            nouveauStatut = StatutAbonnement.EXPIRE;
        }
        else if (aujourdHui.plusDays(7).isAfter(abonnement.getDateFinAbonnement())) {
            nouveauStatut = StatutAbonnement.BIENTOT_EXPIRE;
        }
        else {
            nouveauStatut = StatutAbonnement.EN_COURS;
        }

        if (nouveauStatut != ancienStatut) {
            abonnement.setStatut(nouveauStatut);
            abonnementRepository.save(abonnement);
            logger.info("Abonnement {} : {} → {}", abonnement.getId(), ancienStatut, nouveauStatut);
        }
    }

    @Transactional
    public void mettreAJourStatutsFamille(Long familleId) {
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        List<Abonnement> abonnements = abonnementRepository.findByFamilleId(familleId);
        int compteurMisesAJour = 0;

        for (Abonnement abonnement : abonnements) {
            StatutAbonnement ancienStatut = abonnement.getStatut();
            mettreAJourStatutAbonnement(abonnement);
            if (abonnement.getStatut() != ancienStatut) {
                compteurMisesAJour++;
            }
        }

        logger.info("Statuts mis à jour pour {}/{} abonnements de la famille {}",
                compteurMisesAJour, abonnements.size(), familleId);
    }

    // === MÉTHODES POUR L'ENREGISTREMENT DANS LISTE_PAIMENT ===

    /**
     * 🔥 NOUVELLE MÉTHODE : Créer un enregistrement de paiement pour l'abonnement familial complet
     */
    private void creerEnregistrementListePaimentFamilial(Abonnement abonnement, Famille famille, User staffEnregistreur, String typeOperation) {
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.ABONNEMENT_FAMILIAL);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(abonnement.getPrixAbonnement());
        paiement.setModeDePaiement(abonnement.getModeDePaiement());
        paiement.setReferenceId(abonnement.getId());
        paiement.setAcheteur(famille.getChefFamille()); // Utiliser le chef de famille comme acheteur
        paiement.setStaffEnregistreur(staffEnregistreur);
        paiement.setGym(abonnement.getGym());
        paiement.setDetails(String.format("Abonnement familial %s - Famille: %s (%d membres)",
                typeOperation, famille.getNom(), famille.getMembres().size()));

        listePaimentRepository.save(paiement);
        logger.info("Enregistrement ListePaiment créé pour l'abonnement familial {}: {}", typeOperation, paiement);
    }

    private void creerEnregistrementRenouvellementListePaiment(Abonnement abonnement, User membre, User staffEnregistreur, int moisAjoutes) {
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.ABONNEMENT_FAMILIAL);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(abonnement.getPrixAbonnement());
        paiement.setModeDePaiement(abonnement.getModeDePaiement());
        paiement.setReferenceId(abonnement.getId());
        paiement.setAcheteur(membre);
        paiement.setStaffEnregistreur(staffEnregistreur);
        paiement.setGym(abonnement.getGym());
        paiement.setDetails(String.format("Renouvellement abonnement familial - %s: %s %s (+%s mois)",
                abonnement.getFamille().getNom(),
                membre.getNom(), membre.getPrenom(),
                moisAjoutes));

        listePaimentRepository.save(paiement);
        logger.info("Enregistrement ListePaiment créé pour le renouvellement familial: {}", paiement);
    }

    // 1. Crée un abonnement familial pour une famille - CORRIGÉ : UN SEUL ABONNEMENT
    @Transactional
    public void creerAbonnementFamilial(FamilleAbonnementDTO dto) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new RuntimeException("Seul les membres du staff peuvent créer un abonnement familial");
        }

        Famille famille = familleRepository.findById(dto.getFamilleId())
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(famille.getId());

        if (!getUserGyms(currentUser).contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        List<User> membres = famille.getMembres();
        if (membres == null || membres.isEmpty()) {
            throw new RuntimeException("La famille ne contient aucun membre");
        }

        // 🔥 CORRECTION : Calculer le prix TOTAL pour toute la famille
        BigDecimal prixTotal = BigDecimal.ZERO;
        for (User membre : membres) {
            BigDecimal base = (membre.getGenre() == Genre.FEMME) ? dto.getTarifFemme() : dto.getTarifHomme();
            BigDecimal montantMembre = base.subtract(dto.getReductionParPersonne());

            if (montantMembre.compareTo(BigDecimal.ZERO) < 0) {
                montantMembre = BigDecimal.ZERO;
            }
            prixTotal = prixTotal.add(montantMembre);
        }

        // 🔥 CORRECTION : Créer UN SEUL abonnement pour toute la famille
        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(null); // 🔥 IMPORTANT : Pas de membre spécifique
        abonnement.setGym(famille.getGym());
        abonnement.setTypes(TypeAbonnements.FAMILIALE);
        abonnement.setPrixAbonnement(prixTotal);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());

        LocalDate dateFin = LocalDate.now().plusMonths(dto.getNombreMois().longValue());
        abonnement.setDateFinAbonnement(dateFin);
        abonnement.setDateRappelFinAbonnement(dateFin.minusDays(5));
        abonnement.setNombreDeMois(dto.getNombreMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setStatut(StatutAbonnement.EN_COURS);

        Abonnement savedAbonnement = abonnementRepository.save(abonnement);
        mettreAJourStatutAbonnement(savedAbonnement);

        // 🔥 CORRECTION : Créer un seul enregistrement de paiement pour toute la famille
        creerEnregistrementListePaimentFamilial(savedAbonnement, famille, currentUser, "création");

        // Marquer les frais d'inscription comme payés pour tous les membres
        for (User membre : membres) {
            membre.setFraisInscriptionPayer(true);
            userRepository.save(membre);
        }

        // 🔥 CORRECTION : Passer une liste avec UN seul abonnement à la facture
        List<Abonnement> abonnements = List.of(savedAbonnement);
        factureCollectiveService.creeFactureCollective(famille, abonnements, prixTotal);
    }

    // === NOUVELLES MÉTHODES POUR LE RENOUVELLEMENT AVEC LISTE_PAIMENT ===


    @Transactional
    public void renouvelerAbonnementFamilial(Long familleId, FamilleAbonnementDTO dto) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new RuntimeException("Seul les membres du staff peuvent renouveler un abonnement familial");
        }

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(familleId);

        if (!getUserGyms(currentUser).contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        List<User> membres = famille.getMembres();
        if (membres == null || membres.isEmpty()) {
            throw new RuntimeException("La famille ne contient aucun membre");
        }

        // Calculer le prix TOTAL pour toute la famille
        BigDecimal prixTotal = BigDecimal.ZERO;
        for (User membre : membres) {
            BigDecimal base = (membre.getGenre() == Genre.FEMME) ? dto.getTarifFemme() : dto.getTarifHomme();
            BigDecimal montantMembre = base.subtract(dto.getReductionParPersonne());

            if (montantMembre.compareTo(BigDecimal.ZERO) < 0) {
                montantMembre = BigDecimal.ZERO;
            }
            prixTotal = prixTotal.add(montantMembre);
        }

        // Utiliser le chef de famille comme référence
        User chefFamille = famille.getChefFamille();
        if (chefFamille == null) {
            chefFamille = membres.get(0);
        }

        // Créer UN SEUL abonnement pour toute la famille
        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(chefFamille);
        abonnement.setGym(famille.getGym());
        abonnement.setTypes(TypeAbonnements.FAMILIALE);
        abonnement.setPrixAbonnement(prixTotal);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());

        LocalDate dateFin = LocalDate.now().plusMonths(dto.getNombreMois().longValue());
        abonnement.setDateFinAbonnement(dateFin);
        abonnement.setDateRappelFinAbonnement(dateFin.minusDays(5));
        abonnement.setNombreDeMois(dto.getNombreMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setStatut(StatutAbonnement.EN_COURS); // ✅ STATUT DE L'ABONNEMENT

        Abonnement savedAbonnement = abonnementRepository.save(abonnement);
        mettreAJourStatutAbonnement(savedAbonnement);
        creerEnregistrementListePaimentFamilial(savedAbonnement, famille, currentUser, "renouvellement");

        List<Abonnement> abonnementsRenouvelles = List.of(savedAbonnement);
        factureCollectiveService.creeFactureCollective(famille, abonnementsRenouvelles, prixTotal);

        logger.info("🔄 Abonnement familial renouvelé avec succès pour la famille {} - {} membres",
                famille.getNom(), membres.size());
    }

    // === MÉTHODES MANQUANTES POUR COMPLÉTER LE SERVICE ===

    @Transactional
    public void resilierAbonnement(Long familleId) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new AccessDeniedException("Seul les membres du staff peuvent résilier un abonnement familial");
        }

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(familleId);

        if (!getUserGyms(currentUser).contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        // CORRECTION : Utiliser la méthode utilitaire pour remplacer findByFamilleAndStatut
        List<Abonnement> abonnementsActifs = findByFamilleAndStatut(famille, StatutAbonnement.EN_COURS);

        if (abonnementsActifs.isEmpty()) {
            throw new RuntimeException("Aucun abonnement actif trouvé pour cette famille");
        }

        for (Abonnement abonnement : abonnementsActifs) {
            abonnement.setStatut(StatutAbonnement.RESILIE);
            abonnement.setDateResiliation(LocalDate.now());
            abonnementRepository.save(abonnement);
            logger.info("Abonnement {} résilié", abonnement.getId());
        }

        logger.info("Abonnement familial résilié avec succès pour la famille {}", famille.getNom());
    }

    @Transactional
    public void mettreAJourAbonnement(Long familleId, FamilleAbonnementDTO dto) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new AccessDeniedException("Seul les membres du staff peuvent mettre à jour un abonnement familial");
        }

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(familleId);

        if (!getUserGyms(currentUser).contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        // CORRECTION : Utiliser la méthode utilitaire pour remplacer findByFamilleAndStatut
        List<Abonnement> abonnementsActifs = findByFamilleAndStatut(famille, StatutAbonnement.EN_COURS);

        if (abonnementsActifs.isEmpty()) {
            throw new RuntimeException("Aucun abonnement actif trouvé pour cette famille");
        }

        for (Abonnement abonnement : abonnementsActifs) {
            // 🔥 CORRECTION : Recalculer le prix total pour la famille
            BigDecimal prixTotal = BigDecimal.ZERO;
            for (User membre : famille.getMembres()) {
                BigDecimal base = (membre.getGenre() == Genre.FEMME) ? dto.getTarifFemme() : dto.getTarifHomme();
                BigDecimal montantMembre = base.subtract(dto.getReductionParPersonne());

                if (montantMembre.compareTo(BigDecimal.ZERO) < 0) {
                    montantMembre = BigDecimal.ZERO;
                }
                prixTotal = prixTotal.add(montantMembre);
            }

            abonnement.setPrixAbonnement(prixTotal);
            abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());
            abonnement.setModeDePaiement(dto.getModeDePaiement());
            abonnement.setEnregistrerPar(currentUser);

            abonnementRepository.save(abonnement);
            mettreAJourStatutAbonnement(abonnement);
        }

        logger.info("Abonnement familial mis à jour avec succès pour la famille {}", famille.getNom());
    }

    /**
     * Obtient le statut de l'abonnement d'une famille - CORRIGÉ : Retourne DTO
     */
    public AbonnementDTO getStatutAbonnement(Long familleId) {
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(familleId);

        // CORRECTION : Utiliser la méthode utilitaire
        Abonnement abonnementActif = findFirstByFamilleAndStatutActif(famille);

        if (abonnementActif == null) {
            throw new RuntimeException("Aucun abonnement actif trouvé pour cette famille");
        }

        return convertirEnAbonnementDTO(abonnementActif);
    }

    /**
     * Obtient l'historique des abonnements d'une famille - CORRIGÉ : Retourne DTOs
     */
    public List<AbonnementDTO> getHistoriqueAbonnements(Long familleId) {
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(familleId);

        List<Abonnement> abonnements = abonnementRepository.findByFamilleOrderByDateDebutAbonnementDesc(famille);

        return abonnements.stream()
                .map(this::convertirEnAbonnementDTO)
                .collect(Collectors.toList());
    }

    public boolean estAbonnementActif(Long familleId) {
        try {
            Famille famille = familleRepository.findById(familleId)
                    .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

            mettreAJourStatutsFamille(familleId);

            // CORRECTION : Utiliser la méthode utilitaire
            List<Abonnement> abonnementsActifs = findByFamilleAndStatut(famille, StatutAbonnement.EN_COURS);
            return !abonnementsActifs.isEmpty();
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'abonnement actif: {}", e.getMessage());
            return false;
        }
    }

    public String getDateExpiration(Long familleId) {
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(familleId);

        // CORRECTION : Utiliser la méthode utilitaire
        Abonnement abonnementActif = findFirstByFamilleAndStatutActif(famille);

        if (abonnementActif == null) {
            throw new RuntimeException("Aucun abonnement actif trouvé pour cette famille");
        }

        return abonnementActif.getDateFinAbonnement().toString();
    }

    public long getNombreAbonnementsActifs() {
        List<Famille> familles = familleRepository.findAll();
        for (Famille famille : familles) {
            mettreAJourStatutsFamille(famille.getId());
        }

        return abonnementRepository.countByTypesAndStatut(TypeAbonnements.FAMILIALE, StatutAbonnement.EN_COURS);
    }

    /**
     * Obtient les abonnements expirant bientôt - CORRIGÉ : Retourne DTOs
     */
    public List<AbonnementDTO> getAbonnementsExpirantBientot() {
        LocalDate aujourdHui = LocalDate.now();
        LocalDate dans7Jours = aujourdHui.plusDays(7);

        List<Famille> familles = familleRepository.findAll();
        for (Famille famille : familles) {
            mettreAJourStatutsFamille(famille.getId());
        }

        // CORRECTION : Utiliser une méthode alternative
        List<Abonnement> abonnementsFamiliaux = abonnementRepository.findByTypes(TypeAbonnements.FAMILIALE);

        List<Abonnement> abonnementsExpirant = abonnementsFamiliaux.stream()
                .filter(abonnement -> abonnement.getStatut() == StatutAbonnement.EN_COURS)
                .filter(abonnement -> {
                    LocalDate dateFin = abonnement.getDateFinAbonnement();
                    return !dateFin.isBefore(aujourdHui) && !dateFin.isAfter(dans7Jours);
                })
                .collect(Collectors.toList());

        return abonnementsExpirant.stream()
                .map(this::convertirEnAbonnementDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void mettreEnPause(Long abonnementId) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new AccessDeniedException("Seul les membres du staff peuvent mettre un abonnement en pause");
        }

        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));

        if (!getUserGyms(currentUser).contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EN_COURS) {
            throw new RuntimeException("Seul un abonnement en cours peut être mis en pause");
        }

        abonnement.setStatut(StatutAbonnement.EN_PAUSE);
        abonnement.setDatePauseAbonnement(LocalDate.now());
        abonnementRepository.save(abonnement);

        logger.info("Abonnement {} mis en pause", abonnementId);
    }

    @Transactional
    public void reprendreAbonnement(Long abonnementId) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new AccessDeniedException("Seul les membres du staff peuvent reprendre un abonnement");
        }

        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));

        if (!getUserGyms(currentUser).contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EN_PAUSE) {
            throw new RuntimeException("Seul un abonnement en pause peut être repris");
        }

        long joursPause = ChronoUnit.DAYS.between(abonnement.getDatePauseAbonnement(), LocalDate.now());

        LocalDate nouvelleDateFin = abonnement.getDateFinAbonnement().plusDays(joursPause);
        abonnement.setDateFinAbonnement(nouvelleDateFin);
        abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setDatePauseAbonnement(null);

        abonnementRepository.save(abonnement);
        mettreAJourStatutAbonnement(abonnement);

        logger.info("Abonnement {} repris après {} jours de pause", abonnementId, joursPause);
    }

    @Transactional
    public void mettreAJourStatutAutomatiquement(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));

        mettreAJourStatutAbonnement(abonnement);
    }

    /**
     * Convertit un Abonnement en AbonnementDTO (pour compatibilité)
     */
    private AbonnementDTO convertirEnAbonnementDTO(Abonnement abonnement) {
        AbonnementDTO dto = new AbonnementDTO();
        dto.setId(abonnement.getId());
        dto.setPrixAbonnement(abonnement.getPrixAbonnement());
        dto.setDateDebutAbonnement(abonnement.getDateDebutAbonnement());
        dto.setDateFinAbonnement(abonnement.getDateFinAbonnement());
        dto.setDateRappelFinAbonnement(abonnement.getDateRappelFinAbonnement());
        dto.setPeriodAbonnement(abonnement.getPeriodAbonnement());
        dto.setNombreDeMois(abonnement.getNombreDeMois());
        dto.setModeDePaiement(abonnement.getModeDePaiement());
        dto.setStatut(abonnement.getStatut());
        dto.setTypes(abonnement.getTypes());

        if (abonnement.getMembre() != null) {
            dto.setMembreId(abonnement.getMembre().getId());
            dto.setNomMembre(abonnement.getMembre().getNom() + " " + abonnement.getMembre().getPrenom());
        }

        if (abonnement.getFamille() != null) {
            dto.setFamilleId(abonnement.getFamille().getId());
            dto.setNomFamille(abonnement.getFamille().getNom());
        }

        if (abonnement.getGym() != null) {
            dto.setGymId(abonnement.getGym().getId());
            dto.setNomGym(abonnement.getGym().getNom());
        }

        return dto;
    }

    /**
     * Obtient tous les abonnements familiaux - CORRIGÉ : Utilise maintenant les DTOs
     */
    public List<AbonnementDTO> getAllAbonnementsFamiliaux() {
        // Utilise la nouvelle méthode DTO
        return getAllAbonnementsFamiliauxDTO();
    }

    /**
     * Obtient tous les abonnements familiaux d'un gym spécifique - CORRIGÉ : Utilise maintenant les DTOs
     */
    public List<AbonnementDTO> getAbonnementsFamiliauxParGym(Long gymId) {
        // Utilise la nouvelle méthode DTO
        return getAbonnementsFamiliauxDTOByGymId(gymId);
    }

    @Transactional
    public void verifierEtNotifierAbonnementsExpires() throws MessagingException {
        LocalDate aujourdHui = LocalDate.now();
        LocalDate dateRappel = aujourdHui.plusDays(5);

        List<Famille> familles = familleRepository.findAll();
        for (Famille famille : familles) {
            mettreAJourStatutsFamille(famille.getId());
        }

        // CORRECTION : Utiliser une méthode alternative
        List<Abonnement> abonnementsFamiliaux = abonnementRepository.findByTypes(TypeAbonnements.FAMILIALE);

        List<Abonnement> abonnementsExpirant = abonnementsFamiliaux.stream()
                .filter(abonnement -> abonnement.getStatut() == StatutAbonnement.EN_COURS)
                .filter(abonnement -> abonnement.getDateRappelFinAbonnement() != null &&
                        abonnement.getDateRappelFinAbonnement().equals(dateRappel))
                .collect(Collectors.toList());

        for (Abonnement abonnement : abonnementsExpirant) {
            // Notifier le chef de famille
            User chefFamille = abonnement.getFamille().getChefFamille();

            notificationService.notification(
                    chefFamille,
                    "Rappel d'expiration d'abonnement familial",
                    "Votre abonnement familial pour " + abonnement.getFamille().getNom() + " expire le " + abonnement.getDateFinAbonnement(),
                    "Expiration abonnement familial",
                    TypeNotification.ABONNEMENT,
                    true
            );

            emailService.envoyerEmail(
                    chefFamille.getEmail(),
                    "Rappel d'expiration d'abonnement familial",
                    "Bonjour " + chefFamille.getPrenom() + ",\n\n" +
                            "Votre abonnement familial pour " + abonnement.getFamille().getNom() + " expire le " + abonnement.getDateFinAbonnement() + ".\n" +
                            "Pensez à le renouveler pour continuer à profiter de nos services.\n\n" +
                            "Cordialement,\nL'équipe de la salle de sport"
            );

            logger.info("Notification d'expiration envoyée à {}", chefFamille.getEmail());
        }

        List<Abonnement> abonnementsExpires = abonnementsFamiliaux.stream()
                .filter(abonnement -> abonnement.getStatut() == StatutAbonnement.EN_COURS)
                .filter(abonnement -> abonnement.getDateFinAbonnement().isBefore(aujourdHui))
                .collect(Collectors.toList());

        for (Abonnement abonnement : abonnementsExpires) {
            abonnement.setStatut(StatutAbonnement.EXPIRE);
            abonnementRepository.save(abonnement);
            logger.info("Abonnement {} marqué comme expiré", abonnement.getId());
        }
    }

    public Map<String, Object> getStatistiquesAbonnementsFamiliaux() {
        List<Famille> familles = familleRepository.findAll();
        for (Famille famille : familles) {
            mettreAJourStatutsFamille(famille.getId());
        }

        Map<String, Object> statistiques = new HashMap<>();

        long totalAbonnements = abonnementRepository.countByTypes(TypeAbonnements.FAMILIALE);
        statistiques.put("totalAbonnements", totalAbonnements);

        long abonnementsActifs = abonnementRepository.countByTypesAndStatut(TypeAbonnements.FAMILIALE, StatutAbonnement.EN_COURS);
        statistiques.put("abonnementsActifs", abonnementsActifs);

        long abonnementsEnPause = abonnementRepository.countByTypesAndStatut(TypeAbonnements.FAMILIALE, StatutAbonnement.EN_PAUSE);
        statistiques.put("abonnementsEnPause", abonnementsEnPause);

        long abonnementsExpires = abonnementRepository.countByTypesAndStatut(TypeAbonnements.FAMILIALE, StatutAbonnement.EXPIRE);
        statistiques.put("abonnementsExpires", abonnementsExpires);

        long abonnementsResilies = abonnementRepository.countByTypesAndStatut(TypeAbonnements.FAMILIALE, StatutAbonnement.RESILIE);
        statistiques.put("abonnementsResilies", abonnementsResilies);

        BigDecimal chiffreAffairesTotal = abonnementRepository.getChiffreAffairesTotalFamilial();
        statistiques.put("chiffreAffairesTotal", chiffreAffairesTotal != null ? chiffreAffairesTotal : BigDecimal.ZERO);

        return statistiques;
    }

    @Transactional
    public void supprimerAbonnement(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));

        if (abonnement.getStatut() == StatutAbonnement.EN_COURS) {
            throw new RuntimeException("Impossible de supprimer un abonnement actif");
        }

        abonnementRepository.delete(abonnement);
        logger.info("Abonnement {} supprimé", abonnementId);
    }

    // === MÉTHODES UTILITAIRES POUR LE CALCUL DES PRIX ===

    public BigDecimal calculerPrixFamilialTotal(Famille famille, BigDecimal tarifHomme, BigDecimal tarifFemme, BigDecimal reductionParPersonne, BigDecimal nombreMois) {
        List<User> membres = famille.getMembres();
        if (membres == null || membres.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (User membre : membres) {
            BigDecimal base = (membre.getGenre() == Genre.FEMME) ? tarifFemme : tarifHomme;
            BigDecimal montantMembre = base.subtract(reductionParPersonne);

            if (montantMembre.compareTo(BigDecimal.ZERO) < 0) {
                montantMembre = BigDecimal.ZERO;
            }

            montantMembre = montantMembre.multiply(nombreMois);

            BigDecimal fraisInscription = membre.getFraisInscription() != null ? membre.getFraisInscription() : BigDecimal.ZERO;
            total = total.add(montantMembre.add(fraisInscription));
        }

        return total;
    }



    // Dans votre classe FamilleAbonnementService - REMPLACER la méthode problématique

    @Transactional
    public List<Abonnement> creerAbonnementFamilialEtRetourner(FamilleAbonnementDTO dto) throws MessagingException, AccessDeniedException {
        logger.info("🎯 === DÉBUT creerAbonnementFamilialEtRetourner ===");

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new RuntimeException("Seul les membres du staff peuvent créer un abonnement familial");
        }

        com.cwa.GestionDeSalleDeSportV2.Entity.Gym gymUtilisateur = currentUser.getGym();
        if (gymUtilisateur == null) {
            throw new RuntimeException("Aucun gym associé à l'utilisateur connecté");
        }

        Famille famille = familleRepository.findById(dto.getFamilleId())
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        mettreAJourStatutsFamille(famille.getId());

        if (!famille.getGym().getId().equals(gymUtilisateur.getId())) {
            throw new AccessDeniedException("Accès refusé : cette famille n'appartient pas à votre gym.");
        }

        List<User> membres = famille.getMembres();
        if (membres == null || membres.isEmpty()) {
            throw new RuntimeException("La famille ne contient aucun membre");
        }

        // Utiliser le chef de famille comme référence
        User chefFamille = famille.getChefFamille();
        if (chefFamille == null) {
            chefFamille = membres.get(0);
            logger.warn("⚠️ Aucun chef de famille défini, utilisation du premier membre: {} {}",
                    chefFamille.getNom(), chefFamille.getPrenom());
        }

        // Vérifier qu'aucun abonnement familial n'existe déjà
        List<Abonnement> abonnementsFamiliauxExistants = abonnementRepository.findByFamilleAndTypes(famille, TypeAbonnements.FAMILIALE);
        if (!abonnementsFamiliauxExistants.isEmpty()) {
            throw new RuntimeException("Un abonnement familial existe déjà pour cette famille");
        }

        // 🔥 CORRECTION : Mettre en pause tous les abonnements individuels actifs des membres AVANT création
        for (User membre : membres) {
            List<Abonnement> abonnementsIndividuelsActifs = abonnementRepository.findByMembreAndTypesAndStatut(
                    membre, TypeAbonnements.INDIVIDUEL, StatutAbonnement.EN_COURS);

            if (!abonnementsIndividuelsActifs.isEmpty()) {
                logger.info("⏸️ Mise en pause des abonnements individuels pour {} {}", membre.getNom(), membre.getPrenom());
                for (Abonnement abonnementIndividuel : abonnementsIndividuelsActifs) {
                    abonnementIndividuel.setStatut(StatutAbonnement.EN_PAUSE);
                    abonnementIndividuel.setDatePauseAbonnement(LocalDate.now());
                    abonnementRepository.save(abonnementIndividuel);
                }
            }
        }

        // Calcul du prix TOTAL pour toute la famille
        BigDecimal prixTotal = BigDecimal.ZERO;
        for (User membre : membres) {
            BigDecimal base = (membre.getGenre() == Genre.FEMME) ? dto.getTarifFemme() : dto.getTarifHomme();
            BigDecimal montantMembre = base.subtract(dto.getReductionParPersonne());
            if (montantMembre.compareTo(BigDecimal.ZERO) < 0) {
                montantMembre = BigDecimal.ZERO;
            }
            prixTotal = prixTotal.add(montantMembre);
        }

        // 🔥 CORRECTION : Utiliser le chef de famille comme membre (ne pas mettre null)
        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(chefFamille); // ✅ CORRECTION : Utiliser chefFamille au lieu de null
        abonnement.setGym(gymUtilisateur);
        abonnement.setTypes(TypeAbonnements.FAMILIALE);
        abonnement.setPrixAbonnement(prixTotal);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());

        LocalDate dateFin = LocalDate.now().plusMonths(dto.getNombreMois().longValue());
        abonnement.setDateFinAbonnement(dateFin);
        abonnement.setDateRappelFinAbonnement(dateFin.minusDays(5));
        abonnement.setNombreDeMois(dto.getNombreMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setStatut(StatutAbonnement.EN_COURS);

        // 🔥 CORRECTION : Initialiser les champs optionnels pour éviter les null
        abonnement.setDatePauseAbonnement(null);
        abonnement.setDateResiliation(null);
        abonnement.setJoursAbsence(0);
        abonnement.setTypeDeService(null);

        Abonnement savedAbonnement = abonnementRepository.save(abonnement);
        mettreAJourStatutAbonnement(savedAbonnement);

        // Créer l'enregistrement de paiement
        creerEnregistrementListePaimentFamilial(savedAbonnement, famille, currentUser, "création");

        List<Abonnement> abonnements = List.of(savedAbonnement);
        factureCollectiveService.creeFactureCollective(famille, abonnements, prixTotal);

        logger.info("🎉 ABONNEMENT FAMILIAL CRÉÉ - Famille: {} ({} membres), Référence: {}, Prix: {}",
                famille.getNom(), membres.size(), chefFamille.getNom(), prixTotal);

        return abonnements;
    }
}