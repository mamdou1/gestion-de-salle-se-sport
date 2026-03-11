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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
            UtilisateurActuellementConnecter utilisateurActuellementConnecter,
            NotificationService notificationService
    ) {
        this.evenementRepository = evenementRepository;
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.notificationService = notificationService;
    }

    public long nombreTotalEvennement() throws java.nio.file.AccessDeniedException {
        initializeAccess(true);
        return evenementRepository.countByEvennement();
    }

    private void checkStaffAccess() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN ||
                currentUser.getRole() == Role.RECEPTIONNISTE ||
                currentUser.getRole() == Role.GERANT;

        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
    }

    private void verificationAccesGym(User staff, Gym gym, String action){
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)){
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }

    // 3. Créer un événement
    public EvenementViewDTO createEvenement(EvenementDTO dto) throws MessagingException {
        checkStaffAccess();

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        User createdBy = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Créateur introuvable"));

        Gym gym = gymRepository.findById(currentUser.getGym().getId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        verificationAccesGym(currentUser, gym, "créer un événement dans ");

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

        // 🔔 Notification au gym
        notificationService.notificationAdminAGym(
                gym,
                "Nouvel événement",
                "Un nouvel événement a été créé : " + saved.getNom(),
                "EVENEMENT",
                TypeNotification.EVENEMENT,
                false
        );

        // 🔔 Notification à tous les membres
        List<User> membres = userRepository.findByGymId(gym.getId());

        for (User membre : membres) {
            notificationService.notification(
                    membre,
                    "Nouvel événement",
                    "Un nouvel événement a été créé : " + saved.getNom(),
                    "EVENEMENT",
                    TypeNotification.EVENEMENT,
                    false
            );
        }

        return toViewDTO(saved);
    }

    // 4. Met à jour un événement
    public EvenementViewDTO mettreAJourEvenement(Long id, EvennenemtUpdateDTO dto) throws MessagingException {

        checkStaffAccess();

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        verificationAccesGym(currentUser, evenement.getGym(), "mettre à jour un événement dans ");

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
            evenement.setDateFin(dto.getDateFin());
        }

        if (dto.getCreatedById() != null) {
            User createdBy = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Créateur introuvable"));
            evenement.setCreatedBy(createdBy);
        }

        Evenement updated = evenementRepository.save(evenement);

        notificationService.notificationAdminAGym(
                evenement.getGym(),
                "Événement mis à jour",
                "L'événement a été mis à jour : " + updated.getNom(),
                "EVENEMENT",
                TypeNotification.EVENEMENT,
                false
        );

        List<User> membres = userRepository.findByGymId(evenement.getGym().getId());

        for (User membre : membres) {
            notificationService.notification(
                    membre,
                    "Événement mis à jour",
                    "L'événement a été mis à jour : " + updated.getNom(),
                    "EVENEMENT",
                    TypeNotification.EVENEMENT,
                    false
            );
        }

        return toViewDTO(updated);
    }

    // 5. Supprimer un événement
    public void deleteEvenement(Long id) throws MessagingException {

        checkStaffAccess();

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        verificationAccesGym(currentUser, evenement.getGym(), "supprimer un événement dans");

        evenementRepository.delete(evenement);

        notificationService.notificationAdminAGym(
                evenement.getGym(),
                "Événement supprimé",
                "L'événement a été supprimé : " + evenement.getNom(),
                "EVENEMENT",
                TypeNotification.EVENEMENT,
                false
        );

        List<User> membres = userRepository.findByGymId(evenement.getGym().getId());

        for (User membre : membres) {
            notificationService.notification(
                    membre,
                    "Événement supprimé",
                    "L'événement a été supprimé : " + evenement.getNom(),
                    "EVENEMENT",
                    TypeNotification.EVENEMENT,
                    false
            );
        }
    }

    public EvenementViewDTO getByIdEvenement(Long id) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        return toViewDTO(evenement);
    }

    public List<EvenementViewDTO> getEvenementsByGymAndDateRange() {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        Gym gym = currentUser.getGym();

        if (gym == null) {
            throw new RuntimeException("Aucune salle de sport associée à l'utilisateur.");
        }

        verificationAccesGym(currentUser, gym, "consulter les événements de ");

        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);

        List<Evenement> evenements = evenementRepository
                .findByGymIdAndDateDebutGreaterThanEqualAndDateFinLessThanEqual(gym.getId(), start, end);

        return evenements.stream().map(this::toViewDTO).collect(Collectors.toList());
    }

    private EvenementViewDTO toViewDTO(Evenement evenement) {

        EvenementViewDTO dto = new EvenementViewDTO();

        dto.setId(evenement.getId());
        dto.setGymId(evenement.getGym().getId());
        dto.setNom(evenement.getNom());
        dto.setDescription(evenement.getDescription());
        dto.setStatutEvent(evenement.getStatutEvent());
        dto.setDateDebut(evenement.getDateDebut());
        dto.setDateFin(evenement.getDateFin());

        if (evenement.getCreatedBy() != null) {
            dto.setCreatedByName(
                    evenement.getCreatedBy().getNom() + " " +
                            evenement.getCreatedBy().getPrenom()
            );
        } else {
            dto.setCreatedByName("Inconnu");
        }

        return dto;
    }

    private User initializeAccess(boolean requireStaff) throws java.nio.file.AccessDeniedException {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        if (requireStaff &&
                currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.RECEPTIONNISTE &&
                currentUser.getRole() != Role.GERANT) {

            throw new java.nio.file.AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }

        if (currentUser.getGym() == null && requireStaff) {
            throw new java.nio.file.AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        return currentUser;
    }
}