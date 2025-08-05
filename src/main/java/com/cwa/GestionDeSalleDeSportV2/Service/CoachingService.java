package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingViewDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Coaching;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Repository.CoachingRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachingService {

    private final CoachingRepository coachingRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public CoachingService(
            CoachingRepository coachingRepository,
            UserRepository userRepository,
            GymRepository gymRepository,
            UtilisateurActuellementConnecter utilisateurActuellementConnecter
    ) {
        this.coachingRepository = coachingRepository;
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    //  1.  Cette mothod contient l'utilisateur actuellement connecter et qui a l'autorisation requise
    private void checkStaffAccess() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;
        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
    }

//    private void validateCoach(Long coachId) {
//        User coach = userRepository.findById(coachId)
//                .orElseThrow(() -> new RuntimeException("Coach introuvable"));
//        if (coach.getRole() != Role.COACH && coach.getRole() != Role.RECEPTIONNISTE&& coach.getRole() != Role.GERANT) {
//            throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un coach ou manager");
//        }
//    }

    //  2.  Crée une session
    public CoachingViewDTO createCoaching(CoachingDTO dto) {
        checkStaffAccess();
       // validateCoach(dto.getCoachId());
        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        User coach = userRepository.findById(dto.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach introuvable"));
        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        if (dto.getEndDate() != null && !dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        Coaching coaching = new Coaching();
        coaching.setGym(gym);
        coaching.setClient(client);
        coaching.setCoach(coach);
        coaching.setStartDate(dto.getStartDate());
        coaching.setEndDate(dto.getEndDate());
        coaching.setCost(dto.getCost());
        coaching.setCourseName(dto.getCourseName());

        Coaching saved = coachingRepository.save(coaching);
        return toViewDTO(saved);
    }

    //  3.  Met à jour une session
    public CoachingViewDTO mettreAJourCoaching(Long id, CoachingDTO dto) {
        checkStaffAccess();
       // validateCoach(dto.getCoachId());
        Coaching coaching = coachingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de coaching introuvable"));
        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        User coach = userRepository.findById(dto.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach introuvable"));
        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        if (dto.getEndDate() != null && !dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        coaching.setGym(gym);
        coaching.setClient(client);
        coaching.setCoach(coach);
        coaching.setStartDate(dto.getStartDate());
        coaching.setEndDate(dto.getEndDate());
        coaching.setCost(dto.getCost());
        coaching.setCourseName(dto.getCourseName());

        Coaching updated = coachingRepository.save(coaching);
        return toViewDTO(updated);
    }

    //  4.  Supprime une session
    public void deleteCoaching(Long id) {
        checkStaffAccess();
        Coaching coaching = coachingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de coaching introuvable"));
        coachingRepository.delete(coaching);
    }

    //  5.  Récupère une session
    public CoachingViewDTO getByIdCoaching(Long id) {
        Coaching coaching = coachingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de coaching introuvable"));
        return toViewDTO(coaching);
    }

    //  6.  Cette méthode récupère une liste de coaching (Coaching) associés à une salle de sport spécifique
    //      (gymId) et se déroulant dans une plage de dates donnée (start et end).
    //      Elle est conçue pour alimenter un calendrier (ex. : FullCalendar) dans le frontend,
    //      en fournissant les coaching pertinents pour une période et une salle données.

    public List<CoachingViewDTO> getCoachingsByGymAndDateRange(Long gymId, LocalDateTime start, LocalDateTime end) {
        List<Coaching> coachings = coachingRepository.findByGymIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(gymId, start, end);
        return coachings.stream().map(this::toViewDTO).collect(Collectors.toList());
    }

    //  7.  Convertie une entitee Evenement ent objet EvenementDTO
    private CoachingViewDTO toViewDTO(Coaching coaching) {
        CoachingViewDTO dto = new CoachingViewDTO();
        dto.setId(coaching.getId());
        dto.setGymId(coaching.getGym().getId());
        dto.setClientId(coaching.getClient().getId());
        dto.setClientName(coaching.getClient().getNom() + " " + coaching.getClient().getPrenom());
        dto.setCoachId(coaching.getCoach().getId());
        dto.setCoachName(coaching.getCoach().getNom() + " " + coaching.getCoach().getPrenom());
        dto.setStartDate(coaching.getStartDate());
        dto.setEndDate(coaching.getEndDate());
        dto.setCost(coaching.getCost());
        dto.setCourseName(coaching.getCourseName());
        return dto;
    }
}