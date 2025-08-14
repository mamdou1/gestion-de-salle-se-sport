package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ValidationInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.DemandeIncriptionRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ValidationInscriptionService {

    private final DemandeIncriptionRepository demandeIncriptionRepository;
    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;
    private final AbonnementEventService abonnementEventService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final NotificationService notificationService;

    public ValidationInscriptionService(DemandeIncriptionRepository demandeIncriptionRepository, UserRepository userRepository, AbonnementRepository abonnementRepository, PasswordEncoder passwordEncoder, AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, NotificationService notificationService) {
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.userRepository = userRepository;
        this.abonnementRepository = abonnementRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
    }

    public void validerInscription(ValidationInscriptionDTO dto) throws MessagingException {
        System.out.println("DTO reçu : " + dto);
        DemandeInscription demande = demandeIncriptionRepository.findById(dto.getDemandeId())
                .orElseThrow(()-> new RuntimeException("Demande non trouvé."));

        if (demande.isEstValidee()){
            throw new RuntimeException("Demande déjà valider.");
        }

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User user;
        if (demande.getUser() != null){
            user = demande.getUser();
            // Ajouter le gym à la liste des gyms de l'utilisateur
            if (!user.getGyms().contains(demande.getGym())){
                user.addGym(demande.getGym());
                userRepository.save(user);
            }
            // verifier si un abonnement eixiste déjà
            List<Abonnement> abonnementsEnCours = abonnementRepository.findByMembreAndStatut(user, StatutAbonnement.EN_COURS);
            if (!abonnementsEnCours.isEmpty()){
                throw new RuntimeException("L'utilisateur a déjà un abonnement en cours.");
            }
        } else {
            // 1. Créer le User
            user = new User();

            user.setNom(demande.getNom());
            user.setPrenom(demande.getPrenom());
            user.setAdresse(demande.getAdresse());
            user.setEmail(demande.getEmail());
            user.setTelephone(demande.getTelephone());
            user.setGenre(demande.getGenre());
            user.setRole(Role.MEMBRE);
            user.setGym(demande.getGym()); // Gym principal
            user.addGym(demande.getGym()); // Ajouter à gyms
            user.setDate_de_naissance(demande.getDate_de_naissance());
            user.setDate_creation(LocalDateTime.now());
            user.setOnline(false);
            user.setPassword(demande.getPassword());
            user.setFraisInscription(dto.getFraisInscription());

            userRepository.save(user);
            demande.setUser(user);
        }

        // 2. Créer l’abonnement
        Abonnement abonnement = new Abonnement();

        abonnement.setMembre(user);
        abonnement.setGym(demande.getGym());
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setTypes(dto.getTypes());
        abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());
        abonnement.setNombreDeMois(dto.getNombreDeMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setPrixAbonnement(dto.getPrixAbonnement());
        abonnement.setEnregistrerPar(currentUser);

        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setDateFinAbonnement(LocalDate.now().plusMonths(dto.getNombreDeMois().longValue()));
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

}
