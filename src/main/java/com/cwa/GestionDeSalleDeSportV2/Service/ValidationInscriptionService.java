package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ValidationInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.DemandeIncriptionRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ValidationInscriptionService {

    private final DemandeIncriptionRepository demandeIncriptionRepository;
    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;
    private final AbonnementEventService abonnementEventService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public ValidationInscriptionService(DemandeIncriptionRepository demandeIncriptionRepository, UserRepository userRepository, AbonnementRepository abonnementRepository, PasswordEncoder passwordEncoder, AbonnementEventService abonnementEventService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.userRepository = userRepository;
        this.abonnementRepository = abonnementRepository;
        this.abonnementEventService = abonnementEventService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    public void validerInscription(ValidationInscriptionDTO dto) throws MessagingException {
        DemandeInscription demande = demandeIncriptionRepository.findById(dto.getDemandeId())
                .orElseThrow(()-> new RuntimeException("Demande non trouvé."));

        if (demande.isEstValidee()){
            throw new RuntimeException("Demande déjà valider.");
        }

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        // 1. Créer le User
        User user = new User();

        user.setNom(demande.getNom());
        user.setPrenom(demande.getPrenom());
        user.setAdresse(demande.getAdresse());
        user.setEmail(demande.getEmail());
        user.setTelephone(demande.getTelephone());
        user.setGenre(demande.getGenre());
        user.setRole(Role.MEMBRE);
        user.setGym(demande.getGym());
        user.setDate_de_naissance(demande.getDate_de_naissance());
        user.setDate_creation(LocalDateTime.now());
        user.setOnline(false);
        user.setPassword(demande.getPassword());
        user.setFraisInscription(dto.getFraisInscription());

        userRepository.save(user);

        // 2. Créer l’abonnement
        Abonnement abonnement = new Abonnement();

        abonnement.setMembre(user);
        abonnement.setGym(demande.getGym());
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnement.setType(dto.getTypeAbonnement());
        abonnement.setNombreDeMois(dto.getNombreDeMois());
        abonnement.setModeDePaiement(dto.getModeDePaiement());
        abonnement.setPrixAbonnement(dto.getPrixAbonnement());
        abonnement.setEnregistrerPar(currentUser);

        abonnement.setDateFinAbonnement(LocalDate.now().plusMonths(dto.getNombreDeMois().longValue()));
        abonnement.setDateRappelFinAbonnement(abonnement.getDateFinAbonnement().minusDays(5));

        abonnementRepository.save(abonnement);

        // 3. Marquer la demande comme validée
        demande.setEstValidee(true);
        demandeIncriptionRepository.save(demande);

        abonnementEventService.envoyerFactureParEmail(abonnement.getMembre(), abonnement, "Validation");
    }

}
