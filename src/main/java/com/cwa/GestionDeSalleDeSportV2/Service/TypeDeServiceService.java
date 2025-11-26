package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.TypeDeServiceDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.TypeDeServiceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@Service
public class TypeDeServiceService {

    private final TypeDeServiceRepository typeDeServiceRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final GymRepository gymRepository;

    public TypeDeServiceService(TypeDeServiceRepository typeDeServiceRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, GymRepository gymRepository) {
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.gymRepository = gymRepository;
    }

    // 1. Créer un type de service
    @Transactional
    public TypeDeService createTypeDeService(TypeDeServiceDTO dto) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym gym = currentUser.getGym();
        if (gym == null) {
            throw new AccessDeniedException("Aucun gym associé au staff.");
        }

        // Validation : Au moins un tarif doit être défini
        if (dto.getTarifHomme() == null && dto.getTarifFemme() == null && dto.getTarifUnique() == null) {
            throw new IllegalArgumentException("Au moins un tarif doit être défini.");
        }

        TypeDeService service = new TypeDeService();
        service.setNom(dto.getNom());
        service.setTarifHomme(dto.getTarifHomme());
        service.setTarifFemme(dto.getTarifFemme());
        service.setTarifUnique(dto.getTarifUnique());
        service.setFraisInscription(dto.getFraisInscription());
        service.setGym(gym);

        return typeDeServiceRepository.save(service);
    }

    // 2. Mettre à jour un type de service
    @Transactional
    public TypeDeService updateTypeDeService(Long id, TypeDeServiceDTO dto) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        TypeDeService service = typeDeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable."));

        if (!service.getGym().equals(currentUser.getGym())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce service.");
        }

        if (dto.getNom() != null) service.setNom(dto.getNom());
        if (dto.getTarifHomme() != null) service.setTarifHomme(dto.getTarifHomme());
        if (dto.getTarifFemme() != null) service.setTarifFemme(dto.getTarifFemme());
        if (dto.getTarifUnique() != null) service.setTarifUnique(dto.getTarifUnique());
        if (dto.getFraisInscription() != null) service.setFraisInscription(dto.getFraisInscription());

        // Validation : Au moins un tarif doit être défini
        if (service.getTarifHomme() == null && service.getTarifFemme() == null && service.getTarifUnique() == null) {
            throw new IllegalArgumentException("Au moins un tarif doit être défini.");
        }

        return typeDeServiceRepository.save(service);
    }

    // 3. Supprimer un type de service
    @Transactional
    public void deleteTypeDeService(Long id) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        TypeDeService service = typeDeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable."));

        if (!service.getGym().equals(currentUser.getGym())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer ce service.");
        }

        typeDeServiceRepository.delete(service);
    }

    // 4. Récupérer un type de service par ID
    public TypeDeService getTypeDeServiceById(Long id) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        TypeDeService service = typeDeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable."));

        if (!service.getGym().equals(currentUser.getGym())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à accéder à ce service.");
        }

        return service;
    }

    // 5. Consulter tous les types de services pour un gym
    public List<TypeDeService> getAllTypeDeService() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym gym = currentUser.getGym();
        return typeDeServiceRepository.findByGym(gym);
    }

    // 6. Consulter tous les types de services pour une application mobile (par ID de gym)
    public List<TypeDeService> getAllTypeDeServiceApp(Long gymId) throws AccessDeniedException {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new RuntimeException("Gym non trouvé."));
        return typeDeServiceRepository.findByGym(gym);
    }

    // 7. Récupérer le prix d'un type de service en fonction du genre (pour intégration avec /api/abonnements/prix)
    public Double getPrixByTypeDeServiceAndGenre(Long typeDeServiceId, String genre) throws AccessDeniedException {
        initializeAccess(true);
        TypeDeService typeDeService = typeDeServiceRepository.findById(typeDeServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Type de service non trouvé avec l'ID: " + typeDeServiceId));

        BigDecimal prix;
        if ("HOMME".equalsIgnoreCase(genre)) {
            prix = typeDeService.getTarifHomme();
        } else if ("FEMME".equalsIgnoreCase(genre)) {
            prix = typeDeService.getTarifFemme();
        } else if (typeDeService.getTarifUnique() != null) {
            prix = typeDeService.getTarifUnique(); // Utiliser tarif unique si défini
        } else {
            throw new IllegalArgumentException("Genre invalide ou aucun tarif unique défini.");
        }

        return prix != null ? prix.doubleValue() : null;
    }

    private boolean isStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && !isStaff(currentUser)) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }
}