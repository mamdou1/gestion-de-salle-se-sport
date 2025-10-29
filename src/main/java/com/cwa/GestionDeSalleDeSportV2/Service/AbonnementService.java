package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class AbonnementService {

    private final Logger logger = LoggerFactory.getLogger(AbonnementService.class);

    private final AbonnementRepository abonnementRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final AbonnementEventService abonnementEventService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final TypeDeServiceRepository typeDeServiceRepository;
    private final ValidationInscriptionService validationInscriptionService;
    private final ListePaimentRepository listePaimentRepository;

    public AbonnementService(AbonnementRepository abonnementRepository, UserRepository userRepository, GymRepository gymRepository,
                             AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter,
                             TypeDeServiceRepository typeDeServiceRepository, ValidationInscriptionService validationInscriptionService,
                             ListePaimentRepository listePaimentRepository) {
        this.abonnementRepository = abonnementRepository;
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.validationInscriptionService = validationInscriptionService;
        this.listePaimentRepository = listePaimentRepository;
    }

    /**
     * Récupère le prix d'un abonnement en fonction du type de service et du genre
     * @param typeDeServiceId ID du type de service
     * @param genre Genre du membre (HOMME ou FEMME)
     * @return Prix en FCFA ou null si non trouvé
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public Double getPrixAbonnement(Long typeDeServiceId, String genre) throws AccessDeniedException {
        initializeAccess(true);
        TypeDeService typeDeService = typeDeServiceRepository.findById(typeDeServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Type de service non trouvé avec l'ID: " + typeDeServiceId));

        BigDecimal prix = "HOMME".equalsIgnoreCase(genre) ? typeDeService.getTarifHomme() : typeDeService.getTarifFemme();
        return prix != null ? prix.doubleValue() : null;
    }

    /**
     * Récupère le nombre d'abonnements journaliers
     * @return Nombre d'abonnements du jour
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreAbonnementsJournaliers() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les abonnements journaliers dans");

        LocalDate today = LocalDate.now();
        return abonnementRepository.countByGymIdAndDate(userGym.getId(), today);
    }

    /**
     * Récupère le montant total des abonnements journaliers
     * @return Montant total des abonnements du jour
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalJournalierAbonnements() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total journalier des abonnements dans");

        LocalDate today = LocalDate.now();
        List<Abonnement> abonnements = abonnementRepository.findByGymIdAndDate(userGym.getId(), today);

        return abonnements.stream()
                .map(Abonnement::getPrixAbonnement)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère le nombre d'abonnements hebdomadaires
     * @return Nombre d'abonnements de la semaine
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreAbonnementsHebdomadaires() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les abonnements hebdomadaires dans");

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        return abonnementRepository.countByGymIdAndWeek(userGym.getId(), startOfWeek, endOfWeek);
    }

    /**
     * Récupère le montant total des abonnements hebdomadaires
     * @return Montant total des abonnements de la semaine
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalHebdomadaireAbonnements() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total hebdomadaire des abonnements dans");

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        // Correction : Utiliser la méthode renommée
        List<Abonnement> abonnements = abonnementRepository.findByGymIdAndDateDebutAbonnementBetween(userGym.getId(), startOfWeek, endOfWeek);

        return abonnements.stream()
                .map(Abonnement::getPrixAbonnement)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère le nombre d'abonnements mensuels
     * @return Nombre d'abonnements du mois
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreAbonnementsMensuels() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les abonnements mensuels dans");

        LocalDate today = LocalDate.now();
        return abonnementRepository.countByGymIdAndMonth(userGym.getId(), today);
    }

    /**
     * Récupère le montant total des abonnements mensuels
     * @return Montant total des abonnements du mois
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalMensuelAbonnements() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total mensuel des abonnements dans");

        LocalDate today = LocalDate.now();
        List<Abonnement> abonnements = abonnementRepository.findByGymIdAndMonth(userGym.getId(), today.getYear(), today.getMonthValue());

        return abonnements.stream()
                .map(Abonnement::getPrixAbonnement)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère le nombre d'abonnements annuels
     * @return Nombre d'abonnements de l'année
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public long getNombreAbonnementsAnnuels() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les abonnements annuels dans");

        LocalDate today = LocalDate.now();
        // Correction : Passer today au lieu de today.getYear()
        return abonnementRepository.countByGymIdAndYear(userGym.getId(), today);
    }

    /**
     * Récupère le montant total des abonnements annuels
     * @return Montant total des abonnements de l'année
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getMontantTotalAnnuelAbonnements() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter le montant total annuel des abonnements dans");

        LocalDate today = LocalDate.now();
        List<Abonnement> abonnements = abonnementRepository.findByGymIdAndYear(userGym.getId(), today.getYear());

        return abonnements.stream()
                .map(Abonnement::getPrixAbonnement)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long nombreTotalMembreActif() throws AccessDeniedException {
        initializeAccess(true);
        return abonnementRepository.countByMembreActifs();
    }

    public long nombreTotalMembreExpirer() throws AccessDeniedException {
        initializeAccess(true);
        return abonnementRepository.countByMembreExpirer();
    }

    public long nombreTotalMembreBientotExpirer() throws AccessDeniedException {
        initializeAccess(true);
        return abonnementRepository.countByMembreBientotExpirer();
    }

    // 1. Mettre un abonnement en pause / reprendre
    public Abonnement mettreEnPause(Long idAbonnement, PauseAbonnementDTO dto) throws AccessDeniedException {
        if (dto.getJoursAbsence() < 7) {
            throw new IllegalArgumentException("Le nombre de jours d'absence doit être supérieur ou égal à 7 pour mettre l'abonnement en pause.");
        }

        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EN_COURS) {
            throw new IllegalStateException("Seul un abonnement en cours peut être mis en pause.");
        }

        abonnement.setStatut(StatutAbonnement.EN_PAUSE);
        abonnement.setDatePauseAbonnement(LocalDate.now());
        abonnement.setJoursAbsence(dto.getJoursAbsence());

        return abonnementRepository.save(abonnement);
    }

    public Abonnement reprendreAbonnement(Long idAbonnement) throws AccessDeniedException {
        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EN_PAUSE) {
            throw new IllegalStateException("Seul un abonnement en pause peut être repris.");
        }

        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setDatePauseAbonnement(null);
        abonnement.setJoursAbsence(null);

        return abonnementRepository.save(abonnement);
    }

    public Abonnement resilierAbonnement(Long idAbonnement) throws AccessDeniedException {
        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable"));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EN_COURS) {
            throw new RuntimeException("Seul un abonnement en cours peut être résilié.");
        }

        abonnement.setStatut(StatutAbonnement.RESILIE);
        abonnement.setPrixAbonnement(null);
        abonnement.setDateDebutAbonnement(null);
        abonnement.setDateFinAbonnement(null);
        abonnement.setDateResiliation(LocalDate.now());

        return abonnementRepository.save(abonnement);
    }

    // 2. Création d’un nouvel abonnement avec statut auto
    @Transactional
    public void ajouterAbonnement(AbonnementDTO dto) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User membre = userRepository.findById(dto.getMembreId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));
        Gym gym = currentUser.getGym();
        if (!membre.getGyms().contains(gym)) {
            throw new AccessDeniedException("Le membre n'est pas inscrit à ce gym.");
        }
        TypeDeService typeDeService = typeDeServiceRepository.findById(dto.getTypeDeServiceId())
                .orElseThrow(() -> new RuntimeException("Type de service non trouvé."));
        if (!typeDeService.getGym().equals(gym)) {
            throw new AccessDeniedException("Le type de service n'appartient pas à ce gym.");
        }

        Abonnement abonnement = new Abonnement();
        abonnement.setMembre(membre);
        abonnement.setGym(gym);
        abonnement.setTypes(TypeAbonnements.INDIVIDUEL);
        abonnement.setNombreDeMois(dto.getNombreDeMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());
        LocalDate dateFin = LocalDate.now().plusMonths(dto.getNombreDeMois().longValue());
        abonnement.setDateFinAbonnement(dateFin);
        abonnement.setDateRappelFinAbonnement(dateFin.minusDays(5));
        abonnement.setStatut(calculStatutAbonnemnt(abonnement));
        abonnement.setTypeDeService(typeDeService);

        // Calcul du prix basé sur le genre du membre
        BigDecimal prixUnitaire = (membre.getGenre() == Genre.HOMME) ? typeDeService.getTarifHomme() : typeDeService.getTarifFemme();
        BigDecimal prixTotal = prixUnitaire.multiply(dto.getNombreDeMois());
        abonnement.setPrixAbonnement(prixTotal);

        // Sauvegarde de l'abonnement
        Abonnement savedAbonnement = abonnementRepository.save(abonnement);

        // Création de l'entrée dans liste_paiment
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.ABONNEMENT);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(prixTotal);
        paiement.setModeDePaiement(dto.getModeDePaiement());
        paiement.setReferenceId(savedAbonnement.getId());
        paiement.setAcheteur(membre);
        paiement.setStaffEnregistreur(currentUser);
        paiement.setGym(gym);
        paiement.setDetails("Abonnement: " + typeDeService.getNom() + " (" + dto.getNombreDeMois() + " mois)");
        listePaimentRepository.save(paiement);
        logger.info("Created ListePaiment for abonnement: {}", paiement);

        // Envoi d’une facture suite à l'ajout
        abonnementEventService.envoyerFactureParEmail(membre, abonnement, "Validation");
    }

    // 3. Renouvellement de l'abonnement existant
    @Transactional
    public Abonnement renouvelerAbonnement(Long id, RenouvelerAbonnementDTO dto) throws MessagingException, AccessDeniedException {
        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        LocalDate aujourdHui = LocalDate.now();
        TypeDeService typeDeService = abonnement.getTypeDeService();
        User membre = (User) abonnement.getMembre();
        BigDecimal tarifUnitaire = (membre.getGenre() == Genre.HOMME) ? typeDeService.getTarifHomme() : typeDeService.getTarifFemme();

        if (abonnement.getStatut() == StatutAbonnement.EN_COURS || abonnement.getStatut() == StatutAbonnement.BIENTOT_EXPIRE) {
            LocalDate nouvelleDateFin = abonnement.getDateFinAbonnement().plusMonths(dto.getAjoutMois());
            abonnement.setDateFinAbonnement(nouvelleDateFin);
            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            abonnement.setNombreDeMois(abonnement.getNombreDeMois().add(BigDecimal.valueOf(dto.getAjoutMois())));
            abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());
            abonnement.setModeDePaiement(dto.getModeDePaiement());
        } else if (abonnement.getStatut() == StatutAbonnement.EXPIRE || abonnement.getStatut() == StatutAbonnement.RESILIE) {
            LocalDate nouvelleDateFin = aujourdHui.plusMonths(dto.getAjoutMois());
            abonnement.setDateDebutAbonnement(aujourdHui);
            abonnement.setDateFinAbonnement(nouvelleDateFin);
            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            abonnement.setNombreDeMois(BigDecimal.valueOf(dto.getAjoutMois()));
            abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());
            abonnement.setModeDePaiement(dto.getModeDePaiement());
        }

        // Recalcul du prix
        BigDecimal nouveauPrix = tarifUnitaire.multiply(BigDecimal.valueOf(dto.getAjoutMois()));
        abonnement.setPrixAbonnement(nouveauPrix);
        abonnement.setStatut(calculStatutAbonnemnt(abonnement));

        // Sauvegarde de l'abonnement
        Abonnement savedAbonnement = abonnementRepository.save(abonnement);

        // Création de l'entrée dans liste_paiment
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.ABONNEMENT);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(nouveauPrix);
        paiement.setModeDePaiement(dto.getModeDePaiement());
        paiement.setReferenceId(savedAbonnement.getId());
        paiement.setAcheteur(membre);
        paiement.setStaffEnregistreur(currentUser);
        paiement.setGym(abonnement.getGym());
        paiement.setDetails("Renouvellement Abonnement: " + typeDeService.getNom() + " (" + dto.getAjoutMois() + " mois)");
        listePaimentRepository.save(paiement);
        logger.info("Created ListePaiment for renouvellement: {}", paiement);

        abonnementEventService.envoyerFactureParEmail(membre, abonnement, "Renouvellement");
        return abonnement;
    }

    // 4. Mise à jour du statut automatiquement
    public Abonnement mettreAJourStatutAutomatiquement(Long id) throws AccessDeniedException {
        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        StatutAbonnement nouveauStatut = calculStatutAbonnemnt(abonnement);

        if (!abonnement.getStatut().equals(nouveauStatut)) {
            abonnement.setStatut(nouveauStatut);
            logger.info("✅ Statut mis à jour : {}", nouveauStatut);
        } else {
            logger.info("ℹ️ Statut inchangé : {}", abonnement.getStatut());
        }
        return abonnementRepository.save(abonnement);
    }

    public StatutAbonnement calculStatutAbonnemnt(Abonnement abonnement) {
        LocalDate aujourdHui = LocalDate.now();

        if (abonnement.getStatut() == StatutAbonnement.RESILIE || abonnement.getStatut() == StatutAbonnement.EN_PAUSE) {
            return abonnement.getStatut();
        }

        LocalDate debut = abonnement.getDateDebutAbonnement();
        LocalDate rappelFin = abonnement.getDateRappelFinAbonnement();
        LocalDate fin = abonnement.getDateFinAbonnement();

        if (debut != null && rappelFin != null && !aujourdHui.isBefore(debut) && aujourdHui.isBefore(rappelFin)) {
            return StatutAbonnement.EN_COURS;
        }

        if (rappelFin != null && aujourdHui.isEqual(rappelFin)) {
            return StatutAbonnement.BIENTOT_EXPIRE;
        }

        if (fin != null && aujourdHui.isAfter(fin)) {
            return StatutAbonnement.EXPIRE;
        }

        return StatutAbonnement.RESILIE;
    }

    // 5. Changement de plan d’abonnement avec calcul du reste
    public ResponseEntity<String> gererChangementAbonnement(Long idAbonnement, BigDecimal nouveauAbonnement, LocalDate dateChangement) throws AccessDeniedException {
        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        BigDecimal ancienAbonnement = abonnement.getPrixAbonnement();
        if (nouveauAbonnement.compareTo(ancienAbonnement) < 0) {
            throw new RuntimeException("Vous ne pouvez pas passer à un abonnement moins cher.");
        }

        dateChangement = LocalDate.now();
        LocalDate debut = abonnement.getDateDebutAbonnement();
        LocalDate fin = abonnement.getDateFinAbonnement();
        long jourUtilise = ChronoUnit.DAYS.between(debut, dateChangement);
        long dureeTotale = ChronoUnit.DAYS.between(debut, fin);

        if (jourUtilise < 0 || jourUtilise > dureeTotale) {
            throw new RuntimeException("Date de changement invalide.");
        }

        BigDecimal prixParJour = ancienAbonnement.divide(BigDecimal.valueOf(dureeTotale), 2, RoundingMode.HALF_UP);
        BigDecimal montantConsomme = prixParJour.multiply(BigDecimal.valueOf(jourUtilise));
        BigDecimal montantRestant = ancienAbonnement.subtract(montantConsomme);
        BigDecimal montantComplementaire = nouveauAbonnement.subtract(montantRestant);

        abonnement.setPrixAbonnement(nouveauAbonnement);
        abonnement.setStatut(calculStatutAbonnemnt(abonnement));
        abonnementRepository.save(abonnement);

        String message = String.format("""
                Changement accepté
                Jour utilisé : %d
                Prix journalier : %.2f
                Montant consommé : %.2f
                Reste non consommé : %.2f
                Montant à ajouter pour le nouveau plan : %.2f
                Nouveau statut : %s
                """,
                jourUtilise,
                prixParJour,
                montantConsomme,
                montantRestant,
                montantComplementaire,
                abonnement.getStatut().name());

        return ResponseEntity.ok(message);
    }

    // 6. Afficher tout le abonnement
    public List<Abonnement> getAllAbonnement() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();

        return abonnementRepository.findByGymIn(userGyms);
    }

    public Abonnement getAbonnementById(Long abonnementId) throws AccessDeniedException {
        initializeAccess(true);
        return abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable"));
    }

    // 7. L'historique des abonnement d'un membre
    public List<Abonnement> getHistoriqueAbonnementParMembre(Long membreId) throws AccessDeniedException {
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable."));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(membre.getGym())) {
            throw new AccessDeniedException("Accès refusé à l'historique de ce membre.");
        }

        return abonnementRepository.findByMembreOrderByDateDebutAbonnementDesc(membre);
    }

    public List<Abonnement> getHistoriqueAbonnementParMembreApp() throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        return abonnementRepository.findByMembreOrderByDateDebutAbonnementDesc(currentUser);
    }

    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }

    // 8. Supprimer abonnement
    public void supprimerAbonnement(Long abonnementId) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new AccessDeniedException("Seul les membres du staff peuvent supprimer un abonnement.");
        }

        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé : l'abonnement n'appartient pas à votre gym.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EXPIRE && abonnement.getStatut() != StatutAbonnement.RESILIE) {
            throw new RuntimeException("Impossible de supprimer un abonnement dont le statut est actif ou bientôt expiré.");
        }

        abonnementRepository.delete(abonnement);
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }

    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }

    // === NOUVELLES MÉTHODES DTO ===

    /**
     * Récupère tous les abonnements en DTO
     */
    public List<AbonnementDTO> getAllAbonnementsDTO() {
        try {
            logger.info("🔍 Récupération de tous les abonnements via DTO");
            List<AbonnementDTO> dtos = abonnementRepository.findAllAbonnementDTOs();
            logger.info("✅ {} abonnements récupérés via DTO", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements via DTO", e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements");
        }
    }

    /**
     * Récupère les abonnements avec filtres en DTO
     */
    public List<AbonnementDTO> getAbonnementsDTOByFilters(Long gymId, StatutAbonnement statut, TypeAbonnements type) {
        try {
            logger.info("🔍 Récupération abonnements filtrés - gym: {}, statut: {}, type: {}", gymId, statut, type);
            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsDTOByFilters(gymId, statut, type);
            logger.info("✅ {} abonnements filtrés récupérés via DTO", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("💥 Erreur lors du filtrage des abonnements via DTO", e);
            throw new RuntimeException("Erreur lors du filtrage des abonnements");
        }
    }

    /**
     * Récupère les abonnements par gym en DTO
     */
    public List<AbonnementDTO> getAbonnementsDTOByGymId(Long gymId) {
        try {
            logger.info("🔍 Récupération abonnements par gym {}", gymId);
            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsDTOByGymId(gymId);
            logger.info("✅ {} abonnements récupérés pour le gym {} via DTO", dtos.size(), gymId);
            return dtos;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements par gym via DTO", e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements par gym");
        }
    }

    /**
     * Récupère les abonnements actifs en DTO
     */
    public List<AbonnementDTO> getAbonnementsActifsDTO() {
        try {
            logger.info("🔍 Récupération abonnements actifs via DTO");
            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsActifsDTO();
            logger.info("✅ {} abonnements actifs récupérés via DTO", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements actifs via DTO", e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements actifs");
        }
    }

    /**
     * Récupère un abonnement spécifique par ID en DTO
     */
    public AbonnementDTO getAbonnementDTOById(Long abonnementId) {
        try {
            logger.info("🔍 Récupération abonnement {} via DTO", abonnementId);
            AbonnementDTO dto = abonnementRepository.findAbonnementDTOById(abonnementId)
                    .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));
            logger.info("✅ Abonnement {} récupéré via DTO", abonnementId);
            return dto;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération de l'abonnement {} via DTO", abonnementId, e);
            throw new RuntimeException("Erreur lors de la récupération de l'abonnement");
        }
    }

    /**
     * Récupère les abonnements d'un membre en DTO
     */
    public List<AbonnementDTO> getAbonnementsDTOByMembreId(Long membreId) {
        try {
            logger.info("🔍 Récupération abonnements pour le membre {}", membreId);
            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsDTOByMembreId(membreId);
            logger.info("✅ {} abonnements récupérés pour le membre {} via DTO", dtos.size(), membreId);
            return dtos;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements du membre {} via DTO", membreId, e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements du membre");
        }
    }

    /**
     * Récupère les abonnements d'une famille en DTO
     */
    public List<AbonnementDTO> getAbonnementsDTOByFamilleId(Long familleId) {
        try {
            logger.info("🔍 Récupération abonnements pour la famille {}", familleId);
            List<AbonnementDTO> dtos = abonnementRepository.findAbonnementsDTOByFamilleId(familleId);
            logger.info("✅ {} abonnements récupérés pour la famille {} via DTO", dtos.size(), familleId);
            return dtos;
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements de la famille {} via DTO", familleId, e);
            throw new RuntimeException("Erreur lors de la récupération des abonnements de la famille");
        }
    }

}