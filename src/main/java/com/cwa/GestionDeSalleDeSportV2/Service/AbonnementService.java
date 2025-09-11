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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


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

    public AbonnementService(AbonnementRepository abonnementRepository, UserRepository userRepository, GymRepository gymRepository, AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, TypeDeServiceRepository typeDeServiceRepository, ValidationInscriptionService validationInscriptionService) {
        this.abonnementRepository = abonnementRepository;
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.validationInscriptionService = validationInscriptionService;
    }

    // 1. Mettre un abonnement en pause / reprendre
    public Abonnement mettreEnPause(Long idAbonnement, PauseAbonnementDTO dto) throws AccessDeniedException {
        if (dto.getJoursAbsence() < 7) {
            throw new IllegalArgumentException("Le nombre de jours d'absence doit être supérieur ou égal à 7 pour mettre l'abonnement en pause.");
        }

        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())){
            throw new AccessDeniedException("Accès refusé à cet abonnement. ");
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
                .orElseThrow(()-> new RuntimeException("Abonnement introuvable"));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EN_COURS){
            throw new RuntimeException("Seul un abonnement en cours peut être resilier.");
        }

        abonnement.setStatut(StatutAbonnement.RESILIE);
        abonnement.setPrixAbonnement(null);
        abonnement.setDateDebutAbonnement(null);
        abonnement.setDateFinAbonnement(null);

        LocalDate aujourd_hui = LocalDate.now();

        abonnement.setDateResiliation(aujourd_hui);

        return abonnementRepository.save(abonnement);
    }


    // 2. Création d’un nouvel abonnement avec statut auto
    public void ajouterAbonnement(AbonnementDTO dto) throws MessagingException, AccessDeniedException {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User membre = userRepository.findById(dto.getMembreId())
                .orElseThrow(() -> new RuntimeException("Utilisiteur non trouvé."));
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
        abonnement.setDateFinAbonnement(LocalDate.now().plusMonths(dto.getNombreDeMois().longValue()));
        abonnement.setDateRappelFinAbonnement(LocalDate.now()
                .plusMonths(dto.getNombreDeMois().longValue())
                .minusDays(5));
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setStatut(calculStatutAbonnemnt(abonnement));
        abonnement.setTypeDeService(membre.getTypeDeService());
        
//        Genre genre = membre.getGenre();
//        BigDecimal prix = BigDecimal.ZERO;
//        if (typeDeService.getTarifUnique() != null){
//            prix = typeDeService.getTarifUnique();
//        } else if (genre == Genre.HOMME && typeDeService.getTarifHomme() != null) {
//            prix = typeDeService.getTarifHomme();
//        } else if (genre == Genre.FEMME && typeDeService.getTarifFemme() != null){
//            prix = typeDeService.getTarifHomme();
//        } else  {
//            throw new RuntimeException("Aucun tarif défini pour ce service et ce genre.");
//        }
        BigDecimal prix= validationInscriptionService.getTarif(membre.getGenre(), typeDeService.getId());
        abonnement.setPrixAbonnement(prix);

        abonnementRepository.save(abonnement);

        //  Envoi d’une facture suite à l'ajout
        abonnementEventService.envoyerFactureParEmail((User) abonnement.getMembre(),abonnement, "Validaton");

    }

    // 3. Renouvellement de l'abonnement existant
    @Transactional
    public Abonnement renouvelerAbonnement(Long id, Integer ajoutMois, Double nouveauxPrix) throws MessagingException, AccessDeniedException {
        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        LocalDate aujourd_hui = LocalDate.now();

        // Cas 1 : Abonnement en cours ou bientôt expiré → on ajoute à la date de fin actuelle
        if (abonnement.getStatut() == StatutAbonnement.EN_COURS ||
                abonnement.getStatut() == StatutAbonnement.BIENTOT_EXPIRE) {

            LocalDate nouvelleDateFin = abonnement.getDateFinAbonnement().plusMonths(ajoutMois);

            abonnement.setDateFinAbonnement(nouvelleDateFin);
            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            abonnement.setNombreDeMois(abonnement.getNombreDeMois().add(BigInteger.valueOf(ajoutMois)));
            abonnement.setPeriodAbonnement(abonnement.getPeriodAbonnement());

        }

        // Cas 2 : Abonnement expiré ou résilié → nouvelle période à partir d’aujourd’hui
        else if (abonnement.getStatut() == StatutAbonnement.EXPIRE ||
                abonnement.getStatut() == StatutAbonnement.RESILIE) {

            LocalDate nouvelleDateFin = aujourd_hui.plusMonths(ajoutMois);

            abonnement.setDateDebutAbonnement(aujourd_hui);
            abonnement.setDateFinAbonnement(nouvelleDateFin);
            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            abonnement.setNombreDeMois(BigInteger.valueOf(ajoutMois));
            abonnement.setPeriodAbonnement(abonnement.getPeriodAbonnement());
        }


        if (nouveauxPrix != null) {
            BigDecimal prixActuel = abonnement.getPrixAbonnement() != null
                    ? abonnement.getPrixAbonnement()
                    : BigDecimal.ZERO;

            abonnement.setPrixAbonnement(prixActuel.add(BigDecimal.valueOf(nouveauxPrix)));

        }

        abonnement.setStatut(calculStatutAbonnemnt(abonnement));
        abonnementRepository.save(abonnement);

        // Envoi d’une facture suite au renouvellement
        abonnementEventService.envoyerFactureParEmail((User) abonnement.getMembre(),abonnement, "Renouvellement");
        return abonnement;
    }



    // 4. Mise à jour du statut automatiquement
    public Abonnement mettreAJourStatutAutomatiquement(Long id) throws AccessDeniedException {
            Abonnement abonnement = abonnementRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        StatutAbonnement nouveauStatut = calculStatutAbonnemnt(abonnement);

        if (!abonnement.getStatut().equals(nouveauStatut)) {
            abonnement.setStatut(nouveauStatut);
            abonnement = abonnementRepository.save(abonnement);
            System.out.println("✅ Statut mis à jour : " + nouveauStatut);
        } else {
            System.out.println("ℹ️ Statut inchangé : " + abonnement.getStatut());
        }
            return abonnementRepository.save(abonnement);
    }

    public StatutAbonnement calculStatutAbonnemnt(Abonnement abonnement) {

        LocalDate aujourd_hui = LocalDate.now();

        if (abonnement.getStatut() == StatutAbonnement.RESILIE ||
                abonnement.getStatut() == StatutAbonnement.EN_PAUSE) {
            return abonnement.getStatut();
        }

        LocalDate debut = abonnement.getDateDebutAbonnement();
        LocalDate rappelFin = abonnement.getDateRappelFinAbonnement();
        LocalDate fin = abonnement.getDateFinAbonnement();

        if (debut != null && rappelFin != null &&
                !aujourd_hui.isBefore(debut) && aujourd_hui.isBefore(rappelFin)) {
            return StatutAbonnement.EN_COURS;
        }

        if (rappelFin != null && aujourd_hui.isEqual(rappelFin)) {
            return StatutAbonnement.BIENTOT_EXPIRE;
        }

        if (fin != null && aujourd_hui.isAfter(fin)) {
            return StatutAbonnement.EXPIRE;
        }

        return StatutAbonnement.RESILIE;
    }

    // 5. Changement de plan d’abonnement avec calcul du reste
    public ResponseEntity<String> gererChangementAbonnement(Long idAbonnement, BigDecimal nouveauAbonnement, LocalDate dateChangement) throws AccessDeniedException {

            Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                    .orElseThrow(()-> new RuntimeException("Abonnement introuvable."));

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(abonnement.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

            BigDecimal ancienAbonnement = abonnement.getPrixAbonnement();
            if (nouveauAbonnement.compareTo(ancienAbonnement)<0){
                throw new RuntimeException("Vous pouvez pas quitter d'un abonnement moins cher pour un abonemnt plus cher.");
            }

            dateChangement = LocalDate.now();
            LocalDate debut = abonnement.getDateDebutAbonnement();
            LocalDate fin   = abonnement.getDateFinAbonnement();
            long jourUtiliser = ChronoUnit.DAYS.between(debut, dateChangement);
            long durerTotal = ChronoUnit.DAYS.between(debut, fin);

            if (jourUtiliser < 0 || jourUtiliser > durerTotal){
                throw new RuntimeException("Date de changement invalid.");
            }

            BigDecimal prixParJour = ancienAbonnement.divide(BigDecimal.valueOf(durerTotal), 2, RoundingMode.HALF_UP);
            BigDecimal montantConsommer = prixParJour.multiply(BigDecimal.valueOf(jourUtiliser));
            BigDecimal montantRestant = ancienAbonnement.subtract(montantConsommer);
            BigDecimal montantComplementaire = nouveauAbonnement.subtract(montantRestant);

            abonnement.setPrixAbonnement(nouveauAbonnement);
            abonnement.setStatut(calculStatutAbonnemnt(abonnement));
            abonnementRepository.save(abonnement);

            String message = String.format("""
                    Changement accepté
                    Jour utilisé : %d
                    Prix journalière : %.2f
                    Montant Consommer : %.2f
                    Reste non consommer : %.2f
                    Montant à ajouter pour le nouveau plan : %.2f
                    Nouveau statut : %s
                    """,
                    jourUtiliser,
                    prixParJour,
                    montantConsommer,
                    montantRestant,
                    montantComplementaire,
                    abonnement.getStatut().name());

            return ResponseEntity.ok(message);
    }

    //  6.  Afficher tout le abonnement
    public List<Abonnement> getAllAbonnement() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms(); // Utilise la liste des gyms

        List<Abonnement> abonnements = abonnementRepository.findByGymIn(userGyms);
        return abonnements;
    }

    public Abonnement getAbonnementById(Long abonnementId) throws AccessDeniedException {
        initializeAccess(true);
        return abonnementRepository.findById(abonnementId)
                .orElseThrow(()->new RuntimeException("Abonnement introuvable"));
    }

    //  7.  L'historique des abonnement d'un membre
    public List<Abonnement> getHistoriqueAbonnementParMembre(Long membreId) throws AccessDeniedException {
        User membre = userRepository.findById(membreId)
                .orElseThrow(()-> new RuntimeException("Membre introuvable."));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getGyms().contains(membre.getGym())) {
            throw new AccessDeniedException("Accès refusé à l'historique de ce membre.");
        }

        return abonnementRepository.findByMembreOrderByDateDebutAbonnementDesc(membre);
    }

    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }


    //  8.  Supprimer abonnement
    public void supprimerAbonnement(Long membreId) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw new AccessDeniedException("Seul les membres du staff peuvent supprimer un membre");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Utilisiteur non trouvé."));
        if (!membre.getGyms().contains(membre.getGyms())) {
            throw new AccessDeniedException("Le membre n'est pas inscrit à ce gym.");
        }

        Abonnement abonnement = abonnementRepository.findById(membreId)
                .orElseThrow(()->new RuntimeException("Abonnement introuvable"));

        if (abonnement.getStatut() != StatutAbonnement.EXPIRE || abonnement.getStatut() != StatutAbonnement.RESILIE){
            throw new RuntimeException("Impossible de supprimer un abonnement dont le staut est actif ou bientôt expirer");
        }
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

