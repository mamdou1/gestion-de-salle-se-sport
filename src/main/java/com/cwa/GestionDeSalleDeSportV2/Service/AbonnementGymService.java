package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementGymDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.PauseAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
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
public class AbonnementGymService {

    private final Logger logger = LoggerFactory.getLogger(AbonnementService.class);

    private final AbonnementGymRepository abonnementGymRepository;
    private final AbonnementEventService abonnementEventService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final GymRepository gymRepository;

    public AbonnementGymService(AbonnementGymRepository abonnementGymRepository, AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, GymRepository gymRepository) {
        this.abonnementGymRepository = abonnementGymRepository;
        this.gymRepository = gymRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    // 1. Mettre un abonnement en pause / reprendre
    public AbonnementGym mettreEnPause(Long idAbonnement, PauseAbonnementDTO dto) throws AccessDeniedException {

        initializeAccess(true);

        if (dto.getJoursAbsence() < 7) {
            throw new IllegalArgumentException("Le nombre de jours d'attente doit être supérieur ou égal à 7 pour mettre l'abonnement en pause.");
        }

        AbonnementGym abonnement = abonnementGymRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));


        if (abonnement.getStatut() != StatutAbonnement.EN_COURS) {
            throw new IllegalStateException("Seul un abonnement en cours peut être mis en pause.");
        }

        abonnement.setStatut(StatutAbonnement.EN_PAUSE);
        abonnement.setDatePauseAbonnement(LocalDate.now());
        abonnement.setJoursAbsence(dto.getJoursAbsence());

        return abonnementGymRepository.save(abonnement);
    }

    public AbonnementGym reprendreAbonnement(Long idAbonnement) throws AccessDeniedException {

        initializeAccess(true);

        AbonnementGym abonnement = abonnementGymRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));


        if (abonnement.getStatut() != StatutAbonnement.EN_PAUSE) {
            throw new IllegalStateException("Seul un abonnement en pause peut être repris.");
        }

        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setDatePauseAbonnement(null);
        abonnement.setJoursAbsence(null);

        return abonnementGymRepository.save(abonnement);
    }

    public AbonnementGym resilierAbonnement(Long idAbonnement) throws AccessDeniedException {

        initializeAccess(true);

        AbonnementGym abonnement = abonnementGymRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        if (abonnement.getStatut() != StatutAbonnement.EN_COURS){
            throw new RuntimeException("Seul un abonnement en cours peut être resilier.");
        }

        abonnement.setStatut(StatutAbonnement.RESILIE);
        abonnement.setPrixAbonnement(null);
        abonnement.setDateDebutAbonnement(null);
        abonnement.setDateFinAbonnement(null);

        LocalDate aujourd_hui = LocalDate.now();

        abonnement.setDateResiliation(aujourd_hui);

        return abonnementGymRepository.save(abonnement);
    }


    // 2. Création d’un nouvel abonnement avec statut auto
    public void ajouterAbonnement(AbonnementGymDTO dto) throws MessagingException, AccessDeniedException {
        User currentUser = initializeAccess(true);

        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(()->new RuntimeException("Gym non trouvé."));

        AbonnementGym abonnementGym = new AbonnementGym();
        abonnementGym.setGym(gym);
        abonnementGym.setNombreDeMois(dto.getNombreDeMois());
        abonnementGym.setModeDePaiement(dto.getModeDePaiement());
        abonnementGym.setEnregistrerPar(currentUser);
        abonnementGym.setDateDebutAbonnement(LocalDate.now());
        abonnementGym.setPeriodAbonnement(dto.getPeriodAbonnement());
        abonnementGym.setDateFinAbonnement(LocalDate.now().plusMonths(dto.getNombreDeMois().longValue()));
        abonnementGym.setDateRappelFinAbonnement(LocalDate.now()
                .plusMonths(dto.getNombreDeMois().longValue())
                .minusDays(5));
        abonnementGym.setStatut(StatutAbonnement.EN_COURS);
        abonnementGym.setStatut(calculStatutAbonnemnt(abonnementGym));

        abonnementGym.setPrixAbonnement(dto.getPrixAbonnement());

        abonnementGymRepository.save(abonnementGym);

        //  Envoi d’une facture suite à l'ajout
        abonnementEventService.envoyerFactureAdminAGymParEmail(abonnementGym.getGym(), abonnementGym, "Validaton");

    }

    // 3. Renouvellement de l'abonnement existant
//    @Transactional
//    public AbonnementGym renouvelerAbonnement(Long id, Integer ajoutMois, Double nouveauxPrix) throws MessagingException, AccessDeniedException {
//
//        initializeAccess(true);
//
//        AbonnementGym abonnement = abonnementGymRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));
//
//        LocalDate aujourd_hui = LocalDate.now();
//
//        // Cas 1 : Abonnement en cours ou bientôt expiré → on ajoute à la date de fin actuelle
//        if (abonnement.getStatut() == StatutAbonnement.EN_COURS ||
//                abonnement.getStatut() == StatutAbonnement.BIENTOT_EXPIRE) {
//
//            LocalDate nouvelleDateFin = abonnement.getDateFinAbonnement().plusMonths(ajoutMois);
//
//            abonnement.setDateFinAbonnement(nouvelleDateFin);
//            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
//            abonnement.setNombreDeMois(abonnement.getNombreDeMois().add(BigInteger.valueOf(ajoutMois)));
//            abonnement.setPeriodAbonnement(abonnement.getPeriodAbonnement());
//
//        }
//
//        // Cas 2 : Abonnement expiré ou résilié → nouvelle période à partir d’aujourd’hui
//        else if (abonnement.getStatut() == StatutAbonnement.EXPIRE ||
//                abonnement.getStatut() == StatutAbonnement.RESILIE) {
//
//            LocalDate nouvelleDateFin = aujourd_hui.plusMonths(ajoutMois);
//
//            abonnement.setDateDebutAbonnement(aujourd_hui);
//            abonnement.setDateFinAbonnement(nouvelleDateFin);
//            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
//            abonnement.setNombreDeMois(BigInteger.valueOf(ajoutMois));
//            abonnement.setPeriodAbonnement(abonnement.getPeriodAbonnement());
//            abonnement.setStatut(StatutAbonnement.EN_COURS);
//        }
//
//
//        if (nouveauxPrix != null) {
//            BigDecimal prixActuel = abonnement.getPrixAbonnement() != null
//                    ? abonnement.getPrixAbonnement()
//                    : BigDecimal.ZERO;
//
//            abonnement.setPrixAbonnement(prixActuel.add(BigDecimal.valueOf(nouveauxPrix)));
//        }
//
//        abonnement.setStatut(calculStatutAbonnemnt(abonnement));
//        abonnementGymRepository.save(abonnement);
//
//        // Envoi d’une facture suite au renouvellement
//        abonnementEventService.envoyerFactureAdminAGymParEmail(abonnement.getGym(), abonnement, "Renouvellement");
//        return abonnement;
//    }



    // 4. Mise à jour du statut automatiquement
    public AbonnementGym mettreAJourStatutAutomatiquement(Long id) throws AccessDeniedException {
        initializeAccess(true);

        AbonnementGym abonnement = abonnementGymRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        StatutAbonnement nouveauStatut = calculStatutAbonnemnt(abonnement);

        if (!abonnement.getStatut().equals(nouveauStatut)) {
            abonnement.setStatut(nouveauStatut);
            abonnement = abonnementGymRepository.save(abonnement);
            System.out.println("✅ Statut mis à jour : " + nouveauStatut);
        } else {
            System.out.println("ℹ️ Statut inchangé : " + abonnement.getStatut());
        }

        return abonnement;
    }



    private StatutAbonnement calculStatutAbonnemnt(AbonnementGym abonnementGym) {
        LocalDate aujourdHui = LocalDate.now();

        StatutAbonnement statutActuel = abonnementGym.getStatut();
        LocalDate debut = abonnementGym.getDateDebutAbonnement();
        LocalDate rappelFin = abonnementGym.getDateRappelFinAbonnement();
        LocalDate fin = abonnementGym.getDateFinAbonnement();

        // 1. Abonnement en cours
        if (debut != null && rappelFin != null &&
                !aujourdHui.isBefore(debut) && aujourdHui.isBefore(rappelFin)) {
            return StatutAbonnement.EN_COURS;
        }

        // 2. Bientôt expiré
        if (rappelFin != null && aujourdHui.isEqual(rappelFin)) {
            return StatutAbonnement.BIENTOT_EXPIRE;
        }

        // 3. Expiré
        if (fin != null && aujourdHui.isAfter(fin)) {
            return StatutAbonnement.EXPIRE;
        }

        // 4. Statut manuel prioritaire
        if (statutActuel == StatutAbonnement.RESILIE || statutActuel == StatutAbonnement.EN_PAUSE) {
            return statutActuel;
        }

        // 5. Par défaut
        return StatutAbonnement.RESILIE;
    }


    // 5. Changement de plan d’abonnement avec calcul du reste
    public ResponseEntity<String> gererChangementAbonnement(Long idAbonnement, BigDecimal nouveauAbonnement, LocalDate dateChangement) throws AccessDeniedException {

        AbonnementGym abonnement = abonnementGymRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        initializeAccess(true);

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
        abonnementGymRepository.save(abonnement);

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
    public List<AbonnementGym> getAllAbonnement() throws AccessDeniedException {
        initializeAccess(true);

        List<AbonnementGym> abonnements = abonnementGymRepository.findAll();
        return abonnements;
    }

    public AbonnementGym getAbonnementById(Long abonnementId) throws AccessDeniedException {
        initializeAccess(true);
        return abonnementGymRepository.findById(abonnementId)
                .orElseThrow(()->new RuntimeException("Abonnement introuvable"));
    }

    //  7.  L'historique des abonnement d'un membre
    public List<AbonnementGym> getHistoriqueAbonnementParMembre(Long gymId) throws AccessDeniedException {

        initializeAccess(true);

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(()-> new RuntimeException("Gym introuvable."));

        return abonnementGymRepository.findByGymOrderByDateDebutAbonnementDesc(gym);
    }

    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }


    //  8.  Supprimer abonnement
    public void supprimerAbonnement(Long idAbonnement) throws AccessDeniedException {

        initializeAccess(true);

        AbonnementGym abonnement = abonnementGymRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        if (abonnement.getStatut() != StatutAbonnement.EXPIRE || abonnement.getStatut() != StatutAbonnement.RESILIE){
            throw new RuntimeException("Impossible de supprimer un abonnement dont le staut est actif ou bientôt expirer");
        }
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN_PRINCIPAL) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        return currentUser;
    }


}
