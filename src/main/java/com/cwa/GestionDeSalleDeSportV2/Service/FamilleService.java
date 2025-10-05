package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // toutes les méthodes de cette classe s'exécutent dans une transaction par défaut
public class FamilleService {

    private static final Logger logger = LoggerFactory.getLogger(FamilleService.class);

    @Autowired
    private FamilleRepository familleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    // Méthode pour vérifier les permissions (ADMIN ou RECEPTIONNISTE uniquement)
    private boolean estMembreDuStaff(Object principal) {
        logger.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "NULL");
        if (principal instanceof User) {
            Role role = ((User) principal).getRole();
            logger.info("Rôle de l'utilisateur (via User): {}", role != null ? role.name() : "NULL");
            return role == Role.ADMIN || role == Role.RECEPTIONNISTE;
        } else {
            // Fallback pour UserDetails / authorities
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                boolean hasAdmin = authorities.stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
                boolean hasRecep = authorities.stream().anyMatch(a -> a.getAuthority().contains("RECEPTIONNISTE"));
                logger.info("Authorities: {}, Has ADMIN: {}, Has RECEPTIONNISTE: {}",
                        authorities, hasAdmin, hasRecep);
                return hasAdmin || hasRecep;
            }
            logger.warn("Aucune authority trouvée");
            return false;
        }
    }

    // 1. Créer une famille
    public void creerFamille(FamilleDTO dto) {
        logger.info("=== [Création de famille] Début ===");
        logger.info("Données reçues: {}", dto);

        // 🔐 Vérification des permissions
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            logger.warn("Accès refusé - utilisateur non autorisé à créer une famille");
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut créer une famille.");
        }

        // ✅ Validation du chef
        if (dto.getChefFamilleId() == null) {
            throw new IllegalArgumentException("Le chef de famille est requis pour créer une famille.");
        }

        // 🔍 Récupération du chef
        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(() -> new EntityNotFoundException("Chef de famille non trouvé avec l'ID : " + dto.getChefFamilleId()));

        // ✅ Détermination automatique du gym
        Gym gym = null;

        // 1️⃣ Si gymId fourni → utiliser celui-là
        if (dto.getGymId() != null) {
            gym = gymRepository.findById(dto.getGymId())
                    .orElseThrow(() -> new EntityNotFoundException("Gym non trouvé avec l'ID : " + dto.getGymId()));
            logger.info("Gym trouvé via DTO : {}", gym.getNom());

            // 2️⃣ Sinon → tenter d'utiliser le gym du chef de famille
        } else if (chefFamille.getGym() != null) {
            gym = chefFamille.getGym();
            logger.info("Gym déduit automatiquement du chef : {}", gym.getNom());

            // 3️⃣ Sinon → gym de l'utilisateur connecté (staff)
        } else if (principal instanceof User staff && staff.getGym() != null) {
            gym = staff.getGym();
            logger.info("Gym assigné automatiquement à partir du staff connecté : {}", gym.getNom());
        }

        // 4️⃣ Sinon → erreur
        if (gym == null) {
            logger.error("Aucun gym détecté : impossible de créer une famille sans gym");
            throw new IllegalArgumentException("Impossible de déterminer le gym : aucun gymId fourni, et ni le chef ni le staff n'ont de gym associé.");
        }

        // 🧱 Création de la famille
        Famille famille = new Famille();
        famille.setNom(dto.getNom());
        famille.setChefFamille(chefFamille);
        famille.setGym(gym);

        // 👥 Gestion des membres
        List<Long> membresIds = (dto.getMembresId() != null) ? dto.getMembresId() : Collections.emptyList();
        List<User> membres = userRepository.findAllById(membresIds);

        if (!membres.isEmpty() && membres.contains(chefFamille)) {
            throw new IllegalArgumentException("Le chef de famille ne peut pas être inclus dans la liste des membres.");
        }
        famille.setMembres(membres);

        // 💾 Sauvegarde
        familleRepository.save(famille);
        logger.info("Famille créée avec succès - ID: {}, Nom: '{}', Gym: {}", famille.getId(), famille.getNom(), gym.getNom());
        logger.info("=== [Création de famille] Fin ===");
    }


    // 2. Récupérer toutes les familles
    @Transactional(readOnly = true)
    public List<Famille> getAllFamille() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut consulter les familles.");
        }
        return familleRepository.findAll();
    }

    // 3. Supprimer une famille
    public void supprimerFamille(Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut supprimer une famille.");
        }
        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + id));
        familleRepository.delete(famille);
    }

    // 4. Résilier abonnement familial
    public void resilierAbonnementFamilial(Long familleId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut résilier un abonnement familial.");
        }
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));
        famille.getMembres().forEach(membre -> membre.setTypeDeService(null));
        userRepository.saveAll(famille.getMembres());
    }

    // 5. Mettre à jour le téléphone de référence des membres d'une famille
    public void mettreAJourTelephoneRefrenceMembre(Long familleId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut mettre à jour le téléphone de référence.");
        }
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));
        String chefTelephone = famille.getChefFamille().getTelephone();
        famille.getMembres().forEach(membre -> membre.setTelephone(chefTelephone));
        userRepository.saveAll(famille.getMembres());
    }

    // 6. Récupérer les détails d'une famille par ID
    @Transactional(readOnly = true)
    public FamilleDTO getFamilleById(Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut consulter les détails d'une famille.");
        }
        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + id));
        return convertToDTO(famille);
    }

    // 7. Modifier une famille
    public void modifierFamille(Long id, FamilleDTO dto) {
        logger.info("=== [Modification de famille] Début ===");
        logger.info("ID famille: {}, Données reçues: {}", id, dto);

        // 🔐 Vérification des permissions
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            logger.warn("Accès refusé - utilisateur non autorisé à modifier une famille");
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut modifier une famille.");
        }

        // ✅ Récupération de la famille existante
        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + id));

        // ✅ Validation du chef
        if (dto.getChefFamilleId() == null) {
            throw new IllegalArgumentException("Le chef de famille est requis pour modifier une famille.");
        }

        // 🔍 Récupération du chef
        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(() -> new EntityNotFoundException("Chef de famille non trouvé avec l'ID : " + dto.getChefFamilleId()));

        // ✅ Détermination automatique du gym
        Gym gym = null;

        // 1️⃣ Si gymId fourni → utiliser celui-là
        if (dto.getGymId() != null) {
            gym = gymRepository.findById(dto.getGymId())
                    .orElseThrow(() -> new EntityNotFoundException("Gym non trouvé avec l'ID : " + dto.getGymId()));
            logger.info("Gym trouvé via DTO : {}", gym.getNom());

            // 2️⃣ Sinon → tenter d'utiliser le gym du chef de famille
        } else if (chefFamille.getGym() != null) {
            gym = chefFamille.getGym();
            logger.info("Gym déduit automatiquement du chef : {}", gym.getNom());

            // 3️⃣ Sinon → gym du staff connecté
        } else if (principal instanceof User staff && staff.getGym() != null) {
            gym = staff.getGym();
            logger.info("Gym assigné automatiquement à partir du staff connecté : {}", gym.getNom());
        }

        // 4️⃣ Sinon → erreur
        if (gym == null) {
            logger.error("Aucun gym détecté : impossible de modifier une famille sans gym");
            throw new IllegalArgumentException("Impossible de déterminer le gym : aucun gymId fourni, et ni le chef ni le staff n'ont de gym associé.");
        }

        // 🏗️ Mise à jour des champs
        famille.setNom(dto.getNom());
        famille.setChefFamille(chefFamille);
        famille.setGym(gym);

        // 👥 Gestion des membres
        List<Long> membresIds = (dto.getMembresId() != null) ? dto.getMembresId() : Collections.emptyList();
        List<User> newMembres = userRepository.findAllById(membresIds);

        if (!newMembres.isEmpty() && newMembres.contains(chefFamille)) {
            throw new IllegalArgumentException("Le chef de famille ne peut pas être inclus dans la liste des membres.");
        }

        if (famille.getMembres() == null) {
            famille.setMembres(new ArrayList<>(newMembres));
        } else {
            famille.getMembres().clear();
            famille.getMembres().addAll(newMembres);
        }

        // 💾 Sauvegarde
        familleRepository.save(famille);
        logger.info("Famille modifiée avec succès - ID: {}, Nom: '{}', Gym: {}", famille.getId(), famille.getNom(), gym.getNom());
        logger.info("=== [Modification de famille] Fin ===");
    }


    // 8. Consulter la liste des familles
    @Transactional(readOnly = true)
    public List<Famille> consulterFamille() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut consulter les familles.");
        }
        return familleRepository.findAll();
    }

    // Convertir une entité Famille en FamilleDTO
    private FamilleDTO convertToDTO(Famille famille) {
        FamilleDTO dto = new FamilleDTO();
        dto.setId(famille.getId());
        dto.setNom(famille.getNom());
        if (famille.getChefFamille() != null) {
            dto.setChefFamilleId(famille.getChefFamille().getId());
            dto.setChefFamilleNomPrenom(famille.getChefFamille().getNom() + " " + famille.getChefFamille().getPrenom());
        } else {
            dto.setChefFamilleId(null);
            dto.setChefFamilleNomPrenom("N/A");
        }
        dto.setMembresId(famille.getMembres().stream().map(User::getId).collect(Collectors.toList()));
        dto.setMembreNomPrenoms(famille.getMembres().stream()
                .map(m -> m.getNom() + " " + m.getPrenom())
                .collect(Collectors.toList()));
        dto.setGymId(famille.getGym() != null ? famille.getGym().getId() : null);
        return dto;
    }
}