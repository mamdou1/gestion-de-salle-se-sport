package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ValidationInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.DemandeIncriptionRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.TypeDeServiceRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ValidationInscriptionService {

    private final DemandeIncriptionRepository demandeIncriptionRepository;
    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;
    private final AbonnementEventService abonnementEventService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final NotificationService notificationService;
    private final TypeDeServiceRepository typeDeServiceRepository;

    public ValidationInscriptionService(DemandeIncriptionRepository demandeIncriptionRepository, UserRepository userRepository, AbonnementRepository abonnementRepository, PasswordEncoder passwordEncoder, AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, NotificationService notificationService, TypeDeServiceRepository typeDeServiceRepository) {
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.userRepository = userRepository;
        this.abonnementRepository = abonnementRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
        this.typeDeServiceRepository = typeDeServiceRepository;
    }

    public void validerInscription(Long demandeId, ValidationInscriptionDTO dto) throws MessagingException {
        System.out.println("DTO reçu : " + dto);
        DemandeInscription demande = demandeIncriptionRepository.findById(demandeId)
                .orElseThrow(()-> new RuntimeException("Demande non trouvé."));

        if (demande.isEstValidee()){
            throw new RuntimeException("Demande déjà valider.");
        }

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User user = demande.getUser();
        user.setFraisInscription(demande.getTypeDeService().getFraisInscription());

        // Ajouter le gym à la liste des gyms de l'utilisateur s'il en a plusieurs
        if (demande.getUser() != null){
            user = demande.getUser();

            if (!user.getGyms().contains(demande.getGym())){
                user.addGym(demande.getGym());
                userRepository.save(user);
            }
            // verifier si un abonnement eixiste déjà
            List<Abonnement> abonnementsEnCours = abonnementRepository.findByMembreAndStatut(user, StatutAbonnement.EN_COURS);
            if (!abonnementsEnCours.isEmpty()){
                throw new RuntimeException("L'utilisateur a déjà un abonnement en cours.");
            }
        }

        // 2. Créer l’abonnement
        Abonnement abonnement = new Abonnement();

        abonnement.setMembre(user);
        abonnement.setGym(demande.getGym());
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setTypes(demande.getTypes());
        abonnement.setPeriodAbonnement(demande.getPeriodAbonnement());
        abonnement.setNombreDeMois(demande.getNombreDeMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());

        BigDecimal tarif = getTarif(demande.getUser().getGenre(), demande.getTypeDeService().getId());

        abonnement.setPrixAbonnement(tarif);
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setTypeDeService(demande.getTypeDeService());

        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setDateFinAbonnement(LocalDate.now().plusMonths(demande.getNombreDeMois().longValue()));
        abonnement.setDateRappelFinAbonnement(abonnement.getDateFinAbonnement().minusDays(5));

        abonnementRepository.save(abonnement);

        // 3. Marquer la demande comme validée
        demande.setEstValidee(true);
        demande.setStatut(StatutMembre.VALIDE);
        demandeIncriptionRepository.save(demande);

        abonnementEventService.envoyerFactureParEmail(abonnement.getMembre(), abonnement, "Validation");
    }

    public void rejeterInscription(Long demandeId, String raison) throws MessagingException{
        DemandeInscription demande = demandeIncriptionRepository.findById(demandeId)
                .orElseThrow(()->new RuntimeException("Demamde introuvable"));
        if (demande.getStatut() != StatutMembre.EN_ATTENTE_VALIDATION){
            throw new RuntimeException("Cette demande a déjà été traitée");
        }
        demande.setStatut(StatutMembre.REJETE);
        demande.setRaisonRejet(raison);
        demandeIncriptionRepository.save(demande);

        // Notification au user si existant
        if (demande.getUser() != null){
            notificationService.notification(
                    demande.getUser(),
                    "Demande rejétée",
                    "Votre demande d'inscription au gym " + demande.getGym().getNom() + "a été rejétée. Rason : " + raison,
                    "Inscription",
                    TypeNotification.INSCRIPTION,
                    true
                    );
        }
    }

    public BigDecimal getTarif(Genre genre, Long typeDeServiceId){
        TypeDeService tarif = typeDeServiceRepository.findById(typeDeServiceId)
                .orElseThrow();
        if (tarif.getTarifUnique() != null){
            return tarif.getTarifUnique();
        }

        if (genre == Genre.HOMME && tarif.getTarifHomme() != null){
            return tarif.getTarifHomme();
        } else if (genre == Genre.FEMME && tarif.getTarifFemme() != null) {
            return tarif.getTarifFemme();
        }
        throw new RuntimeException("Aucun tarif défini pour ce service et ce genre.");
    }

}
