package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingUpdateDTO;
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

    //  2.  vérification de droit d'accès à la gym
    private void verificationAccesGym(User staff, Gym gym, String action){
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)){
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " +action+ " cette salle de gym");
        }
    }

    //  3.  Crée une session
    public CoachingViewDTO createCoaching(CoachingDTO dto) {
        checkStaffAccess();
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        // validateCoach(dto.getCoachId());
        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        User coach = userRepository.findById(dto.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach introuvable"));
        Gym gym = gymRepository.findById(currentUser.getGym().getId())
                .orElseThrow(() -> new RuntimeException("Salle de sport introuvable"));

        verificationAccesGym(currentUser, gym, "crée une session dans");

        if (dto.getDateFin() != null && !dto.getDateDebut().isBefore(dto.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        if (!(coach.getRole() == Role.COACH)){
            throw new RuntimeException("Seul les coachs peuvent dispenser une session");
        }

        Coaching coaching = new Coaching();
        coaching.setGym(gym);
        coaching.setClient(client);
        coaching.setCoach(coach);
        coaching.setDateDebut(dto.getDateDebut());
        coaching.setDateFin(dto.getDateFin());
        coaching.setPrix(dto.getPrix());
        coaching.setNomCours(dto.getNomCours());
        coaching.setDescription(dto.getDescription());

        Coaching saved = coachingRepository.save(coaching);
        return toViewDTO(saved);
    }

    //  4.  Met à jour une session
    public CoachingViewDTO mettreAJourCoaching(Long id, CoachingUpdateDTO dto) {
        checkStaffAccess();
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Coaching coaching = coachingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de coaching introuvable"));

        // Vérification d'accès à la gym (utilise la gym existante de la session)
        verificationAccesGym(currentUser, coaching.getGym(), "mettre à jour une session dans ");

        // Mise à jour conditionnelle des champs
        if (dto.getClientId() != null) {
            User client = userRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable"));
            coaching.setClient(client);
        }
        if (dto.getCoachId() != null) {
            User coach = userRepository.findById(dto.getCoachId())
                    .orElseThrow(() -> new RuntimeException("Coach introuvable"));
            coaching.setCoach(coach);
        }

        if (dto.getDateDebut() != null) {
            coaching.setDateDebut(dto.getDateDebut());
        }
        if (dto.getDateFin() != null) {
            if (dto.getDateDebut() != null && !dto.getDateDebut().isBefore(dto.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            coaching.setDateFin(dto.getDateFin());
        }
        if (dto.getPrix() != null) {
            coaching.setPrix(dto.getPrix());
        }
        if (dto.getNomCours() != null) {
            coaching.setNomCours(dto.getNomCours());
        }
        if (dto.getDescription() != null) {
            coaching.setDescription(dto.getDescription());
        }

        Coaching updated = coachingRepository.save(coaching);
        return toViewDTO(updated);
    }

    //  5.  Supprime une session
    public void deleteCoaching(Long id) {
        checkStaffAccess();
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        Coaching coaching = coachingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de coaching introuvable"));

        verificationAccesGym(currentUser, coaching.getGym(), "supprimer une session dans");

        coachingRepository.delete(coaching);
    }

    //  6.  Récupère une session
    public CoachingViewDTO getByIdCoaching(Long id) {
        Coaching coaching = coachingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de coaching introuvable"));

        return toViewDTO(coaching);
    }

    //  7.  Cette méthode récupère une liste de coaching (Coaching) associés à une salle de sport spécifique
    //      (gymId) et se déroulant dans une plage de dates donnée (start et end).
    //      Elle est conçue pour alimenter un calendrier (ex. : FullCalendar) dans le frontend,
    //      en fournissant les coaching pertinents pour une période et une salle données.

    public List<CoachingViewDTO> getCoachingsByGymAndDateRange(Long gymId, LocalDateTime start, LocalDateTime end) {
        List<Coaching> coachings = coachingRepository.findByGymIdAndDateDebutGreaterThanEqualAndDateFinLessThanEqual(gymId, start, end);
        return coachings.stream().map(this::toViewDTO).collect(Collectors.toList());
    }

    //  8.  Convertie une entitee Evenement ent objet EvenementDTO
    private CoachingViewDTO toViewDTO(Coaching coaching) {
        CoachingViewDTO dto = new CoachingViewDTO();
        dto.setId(coaching.getId());
        dto.setGymId(coaching.getGym().getId());
        dto.setClientId(coaching.getClient().getId());
        dto.setNomClient(coaching.getClient().getNom() + " " + coaching.getClient().getPrenom());
        dto.setCoachId(coaching.getCoach().getId());
        dto.setNomCoach(coaching.getCoach().getNom() + " " + coaching.getCoach().getPrenom());
        dto.setDateDebut(coaching.getDateDebut());
        dto.setDateFin(coaching.getDateFin());
        dto.setPrix(coaching.getPrix());
        dto.setNomCours(coaching.getNomCours());
        dto.setDescription(coaching.getDescription());
        return dto;
    }
}