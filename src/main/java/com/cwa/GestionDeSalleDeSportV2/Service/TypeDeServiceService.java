package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.TypeDeServiceDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Repository.TypeDeServiceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TypeDeServiceService {

    private final TypeDeServiceRepository typeDeServiceRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public TypeDeServiceService(TypeDeServiceRepository typeDeServiceRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    //  1.  Créer les type de service
    @Transactional
    public TypeDeService createTypeDeService(TypeDeServiceDTO dto) {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!isStaff(currentUser)) {
            throw new AccessDeniedException("Seul le staff peut créer un service.");
        }

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

    //  2.  Mettre à jour un type de service
    @Transactional
    public TypeDeService updateTypeDeService(Long id, TypeDeServiceDTO dto) {
        TypeDeService service = typeDeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable."));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!isStaff(currentUser)) {
            throw new AccessDeniedException("Seul le staff peut modifier un service.");
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

    //  3.  Supprimer un type de service
    public void deleteTypeDeService(Long id) {
        TypeDeService service = typeDeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable."));
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!isStaff(currentUser)) {
            throw new AccessDeniedException("Seul le staff peut supprimer un service.");
        }
        typeDeServiceRepository.delete(service);
    }

    //  4.  getById un type de service
    public TypeDeService getTypeDeServiceById(Long id) {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!isStaff(currentUser)) {
            throw new AccessDeniedException("Seul le staff peut consulter un service.");
        }
        return typeDeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable."));
    }

    //   5. Conslter tout les types de services
    public List<TypeDeService> getAllTypeDeService() {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!isStaff(currentUser)) {
            throw new AccessDeniedException("Seul le staff peut lister les services.");
        }
        Gym gym = currentUser.getGym();
        return typeDeServiceRepository.findByGym(gym); // Filtrer par gym du staff
    }

    private boolean isStaff(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.RECEPTIONNISTE || user.getRole() == Role.GERANT;
    }

}