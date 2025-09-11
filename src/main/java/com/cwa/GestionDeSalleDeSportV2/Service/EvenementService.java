package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.EvenementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.EvenementViewDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.EvennenemtUpdateDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutEvent;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Evenement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Repository.EvenementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvenementService {

    private final Logger logger = LoggerFactory.getLogger(EvenementService.class);

    private final EvenementRepository evenementRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final NotificationService notificationService;

    public EvenementService(
            EvenementRepository evenementRepository,
            UserRepository userRepository,
            GymRepository gymRepository,
            UtilisateurActuellementConnecter utilisateurActuellementConnecter, NotificationService notificationService
    ) {
        this.evenementRepository = evenementRepository;
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
    }

    //  1.  Cette mothod contient l'utilisateur actuellement connecter et qui a l'autorisation requise
    private void checkStaffAccess() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;
        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
    }

    // 2. Vérifie l'accès à une salle spécifique pour une action donnée
    private void verificationAccesGym(User staff, Gym gym, String action){
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)){
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }

    //  3.  Crée un événement
    public EvenementViewDTO createEvenement(EvenementDTO dto) throws MessagingException {
        checkStaffAccess();
        User curentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User createdBy = userRepository.findById(curentUser.getId())
                .orElseThrow(() -> new RuntimeException("Créateur introuvable"));
        Gym gym = gymRepository.findById(curentUser.getGym().getId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        verificationAccesGym(curentUser, gym, "créer un événement dans ");

        if (dto.getDateFin() != null && !dto.getDateDebut().isBefore(dto.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        LocalDateTime aujourd_hui = LocalDateTime.now();

        Evenement evenement = new Evenement();
        evenement.setGym(gym);
        evenement.setNom(dto.getNom());
        evenement.setDescription(dto.getDescription());
        if ((dto.getDateDebut() != null) && (dto.getDateFin() != null)) {
            if (dto.getDateDebut().isAfter(aujourd_hui)){
            evenement.setStatutEvent(StatutEvent.EN_ATTENTE);
            } else if ((dto.getDateFin().isBefore(aujourd_hui))) {
                evenement.setStatutEvent(StatutEvent.TERMINER);
            } else {
                evenement.setStatutEvent(StatutEvent.EN_COURS);
            }
        }
        evenement.setDateDebut(dto.getDateDebut());
        evenement.setDateFin(dto.getDateFin());
        evenement.setCreatedBy(createdBy);

        Evenement saved = evenementRepository.save(evenement);

        notificationService.notification(
                null, // Tous les membres de la gym
                "Nouvel événement",
                "Un nouvel événement a été créé : " + saved.getNom(),
                "Événement",
                TypeNotification.EVENEMENT,
                false
        );

        return toViewDTO(saved);
    }

    //  4.  Met à jour un événement
    public EvenementViewDTO mettreAJourEvenement(Long id, EvennenemtUpdateDTO dto) throws MessagingException {
        checkStaffAccess();
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        // Vérification d'accès à la gym (utilise la gym existante de l'événement)
        verificationAccesGym(currentUser, evenement.getGym(), "mettre à jour un événement dans ");

        // Mise à jour conditionnelle des champs
        if (dto.getNom() != null) {
            evenement.setNom(dto.getNom());
        }
        if (dto.getDescription() != null) {
            evenement.setDescription(dto.getDescription());
        }
        if (dto.getStatutEvent() != null) {
            evenement.setStatutEvent(dto.getStatutEvent());
        }
        if (dto.getDateDebut() != null) {
            evenement.setDateDebut(dto.getDateDebut());
        }
        if (dto.getDateFin() != null) {
            if (dto.getDateDebut() != null && !dto.getDateDebut().isBefore(dto.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            evenement.setDateFin(dto.getDateFin());
        }
        if (dto.getCreatedById() != null) {
            User createdBy = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Créateur introuvable"));
            evenement.setCreatedBy(createdBy);
        }

        Evenement updated = evenementRepository.save(evenement);

        notificationService.notification(
                null, // Tous les membres de la gym
                "Événement mis à jour",
                "L'événement a été mis à jour : " + updated.getNom(),
                "Événement",
                TypeNotification.EVENEMENT,
                false
        );

        return toViewDTO(updated);
    }

    //  5.   Supprime un événement
    public void deleteEvenement(Long id) throws MessagingException {
        checkStaffAccess();
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        evenementRepository.delete(evenement);

        // Vérification d'accès à la gym pour la mise à jour
        verificationAccesGym(currentUser, evenement.getGym(), "supprimer un événement dans");

        notificationService.notification(
                null,
                "Événement supprimé",
                "L'événement a été supprimé : " + evenement.getNom(),
                "Événement",
                TypeNotification.EVENEMENT,
                false
        );
    }

    //  6.  Récupère un événement
    public EvenementViewDTO getByIdEvenement(Long id) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        return toViewDTO(evenement);
    }

    //  7.  Cette méthode récupère une liste d'événements (Evenement) associés à une salle de sport spécifique
    //      (gymId) et se déroulant dans une plage de dates donnée (start et end).
    //      Elle est conçue pour alimenter un calendrier (ex. : FullCalendar) dans le frontend,
    //      en fournissant les événements pertinents pour une période et une salle données.

    public List<EvenementViewDTO> getEvenementsByGymAndDateRange() {
        checkStaffAccess();
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new RuntimeException("Aucune salle de sport associée à l'utilisateur.");
        }

        verificationAccesGym(currentUser, gym, "consulter les événements de ");

        //  Détermine la plage de dates du mois courant
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);

        List<Evenement> evenements = evenementRepository
                .findByGymIdAndDateDebutGreaterThanEqualAndDateFinLessThanEqual(gym.getId(), start, end);

        // 🔁 Transforme en DTO
        return evenements.stream().map(this::toViewDTO).collect(Collectors.toList());
    }

    public StatutEvent calculStatutEvent(Evenement evenement){
        LocalDateTime aujourd_hui = LocalDateTime.now();

        //  1.  Terminer (date de fin depacer)
        if (evenement.getDateFin() != null && aujourd_hui.isAfter(evenement.getDateFin())){
            return StatutEvent.TERMINER;
        }

        //  2.  En attente (avant date de debut)
        if (evenement.getDateDebut() != null && aujourd_hui.isBefore(evenement.getDateDebut())){
            return StatutEvent.EN_ATTENTE;
        }

        return StatutEvent.EN_COURS;

    }

    @Transactional
    @Scheduled(cron = "0 0 6 * * *") // Exécute tous les jours à 6h00 (optionnel)
    public void verifierStatutEvennement() throws MessagingException{
        logger.info("Démarrage de la vérification des statut des événement à {}", LocalDateTime.now());
        List<Evenement> evenements = evenementRepository.findAll();
        for (Evenement evenement : evenements ){
            StatutEvent nouveauStatut = calculStatutEvent(evenement);
            if (evenement.getStatutEvent() != nouveauStatut){
                logger.debug("Mis à jour du statut de l'événement {} de {} à {}", evenement.getId(), evenement.getStatutEvent(), nouveauStatut);
                evenement.setStatutEvent(nouveauStatut);
                evenementRepository.save(evenement);
                notificationService.notification(
                        null,
                        "Mise à jour de l'événement",
                        "Le statut de l'événement '" + evenement.getNom() +"' a changer à : " +nouveauStatut,
                        "Evénement",
                        TypeNotification.EVENEMENT,
                        false
                );
            }else {
                logger.debug("Aucun changement de statut pour l'événement {}", evenement.getId());
            }
        }
        logger.info("Fin de la vérification des statuts des évéenement.");
    }

    // Exécute la vérification au démarrage de l'application
    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void auDemarrageDeLApplication() throws MessagingException{
        logger.info("Vérification des statuts des événements au démarrage de l'application à {}", LocalDateTime.now());
        verifierStatutEvennement(); // Réutilisons la logique existante
    }

    //  8.  Convertie une entitee Evenement ent objet EvenementDTO
    private EvenementViewDTO toViewDTO(Evenement evenement) {
        EvenementViewDTO dto = new EvenementViewDTO();
        dto.setId(evenement.getId());
        dto.setGymId(evenement.getGym().getId());
        dto.setNom(evenement.getNom());
        dto.setDescription(evenement.getDescription());
        dto.setStatutEvent(evenement.getStatutEvent());
        dto.setDateDebut(evenement.getDateDebut());
        dto.setDateFin(evenement.getDateFin());
        dto.setCreatedByName(evenement.getCreatedBy().getNom() + " " + evenement.getCreatedBy().getPrenom());
        return dto;
    }
}