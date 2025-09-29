package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.DemandeInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionEnLigneDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.DemandeIncriptionRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.TypeDeServiceRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class DemandeInscriptionService {

    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final DemandeIncriptionRepository demandeIncriptionRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TypeDeServiceRepository typeDeServiceRepository;

    public DemandeInscriptionService(UtilisateurActuellementConnecter utilisateurActuellementConnecter, DemandeIncriptionRepository demandeIncriptionRepository, GymRepository gymRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, NotificationService notificationService, TypeDeServiceRepository typeDeServiceRepository) {
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.gymRepository = gymRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.typeDeServiceRepository = typeDeServiceRepository;
    }

    public void soumettreDemande(Long gymId, DemandeInscriptionDTO dto){

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(()-> new RuntimeException("Gym non trouvé."));

        TypeDeService typeDeService = typeDeServiceRepository.findById(dto.getTypeDeService())
                .orElseThrow(()->new RuntimeException("Type de service non trouvé:"));
        User membre = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        DemandeInscription demande = new DemandeInscription();

        demande.setGym(gym);
        demande.setUser(membre);
        demande.setTypeDeService(typeDeService);
        demande.setPeriodAbonnement(dto.getPeriodAbonnement());
        demande.setNombreDeMois(dto.getNombreDeMois());

        demandeIncriptionRepository.save(demande);

        notificationService.notifyGymAndMember(
                gym,
                membre,
                "Demande de validation d' inscription",
                "Vous avez reçu une demande une nouvelle demande d'insdription au pres de votre salle de sport",
                "Validation",
                TypeNotification.VALIDATION_INSCRIPTION,
                false

        );
    }

    public List<DemandeInscription> getDemandeNonValide(){
        return demandeIncriptionRepository.findByEstValideeFalse();
    }

    public void inscriptionEnLigne(InscriptionEnLigneDTO dto, MultipartFile file) throws IOException {

        // Vérifier si un utilisateur existe déjà avec le même telephone ou email
        //Optional<User> existingUser = userRepository.findByTelephoneOrEmail(dto.getTelephone(), dto.getEmail());

        User newMembre = new User();

        //  1)
        newMembre.setNom(dto.getNom());
        newMembre.setPrenom(dto.getPrenom());
        newMembre.setAdresse(dto.getAdresse());
        newMembre.setEmail(dto.getEmail());

        //  2)
        newMembre.setGenre(dto.getGenre());
        newMembre.setDate_de_naissance(dto.getDate_de_naissance());
        newMembre.setStatut(StatutMembre.EN_ATTENTE_VALIDATION);
        newMembre.setDate_creation(LocalDateTime.now());
        newMembre.setRole(Role.MEMBRE);
        newMembre.setFraisInscriptionPayer(false);

        if (file != null && !file.isEmpty()){
            newMembre.setProfil(file.getBytes()); //  Conversion du MultipartFile en byte[]
        }

        //  3)
        newMembre.setTelephone(dto.getTelephone());
        newMembre.setPassword(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(newMembre);
        notificationService.notifyInscriptionEnLigne(
                newMembre,
                "Demande de validation de panier",
                "Vous avez reçu une demande une nouveau panier en attente de validation",
                "Validation",
                TypeNotification.INSCRIPTION

        );
    }

}

