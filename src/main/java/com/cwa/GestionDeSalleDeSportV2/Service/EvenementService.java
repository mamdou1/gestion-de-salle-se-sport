package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.EvenementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.EvenementViewDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Evenement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Repository.EvenementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvenementService {

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

    //  2.  Crée un événement
    public EvenementViewDTO createEvenement(EvenementDTO dto) throws MessagingException {
        checkStaffAccess();
        User createdBy = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new RuntimeException("Créateur introuvable"));
        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        if (dto.getEndDate() != null && !dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        Evenement evenement = new Evenement();
        evenement.setGym(gym);
        evenement.setTitle(dto.getTitle());
        evenement.setDescription(dto.getDescription());
        evenement.setEventType(dto.getEventType());
        evenement.setStartDate(dto.getStartDate());
        evenement.setEndDate(dto.getEndDate());
        evenement.setCreatedBy(createdBy);

        Evenement saved = evenementRepository.save(evenement);

        notificationService.notification(
                null, // Tous les membres de la gym
                "Nouvel événement",
                "Un nouvel événement a été créé : " + saved.getTitle(),
                "Événement",
                TypeNotification.EVENEMENT,
                false
        );

        return toViewDTO(saved);
    }

    //  3.  Met à jour un événement
    public EvenementViewDTO mettreAJourEvenement(Long id, EvenementDTO dto) throws MessagingException {
        checkStaffAccess();
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        User createdBy = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new RuntimeException("Créateur introuvable"));
        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        if (dto.getEndDate() != null && !dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        evenement.setGym(gym);
        evenement.setTitle(dto.getTitle());
        evenement.setDescription(dto.getDescription());
        evenement.setEventType(dto.getEventType());
        evenement.setStartDate(dto.getStartDate());
        evenement.setEndDate(dto.getEndDate());
        evenement.setCreatedBy(createdBy);

        Evenement updated = evenementRepository.save(evenement);

        notificationService.notification(
                null, // Tous les membres de la gym
                "Événement mis à jour",
                "L'événement a été mis à jour : " + updated.getTitle(),
                "Événement",
                TypeNotification.EVENEMENT,
                false
        );

        return toViewDTO(updated);
    }

    //  4.   Supprime un événement
    public void deleteEvenement(Long id) throws MessagingException {
        checkStaffAccess();
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        evenementRepository.delete(evenement);

        notificationService.notification(
                null,
                "Événement supprimé",
                "L'événement a été supprimé : " + evenement.getTitle(),
                "Événement",
                TypeNotification.EVENEMENT,
                false
        );
    }

    //  5.  Récupère un événement
    public EvenementViewDTO getByIdEvenement(Long id) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        return toViewDTO(evenement);
    }

    //  6.  Cette méthode récupère une liste d'événements (Evenement) associés à une salle de sport spécifique
    //      (gymId) et se déroulant dans une plage de dates donnée (start et end).
    //      Elle est conçue pour alimenter un calendrier (ex. : FullCalendar) dans le frontend,
    //      en fournissant les événements pertinents pour une période et une salle données.

    public List<EvenementViewDTO> getEvenementsByGymAndDateRange(Long gymId, LocalDateTime start, LocalDateTime end) {
        List<Evenement> evenements = evenementRepository.findByGymIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(gymId, start, end);
        return evenements.stream().map(this::toViewDTO).collect(Collectors.toList());
    }

    //  7.  Convertie une entitee Evenement ent objet EvenementDTO
    private EvenementViewDTO toViewDTO(Evenement evenement) {
        EvenementViewDTO dto = new EvenementViewDTO();
        dto.setId(evenement.getId());
        dto.setGymId(evenement.getGym().getId());
        dto.setTitle(evenement.getTitle());
        dto.setDescription(evenement.getDescription());
        dto.setEventType(evenement.getEventType());
        dto.setStartDate(evenement.getStartDate());
        dto.setEndDate(evenement.getEndDate());
        dto.setCreatedByName(evenement.getCreatedBy().getNom() + " " + evenement.getCreatedBy().getPrenom());
        return dto;
    }
}