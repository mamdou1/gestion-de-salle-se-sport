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
import org.springframework.transaction.annotation.Transactional;
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
    private final StockageDeFichierService stockageDeFichierService;

    public DemandeInscriptionService(
            UtilisateurActuellementConnecter utilisateurActuellementConnecter,
            DemandeIncriptionRepository demandeIncriptionRepository,
            GymRepository gymRepository,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            NotificationService notificationService,
            TypeDeServiceRepository typeDeServiceRepository,
            StockageDeFichierService stockageDeFichierService) {
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.gymRepository = gymRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.stockageDeFichierService = stockageDeFichierService;
    }

    @Transactional
    public void soumettreDemande(Long gymId, DemandeInscriptionDTO dto) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new RuntimeException("Gym non trouvé."));

        TypeDeService typeDeService = typeDeServiceRepository.findById(dto.getTypeDeService())
                .orElseThrow(() -> new RuntimeException("Type de service non trouvé:"));

        User membre = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        DemandeInscription demande = new DemandeInscription();
        demande.setGym(gym);
        demande.setUser(membre);
        demande.setTypeDeService(typeDeService);
        demande.setPeriodAbonnement(dto.getPeriodAbonnement());
        demande.setNombreDeMois(dto.getNombreDeMois());

        demandeIncriptionRepository.save(demande);

        notificationService.notifyInscriptionEnLigne(
                membre,
                "Demande de validation d'inscription",
                "Votre demande d'inscription auprès de la salle de sport " + gym.getNom() + " a été envoyée avec succès.",
                "Envoyer",
                TypeNotification.VALIDATION_INSCRIPTION
        );

        notificationService.notifyInscriptionEnLigneGym(
                gym,
                "Demande de validation d'inscription",
                "Vous avez reçu une nouvelle demande d'inscription auprès de votre salle de sport",
                "Validation",
                TypeNotification.VALIDATION_INSCRIPTION
        );
    }

    public List<DemandeInscription> getDemandeNonValide() {
        return demandeIncriptionRepository.findByEstValideeFalse();
    }

    // 🔥 NOUVELLE MÉTHODE : Valider une demande d'inscription
    @Transactional
    public void validerDemandeInscription(Long id) {
        DemandeInscription demande = demandeIncriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription non trouvée avec l'ID: " + id));

        // Marquer la demande comme validée
        demande.setEstValidee(true);
        demande.setDateValidation(LocalDateTime.now());
        demandeIncriptionRepository.save(demande);

        // Mettre à jour le statut de l'utilisateur
        User user = demande.getUser();
        user.setGym(demande.getGym());
        userRepository.save(user);

        // Envoyer une notification de confirmation à l'utilisateur
        notificationService.notifyInscriptionEnLigne(
                user,
                "Inscription validée",
                "Félicitations ! Votre inscription à la salle de sport " + demande.getGym().getNom() + " a été validée avec succès.",
                "Validation réussie",
                TypeNotification.VALIDATION_INSCRIPTION

        );

        // Envoyer une notification au gym pour confirmer la validation
        notificationService.notifyInscriptionEnLigneGym(
                demande.getGym(),
                "Inscription validée",
                "L'inscription de " + user.getPrenom() + " " + user.getNom() + " a été validée avec succès.",
                "Validation terminée",
                TypeNotification.VALIDATION_INSCRIPTION
        );
    }

    // 🔥 NOUVELLE MÉTHODE : Rejeter une demande d'inscription
    @Transactional
    public void rejeterDemandeInscription(Long id) {
        DemandeInscription demande = demandeIncriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription non trouvée avec l'ID: " + id));

        // Marquer la demande comme rejetée
        demande.setEstValidee(false);
        demande.setDateValidation(LocalDateTime.now());
        demandeIncriptionRepository.save(demande);

        User user = demande.getUser();
        userRepository.save(user);

        // Envoyer une notification de rejet à l'utilisateur
        notificationService.notifyInscriptionEnLigne(
                user,
                "Inscription rejetée",
                "Votre inscription à la salle de sport " + demande.getGym().getNom() + " a été rejetée. Veuillez contacter le gym pour plus d'informations.",
                "Rejet",
                TypeNotification.INFO
        );

        // Envoyer une notification au gym pour confirmer le rejet
        notificationService.notifyInscriptionEnLigneGym(
                demande.getGym(),
                "Inscription rejetée",
                "L'inscription de " + user.getPrenom() + " " + user.getNom() + " a été rejetée.",
                "Rejet terminé",
                TypeNotification.INFO
        );
    }

    // 🔥 NOUVELLE MÉTHODE : Obtenir une demande par son ID
    public DemandeInscription getDemandeById(Long id) {
        return demandeIncriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription non trouvée avec l'ID: " + id));
    }

    // 🔥 NOUVELLE MÉTHODE : Obtenir toutes les demandes d'un gym
    public List<DemandeInscription> getDemandesByGym(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new RuntimeException("Gym non trouvé avec l'ID: " + gymId));
        return demandeIncriptionRepository.findByGym(gym);
    }

    // 🔥 NOUVELLE MÉTHODE : Obtenir les demandes en attente d'un gym
    public List<DemandeInscription> getDemandesEnAttenteByGym(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new RuntimeException("Gym non trouvé avec l'ID: " + gymId));
        return demandeIncriptionRepository.findByGymAndEstValideeFalse(gym);
    }

    @Transactional
    public void inscriptionEnLigne(InscriptionEnLigneDTO dto, MultipartFile file) throws IOException {
        User newMembre = new User();

        // 1) Set basic user details
        newMembre.setNom(dto.getNom());
        newMembre.setPrenom(dto.getPrenom());
        newMembre.setAdresse(dto.getAdresse());
        newMembre.setEmail(dto.getEmail());

        // 2) Set additional user details
        newMembre.setGenre(dto.getGenre());
        newMembre.setDate_de_naissance(dto.getDate_de_naissance());
        newMembre.setStatut(StatutMembre.EN_ATTENTE_VALIDATION);
        newMembre.setDate_creation(LocalDateTime.now());
        newMembre.setRole(Role.MEMBRE);
        newMembre.setFraisInscriptionPayer(false);

        // 3) Set telephone and password
        newMembre.setTelephone(dto.getTelephone());
        newMembre.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Save user to generate ID
        User savedMembre = userRepository.save(newMembre);

        // Handle photo upload
        if (file != null && !file.isEmpty()) {
            String fileName = stockageDeFichierService.store(file, "membres/" + savedMembre.getId());
            savedMembre.setImageUrl("/uploads/membres/" + savedMembre.getId() + "/" + fileName);
            userRepository.save(savedMembre);
        }

        notificationService.notifyInscriptionEnLigne(
                savedMembre,
                "Inscription en ligne",
                "Votre inscription en ligne a été enregistrée avec succès. En attente de validation.",
                "Inscription",
                TypeNotification.INSCRIPTION
        );
    }
}