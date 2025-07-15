package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
public class AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final AbonnementEventService abonnementEventService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public AbonnementService(AbonnementRepository abonnementRepository, UserRepository userRepository, GymRepository gymRepository, AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.abonnementRepository = abonnementRepository;
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    // 1. Mettre un abonnement en pause / reprendre
    public Abonnement mettreEnPause(Long idAbonnement) {

        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        abonnement.setStatut(StatutAbonnement.EN_PAUSE);
        abonnement.setDatePauseAbonnement(LocalDate.now());

        return abonnementRepository.save(abonnement);
    }

    public Abonnement reprendreAbonnement(Long idAbonnement) {
        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setDatePauseAbonnement(null);

        return abonnementRepository.save(abonnement);
    }

    // 2. Création d’un nouvel abonnement avec statut auto
    public void ajouterAbonnement(AbonnementDTO dto) throws MessagingException {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User membre = userRepository.findById(dto.getMembreId())
                .orElseThrow(() -> new RuntimeException("Utilisiteur non trouvé."));
        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Gym non trouvé:"));

        Abonnement abonnement = new Abonnement();
        abonnement.setMembre(membre);
        abonnement.setGym(gym);
        abonnement.setType(dto.getType());
        abonnement.setPrixAbonnement(dto.getPrixAbonnement());
        abonnement.setNombreDeMois(dto.getNombreDeMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setDateFinAbonnement(LocalDate.now().plusMonths(dto.getNombreDeMois().longValue()));
        abonnement.setDateRappelFinAbonnement(LocalDate.now()
                .plusMonths(dto.getNombreDeMois().longValue())
                .minusDays(5));
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setStatut(calculStatutAbonnemnt(abonnement));

        abonnementRepository.save(abonnement);

        // 📦 Envoi d’une facture suite à l'ajout
        abonnementEventService.envoyerFactureParEmail(abonnement.getMembre(),abonnement, "Validaton");

    }

    // 3. Renouvellement de l'abonnement existant
    @Transactional
    public Abonnement renouvelerAbonnement(Long id, Integer ajoutMois, Double nouveauxPrix) throws MessagingException {
        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));

        LocalDate aujourd_hui = LocalDate.now();

        // Cas 1 : Abonnement en cours ou bientôt expiré → on ajoute à la date de fin actuelle
        if (abonnement.getStatut() == StatutAbonnement.EN_COURS ||
                abonnement.getStatut() == StatutAbonnement.BIENTOT_EXPIRE) {

            LocalDate nouvelleDateFin = abonnement.getDateFinAbonnement().plusMonths(ajoutMois);

            abonnement.setDateFinAbonnement(nouvelleDateFin);
            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            abonnement.setNombreDeMois(abonnement.getNombreDeMois().add(BigInteger.valueOf(ajoutMois)));

        }

        // Cas 2 : Abonnement expiré ou résilié → nouvelle période à partir d’aujourd’hui
        else if (abonnement.getStatut() == StatutAbonnement.EXPIRE ||
                abonnement.getStatut() == StatutAbonnement.RESILIE) {

            LocalDate nouvelleDateFin = aujourd_hui.plusMonths(ajoutMois);

            abonnement.setDateDebutAbonnement(aujourd_hui);
            abonnement.setDateFinAbonnement(nouvelleDateFin);
            abonnement.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            abonnement.setNombreDeMois(BigInteger.valueOf(ajoutMois));
        }


        if (nouveauxPrix != null) {
            abonnement.setPrixAbonnement(BigDecimal.valueOf(nouveauxPrix));
        }

        abonnement.setStatut(calculStatutAbonnemnt(abonnement));
        abonnementRepository.save(abonnement);

        // Envoi d’une facture suite au renouvellement
        abonnementEventService.envoyerFactureParEmail(abonnement.getMembre(),abonnement, "Renouvellement");
        return abonnement;
    }



    // 4. Mise à jour du statut automatiquement
    public Abonnement mettreAJourStatutAutomatiquement(Long id){
            Abonnement abonnement = abonnementRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("Abonnement introuvable."));
            abonnement.setStatut(calculStatutAbonnemnt(abonnement));
            return abonnementRepository.save(abonnement);
    }

    public StatutAbonnement calculStatutAbonnemnt(Abonnement abonnement) {
        LocalDate aujourd_hui = LocalDate.now();

        // 1. Si l'abonnement a ete resilier manuellement
        //  2. Si l'abonnement a ete mis en pause
        if (abonnement.getStatut() == StatutAbonnement.RESILIE ||
            abonnement.getStatut() == StatutAbonnement.EN_PAUSE){
            return abonnement.getStatut();
        }

        // 3. Actif (entre date_debut et date_rappel)
        if (abonnement.getDateDebutAbonnement() != null && abonnement.getDateRappelFinAbonnement() != null &&
                (aujourd_hui.equals(abonnement.getDateDebutAbonnement()) || aujourd_hui.isAfter(abonnement.getDateDebutAbonnement()))
        && aujourd_hui.isBefore(abonnement.getDateRappelFinAbonnement())){
            return StatutAbonnement.EN_COURS;
        }

        // 4. Bientôt expiré (exactement à date_rappel_fin_abonnement)
        if (abonnement.getDateRappelFinAbonnement() != null && aujourd_hui.equals(abonnement.getDateFinAbonnement())){
            return StatutAbonnement.BIENTOT_EXPIRE;
        }

        // 5. Si l'abonnement est expiré (date actuelle > date de fin)
        if (abonnement.getDateFinAbonnement() != null && aujourd_hui.isAfter(abonnement.getDateFinAbonnement())){
            return StatutAbonnement.EXPIRE;
        }

        // 6. Sinon, considéré comme Résilié
        return StatutAbonnement.EN_COURS;
    }

    // 5. Changement de plan d’abonnement avec calcul du reste
    public ResponseEntity<String> gererChangementAbonnement(Long idAbonnement, BigDecimal nouveauAbonnement, LocalDate dateChangement){

            Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                    .orElseThrow(()-> new RuntimeException("Abonnement introuvable."));

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
    public List<Abonnement> getAllAbonnement(){
            return abonnementRepository.findAll();
    }

    //  7.  L'historique des abonnement d'un membre
    public List<Abonnement> getHistoriqueAbonnementParMembre(Long membreId){
        User membre = userRepository.findById(membreId)
                .orElseThrow(()-> new RuntimeException("Membre introuvable."));
        return abonnementRepository.findByMembreOrderByDateDebutAbonnementDesc(membre);
    }
}
