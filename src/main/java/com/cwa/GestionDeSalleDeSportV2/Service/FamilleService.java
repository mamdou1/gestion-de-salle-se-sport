package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FamilleService {

    private static final Logger logger = LoggerFactory.getLogger(FamilleService.class);

    @Autowired
    private FamilleRepository familleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    // === MÉTHODES EXISTANTES (gardées pour compatibilité) ===

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

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            logger.warn("Accès refusé - utilisateur non autorisé à créer une famille");
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut créer une famille.");
        }

        if (dto.getChefFamilleId() == null) {
            throw new IllegalArgumentException("Le chef de famille est requis pour créer une famille.");
        }

        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(() -> new EntityNotFoundException("Chef de famille non trouvé avec l'ID : " + dto.getChefFamilleId()));

        Gym gym = null;
        if (dto.getGymId() != null) {
            gym = gymRepository.findById(dto.getGymId())
                    .orElseThrow(() -> new EntityNotFoundException("Gym non trouvé avec l'ID : " + dto.getGymId()));
        } else if (chefFamille.getGym() != null) {
            gym = chefFamille.getGym();
        } else if (principal instanceof User staff && staff.getGym() != null) {
            gym = staff.getGym();
        }

        if (gym == null) {
            throw new IllegalArgumentException("Impossible de déterminer le gym.");
        }

        Famille famille = new Famille();
        famille.setNom(dto.getNom());
        famille.setChefFamille(chefFamille);
        famille.setGym(gym);

        List<Long> membresIds = (dto.getMembresId() != null) ? dto.getMembresId() : Collections.emptyList();
        List<User> membres = userRepository.findAllById(membresIds);

        if (!membres.isEmpty() && membres.contains(chefFamille)) {
            throw new IllegalArgumentException("Le chef de famille ne peut pas être inclus dans la liste des membres.");
        }

        // Initialiser la liste des membres
        famille.setMembres(new ArrayList<>());

        // Ajouter les membres un par un et les affecter à la famille
        for (User membre : membres) {
            membre.setFamille(famille);
            famille.getMembres().add(membre);
        }

        famille = familleRepository.save(famille);

        // Sauvegarder les membres avec leur famille
        userRepository.saveAll(membres);

        // Affecter le chef de famille à la famille
        if (chefFamille.getFamille() == null) {
            chefFamille.setFamille(famille);
            userRepository.save(chefFamille);
        }

        logger.info("Famille créée avec succès - ID: {}, Nom: '{}', Gym: {}, Membres: {}",
                famille.getId(), famille.getNom(), gym.getNom(), famille.getMembres().size());
        logger.info("=== [Création de famille] Fin ===");
    }

    // 2. Récupérer toutes les familles
    @Transactional(readOnly = true)
    public List<FamilleDTO> getAllFamille() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut consulter les familles.");
        }
        return familleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        logger.info("=== [Récupération de famille] ID: {} ===", id);

        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + id));

        FamilleDTO familleDTO = new FamilleDTO();
        familleDTO.setId(famille.getId());
        familleDTO.setNom(famille.getNom());
        familleDTO.setChefFamilleId(famille.getChefFamille().getId());
        familleDTO.setChefFamilleNomPrenom(famille.getChefFamille().getNom() + " " + famille.getChefFamille().getPrenom());
        familleDTO.setMembresId(famille.getMembres().stream().map(User::getId).collect(Collectors.toList()));
        familleDTO.setMembreNomPrenoms(
                famille.getMembres().stream()
                        .map(m -> m.getNom() + " " + m.getPrenom())
                        .collect(Collectors.toList())
        );
        familleDTO.setGymId(famille.getGym().getId());

        logger.info("DTO retourné: {}", familleDTO);
        logger.info("=== [Récupération de famille] Fin ===");
        return familleDTO;
    }

    // 7. Modifier une famille
    public void modifierFamille(Long id, FamilleDTO dto) {
        logger.info("=== [Modification de famille] Début ===");
        logger.info("ID famille: {}, Données reçues: {}", id, dto);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            logger.warn("Accès refusé - utilisateur non autorisé à modifier une famille");
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut modifier une famille.");
        }

        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + id));

        if (dto.getChefFamilleId() == null) {
            throw new IllegalArgumentException("Le chef de famille est requis pour modifier une famille.");
        }

        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(() -> new EntityNotFoundException("Chef de famille non trouvé avec l'ID : " + dto.getChefFamilleId()));

        Gym gym = null;
        if (dto.getGymId() != null) {
            gym = gymRepository.findById(dto.getGymId())
                    .orElseThrow(() -> new EntityNotFoundException("Gym non trouvé avec l'ID : " + dto.getGymId()));
            logger.info("Gym trouvé via DTO : {}", gym.getNom());
        } else if (chefFamille.getGym() != null) {
            gym = chefFamille.getGym();
            logger.info("Gym déduit automatiquement du chef : {}", gym.getNom());
        } else if (principal instanceof User staff && staff.getGym() != null) {
            gym = staff.getGym();
            logger.info("Gym assigné automatiquement à partir du staff connecté : {}", gym.getNom());
        }

        if (gym == null) {
            logger.error("Aucun gym détecté : impossible de modifier une famille sans gym");
            throw new IllegalArgumentException("Impossible de déterminer le gym : aucun gymId fourni, et ni le chef ni le staff n'ont de gym associé.");
        }

        famille.setNom(dto.getNom());
        famille.setChefFamille(chefFamille);
        famille.setGym(gym);

        List<Long> membresIds = (dto.getMembresId() != null) ? dto.getMembresId() : Collections.emptyList();
        List<User> newMembres = userRepository.findAllById(membresIds);

        if (!newMembres.isEmpty() && newMembres.contains(chefFamille)) {
            throw new IllegalArgumentException("Le chef de famille ne peut pas être inclus dans la liste des membres.");
        }

        if (famille.getMembres() == null) {
            famille.setMembres(new ArrayList<>());
        } else {
            famille.getMembres().forEach(membre -> membre.setFamille(null));
            famille.getMembres().clear();
        }

        for (User membre : newMembres) {
            membre.setFamille(famille);
            famille.getMembres().add(membre);
        }

        familleRepository.save(famille);
        userRepository.saveAll(newMembres);

        logger.info("Famille modifiée avec succès - ID: {}, Nom: '{}', Gym: {}, Membres: {}",
                famille.getId(), famille.getNom(), gym.getNom(), famille.getMembres().size());
        logger.info("=== [Modification de famille] Fin ===");
    }

    // 8. Consulter la liste des familles
    @Transactional(readOnly = true)
    public List<FamilleDTO> consulterFamille() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new RuntimeException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut consulter les familles.");
        }
        return familleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 9. Ajouter un membre à une famille (par le staff)
    public void ajouterMembreAFamille(Long familleId, MembreDTO membreDTO) throws AccessDeniedException, MessagingException {
        logger.info("=== [Ajout membre à famille] Début ===");
        logger.info("Famille ID: {}, Membre: {}", familleId, membreDTO);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new AccessDeniedException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut ajouter un membre à une famille.");
        }

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));

        User membreExistant = userRepository.findByEmail(membreDTO.getEmailMembre());
        User nouveauMembre;

        if (membreExistant != null) {
            if (membreExistant.getFamille() != null && !membreExistant.getFamille().getId().equals(familleId)) {
                throw new IllegalArgumentException("Ce membre appartient déjà à une autre famille.");
            }
            nouveauMembre = membreExistant;
        } else {
            nouveauMembre = new User();
            nouveauMembre.setNom(membreDTO.getNomMembre());
            nouveauMembre.setPrenom(membreDTO.getPrenomMembre());
            nouveauMembre.setEmail(membreDTO.getEmailMembre());
            nouveauMembre.setTelephone(membreDTO.getNumeroTelephoneMembre());
            nouveauMembre.setGenre(membreDTO.getGenreMembre());
            nouveauMembre.setRole(Role.MEMBRE);
            nouveauMembre.setGym(famille.getGym());
            nouveauMembre = userRepository.save(nouveauMembre);
        }

        nouveauMembre.setFamille(famille);
        if (famille.getMembres() == null) {
            famille.setMembres(new ArrayList<>());
        }
        famille.getMembres().add(nouveauMembre);

        userRepository.save(nouveauMembre);
        familleRepository.save(famille);

        String nomComplet = getNomComplet(nouveauMembre);
        logger.info("Membre ajouté avec succès - ID: {}, Nom: {}, Famille: {}",
                nouveauMembre.getId(), nomComplet, famille.getNom());
        logger.info("=== [Ajout membre à famille] Fin ===");
    }

    // 10. Retirer un membre d'une famille
    public void retirerMembreDeFamille(Long familleId, Long membreId) throws AccessDeniedException, MessagingException {
        logger.info("=== [Retrait membre de famille] Début ===");
        logger.info("Famille ID: {}, Membre ID: {}", familleId, membreId);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new AccessDeniedException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut retirer un membre d'une famille.");
        }

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new EntityNotFoundException("Membre non trouvé avec l'ID : " + membreId));

        if (!famille.getMembres().contains(membre)) {
            throw new IllegalArgumentException("Ce membre n'appartient pas à cette famille.");
        }

        if (famille.getChefFamille().getId().equals(membreId)) {
            throw new IllegalArgumentException("Impossible de retirer le chef de famille. Transférez d'abord le rôle de chef.");
        }

        membre.setFamille(null);
        famille.getMembres().remove(membre);

        userRepository.save(membre);
        familleRepository.save(famille);

        String nomComplet = getNomComplet(membre);
        logger.info("Membre retiré avec succès - ID: {}, Nom: {}, Famille: {}",
                membre.getId(), nomComplet, famille.getNom());
        logger.info("=== [Retrait membre de famille] Fin ===");
    }

    // 11. Changer le chef de famille
    public void changerChefFamille(Long familleId, Long nouveauChefId) throws AccessDeniedException {
        logger.info("=== [Changement chef famille] Début ===");
        logger.info("Famille ID: {}, Nouveau chef ID: {}", familleId, nouveauChefId);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new AccessDeniedException("Accès non autorisé : seul un ADMIN ou RECEPTIONNISTE peut changer le chef de famille.");
        }

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));

        User nouveauChef = userRepository.findById(nouveauChefId)
                .orElseThrow(() -> new EntityNotFoundException("Nouveau chef non trouvé avec l'ID : " + nouveauChefId));

        if (famille.getMembres() == null || !famille.getMembres().contains(nouveauChef)) {
            throw new IllegalArgumentException("Le nouveau chef doit appartenir à la famille.");
        }

        User ancienChef = famille.getChefFamille();
        famille.setChefFamille(nouveauChef);

        familleRepository.save(famille);

        String ancienNomComplet = getNomComplet(ancienChef);
        String nouveauNomComplet = getNomComplet(nouveauChef);

        logger.info("Chef de famille changé avec succès - Ancien: {}, Nouveau: {}, Famille: {}",
                ancienNomComplet, nouveauNomComplet, famille.getNom());
        logger.info("=== [Changement chef famille] Fin ===");
    }

    // 12. Obtenir tous les membres d'une famille
    @Transactional(readOnly = true)
    public List<User> getMembresDeFamille(Long familleId) {
        logger.info("=== [Récupération membres famille] Début ===");
        logger.info("Famille ID: {}", familleId);

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));

        List<User> membres = famille.getMembres() != null ? famille.getMembres() : Collections.emptyList();

        logger.info("Membres récupérés: {} pour famille: {}", membres.size(), famille.getNom());
        logger.info("=== [Récupération membres famille] Fin ===");

        return membres;
    }

    // 13. Vérifier si un membre appartient à une famille
    @Transactional(readOnly = true)
    public boolean membreAppartientAFamille(Long familleId, Long membreId) {
        logger.info("=== [Vérification appartenance membre] Début ===");
        logger.info("Famille ID: {}, Membre ID: {}", familleId, membreId);

        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID : " + familleId));

        boolean appartient = famille.getMembres().stream()
                .anyMatch(membre -> membre.getId().equals(membreId));

        logger.info("Membre {} appartient à famille {}: {}", membreId, familleId, appartient);
        logger.info("=== [Vérification appartenance membre] Fin ===");

        return appartient;
    }

    // 14. Soumettre une demande d'ajout à une famille (par un membre)
    public void soumettreDemandeAjout(Long familleId, MembreDTO membreDTO) throws MessagingException {
        logger.info("=== [Soumission demande ajout] Début ===");
        logger.info("Famille ID: {}, Membre: {}", familleId, membreDTO);

        // Implémentation basique - à adapter selon votre logique métier
        // Cette méthode pourrait créer une entrée dans une table de demandes en attente

        logger.info("Demande d'ajout soumise avec succès pour famille: {}", familleId);
        logger.info("=== [Soumission demande ajout] Fin ===");
    }

    // 15. Obtenir les demandes d'ajout en attente pour une famille
    @Transactional(readOnly = true)
    public List<MembreDTO> getDemandesAjoutEnAttente(Long familleId) throws AccessDeniedException {
        logger.info("=== [Récupération demandes ajout] Début ===");
        logger.info("Famille ID: {}", familleId);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new AccessDeniedException("Accès non autorisé.");
        }

        // Implémentation basique - retourne une liste vide pour l'exemple
        List<MembreDTO> demandes = Collections.emptyList();

        logger.info("Demandes en attente récupérées: {} pour famille: {}", demandes.size(), familleId);
        logger.info("=== [Récupération demandes ajout] Fin ===");

        return demandes;
    }

    // 16. Accepter une demande d'ajout
    public void accepterDemandeAjout(Long familleId, Long demandeId) throws AccessDeniedException, MessagingException {
        logger.info("=== [Acceptation demande ajout] Début ===");
        logger.info("Famille ID: {}, Demande ID: {}", familleId, demandeId);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new AccessDeniedException("Accès non autorisé.");
        }

        // Implémentation basique - à adapter selon votre logique métier

        logger.info("Demande d'ajout acceptée - Demande ID: {}, Famille: {}", demandeId, familleId);
        logger.info("=== [Acceptation demande ajout] Fin ===");
    }

    // 17. Refuser une demande d'ajout
    public void refuserDemandeAjout(Long familleId, Long demandeId) throws AccessDeniedException, MessagingException {
        logger.info("=== [Refus demande ajout] Début ===");
        logger.info("Famille ID: {}, Demande ID: {}", familleId, demandeId);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!estMembreDuStaff(principal)) {
            throw new AccessDeniedException("Accès non autorisé.");
        }

        // Implémentation basique - à adapter selon votre logique métier

        logger.info("Demande d'ajout refusée - Demande ID: {}, Famille: {}", demandeId, familleId);
        logger.info("=== [Refus demande ajout] Fin ===");
    }

    // 18. Obtenir le nombre total de familles
    @Transactional(readOnly = true)
    public long getNombreTotalFamilles() {
        logger.info("=== [Comptage familles] Début ===");

        long count = familleRepository.count();

        logger.info("Nombre total de familles: {}", count);
        logger.info("=== [Comptage familles] Fin ===");

        return count;
    }

    // 19. Obtenir le nombre moyen de membres par famille
    @Transactional(readOnly = true)
    public double getMoyenneMembresParFamille() {
        logger.info("=== [Calcul moyenne membres] Début ===");

        List<Famille> familles = familleRepository.findAll();
        if (familles.isEmpty()) {
            return 0.0;
        }

        double totalMembres = familles.stream()
                .mapToDouble(f -> f.getMembres() != null ? f.getMembres().size() : 0)
                .sum();

        double moyenne = totalMembres / familles.size();

        logger.info("Moyenne membres par famille: {}", moyenne);
        logger.info("=== [Calcul moyenne membres] Fin ===");

        return Math.round(moyenne * 100.0) / 100.0;
    }

    // 20. Obtenir les familles avec le plus de membres
    @Transactional(readOnly = true)
    public List<FamilleDTO> getFamillesAvecPlusDeMembres(int limit) {
        logger.info("=== [Récupération familles plus grandes] Début ===");
        logger.info("Limit: {}", limit);

        List<Famille> familles = familleRepository.findAll();

        List<FamilleDTO> famillesTriees = familles.stream()
                .sorted((f1, f2) -> {
                    int size1 = f1.getMembres() != null ? f1.getMembres().size() : 0;
                    int size2 = f2.getMembres() != null ? f2.getMembres().size() : 0;
                    return Integer.compare(size2, size1);
                })
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("Familles avec plus de membres récupérées: {}", famillesTriees.size());
        logger.info("=== [Récupération familles plus grandes] Fin ===");

        return famillesTriees;
    }

    // 21. Rechercher des familles par nom
    @Transactional(readOnly = true)
    public List<FamilleDTO> rechercherFamillesParNom(String nom) {
        logger.info("=== [Recherche familles par nom] Début ===");
        logger.info("Nom recherché: {}", nom);

        List<Famille> familles = familleRepository.findByNomContainingIgnoreCase(nom);
        List<FamilleDTO> resultats = familles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("Résultats recherche: {} familles trouvées", resultats.size());
        logger.info("=== [Recherche familles par nom] Fin ===");

        return resultats;
    }

    // 22. Filtrer les familles par chef de famille
    @Transactional(readOnly = true)
    public List<FamilleDTO> filtrerFamillesParChef(String nomChef) {
        logger.info("=== [Filtrage familles par chef] Début ===");
        logger.info("Nom chef: {}", nomChef);

        List<Famille> toutesFamilles = familleRepository.findAll();

        List<FamilleDTO> resultats = toutesFamilles.stream()
                .filter(famille -> {
                    if (famille.getChefFamille() == null) return false;
                    String nomCompletChef = getNomComplet(famille.getChefFamille());
                    return nomCompletChef.toLowerCase().contains(nomChef.toLowerCase());
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("Résultats filtrage: {} familles trouvées", resultats.size());
        logger.info("=== [Filtrage familles par chef] Fin ===");

        return resultats;
    }

    // 23. Filtrer les familles par nombre de membres
    @Transactional(readOnly = true)
    public List<FamilleDTO> filtrerFamillesParNombreMembres(int minMembres, int maxMembres) {
        logger.info("=== [Filtrage familles par nombre membres] Début ===");
        logger.info("Min: {}, Max: {}", minMembres, maxMembres);

        List<Famille> familles = familleRepository.findAll();

        List<FamilleDTO> resultats = familles.stream()
                .filter(f -> {
                    int nombreMembres = f.getMembres() != null ? f.getMembres().size() : 0;
                    return nombreMembres >= minMembres && nombreMembres <= maxMembres;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("Résultats filtrage: {} familles trouvées", resultats.size());
        logger.info("=== [Filtrage familles par nombre membres] Fin ===");

        return resultats;
    }

    // === MÉTHODES CORRIGÉES POUR LE COMPTAGE ===

    // 24. Obtenir le nombre de membres sans famille (CORRIGÉ ET OPTIMISÉ)
    @Transactional(readOnly = true)
    public long getNombreMembresSansFamille() {
        logger.info("=== [Comptage membres sans famille] Début ===");

        try {
            // Utilisation de la méthode optimisée du repository
            Long count = userRepository.countMembresSansFamille();
            if (count == null) {
                count = 0L;
            }

            logger.info("Nombre de membres sans famille: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des membres sans famille: {}", e.getMessage());
            // Fallback sécurisé
            return 0L;
        }
    }

    // 25. Obtenir le nombre total de membres (CORRIGÉ ET OPTIMISÉ)
    @Transactional(readOnly = true)
    public long getNombreTotalMembres() {
        logger.info("=== [Comptage total membres] Début ===");

        try {
            // Utilisation de la méthode optimisée du repository
            Long count = userRepository.countTotalMembres();
            if (count == null) {
                count = 0L;
            }

            logger.info("Nombre total de membres: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Erreur lors du comptage total des membres: {}", e.getMessage());
            // Fallback sécurisé
            return 0L;
        }
    }

    // 26. Obtenir toutes les statistiques (VERSION CORRIGÉE)
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesFamilles() {
        logger.info("=== [Récupération statistiques familles] Début ===");

        Map<String, Object> stats = new HashMap<>();

        try {
            // Utilisation des méthodes optimisées
            long totalMembres = getNombreTotalMembres();
            long totalFamilles = getNombreTotalFamilles();
            long membresSansFamille = getNombreMembresSansFamille();
            double moyenneMembres = getMoyenneMembresParFamille();

            // Calculer le nombre total de membres dans les familles
            List<Famille> familles = familleRepository.findAll();
            long totalMembresDansFamilles = familles.stream()
                    .mapToLong(f -> f.getMembres() != null ? f.getMembres().size() : 0)
                    .sum();

            // VÉRIFICATION DE COHÉRENCE
            boolean coherent = totalMembres == (membresSansFamille + totalMembresDansFamilles);

            stats.put("totalMembres", totalMembres);
            stats.put("totalFamilles", totalFamilles);
            stats.put("membresSansFamille", membresSansFamille);
            stats.put("membresAvecFamille", totalMembresDansFamilles);
            stats.put("moyenneMembresParFamille", moyenneMembres);
            stats.put("coherent", coherent);

            if (!coherent) {
                long ecart = totalMembres - (membresSansFamille + totalMembresDansFamilles);
                logger.warn("INCOHÉRENCE DANS LES STATISTIQUES: {} != {} + {} (Écart: {})",
                        totalMembres, membresSansFamille, totalMembresDansFamilles, ecart);
                stats.put("ecart", ecart);

                // DEBUG: Log détaillé pour identifier le problème
                logger.debug("Détail du calcul: totalMembres={}, membresSansFamille={}, totalMembresDansFamilles={}",
                        totalMembres, membresSansFamille, totalMembresDansFamilles);
            } else {
                logger.info("✅ Statistiques cohérentes: {} = {} + {}",
                        totalMembres, membresSansFamille, totalMembresDansFamilles);
            }

        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques: {}", e.getMessage(), e);
            // Valeurs par défaut en cas d'erreur
            stats.put("totalMembres", 0);
            stats.put("totalFamilles", 0);
            stats.put("membresSansFamille", 0);
            stats.put("membresAvecFamille", 0);
            stats.put("moyenneMembresParFamille", 0.0);
            stats.put("coherent", false);
            stats.put("erreur", e.getMessage());
        }

        logger.info("Statistiques calculées: {}", stats);
        logger.info("=== [Récupération statistiques familles] Fin ===");

        return stats;
    }

    // 27. Vérifier la cohérence des données (VERSION AMÉLIORÉE)
    @Transactional(readOnly = true)
    public Map<String, Object> verifierCoherenceDonnees() {
        logger.info("=== [Vérification cohérence données] Début ===");

        Map<String, Object> resultat = new HashMap<>();

        try {
            long totalMembres = getNombreTotalMembres();
            long membresSansFamille = getNombreMembresSansFamille();

            // Calculer le nombre total de membres dans les familles
            List<Famille> familles = familleRepository.findAll();
            long totalMembresDansFamilles = familles.stream()
                    .mapToLong(f -> f.getMembres() != null ? f.getMembres().size() : 0)
                    .sum();

            // Vérifier l'équation : totalMembres = membresSansFamille + totalMembresDansFamilles
            boolean coherent = totalMembres == (membresSansFamille + totalMembresDansFamilles);

            resultat.put("totalMembres", totalMembres);
            resultat.put("membresSansFamille", membresSansFamille);
            resultat.put("membresDansFamilles", totalMembresDansFamilles);
            resultat.put("coherent", coherent);
            resultat.put("equation", totalMembres + " = " + membresSansFamille + " + " + totalMembresDansFamilles);

            if (!coherent) {
                long ecart = totalMembres - (membresSansFamille + totalMembresDansFamilles);
                logger.error("❌ INCOHÉRENCE DÉTECTÉE: {} != {} + {} (Écart: {})",
                        totalMembres, membresSansFamille, totalMembresDansFamilles, ecart);
                resultat.put("ecart", ecart);

                // Analyse détaillée pour debug
                logger.debug("Analyse détaillée:");
                logger.debug("- Total membres (countTotalMembres): {}", totalMembres);
                logger.debug("- Membres sans famille (countMembresSansFamille): {}", membresSansFamille);
                logger.debug("- Membres dans familles (somme manuelle): {}", totalMembresDansFamilles);

                // Vérification alternative avec une autre méthode
                try {
                    Object[] stats = userRepository.getStatistiquesMembresFamilles();
                    if (stats != null && stats.length >= 3) {
                        Long altTotal = ((Number) stats[0]).longValue();
                        Long altSansFamille = ((Number) stats[1]).longValue();
                        Long altAvecFamille = ((Number) stats[2]).longValue();

                        logger.debug("Vérification alternative:");
                        logger.debug("- Total (getStatistiquesMembresFamilles): {}", altTotal);
                        logger.debug("- Sans famille (getStatistiquesMembresFamilles): {}", altSansFamille);
                        logger.debug("- Avec famille (getStatistiquesMembresFamilles): {}", altAvecFamille);

                        resultat.put("verification_alternative", Map.of(
                                "total", altTotal,
                                "sansFamille", altSansFamille,
                                "avecFamille", altAvecFamille
                        ));
                    }
                } catch (Exception e) {
                    logger.debug("Vérification alternative échouée: {}", e.getMessage());
                }
            } else {
                logger.info("✅ Données cohérentes: {} = {} + {}",
                        totalMembres, membresSansFamille, totalMembresDansFamilles);
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de cohérence: {}", e.getMessage(), e);
            resultat.put("erreur", e.getMessage());
            resultat.put("coherent", false);
        }

        logger.info("Résultat vérification cohérence: {}", resultat);
        logger.info("=== [Vérification cohérence données] Fin ===");

        return resultat;
    }

    // 28. Méthode de debug pour analyser les données
    @Transactional(readOnly = true)
    public Map<String, Object> analyserDonneesPourDebug() {
        logger.info("=== [Analyse données pour debug] Début ===");

        Map<String, Object> analyse = new HashMap<>();

        try {
            // 1. Compter avec différentes méthodes
            Long total1 = userRepository.countTotalMembres();
            Long total2 = userRepository.countByRole(Role.MEMBRE);

            Long sansFamille1 = userRepository.countMembresSansFamille();
            Long sansFamille2 = userRepository.countByFamilleIsNullAndRole(Role.MEMBRE);

            // 2. Liste des membres sans famille
            List<User> membresSansFamille = userRepository.findMembresSansFamilleWithDetails();

            // 3. Statistiques avancées
            Object[] stats = userRepository.getStatistiquesMembresFamilles();

            analyse.put("countTotalMembres", total1);
            analyse.put("countByRole_MEMBRE", total2);
            analyse.put("countMembresSansFamille", sansFamille1);
            analyse.put("countByFamilleIsNullAndRole", sansFamille2);
            analyse.put("membresSansFamille_liste", membresSansFamille.size());
            analyse.put("statistiquesMembresFamilles", stats != null ? Arrays.toString(stats) : "null");

            // 4. Vérifier les familles
            List<Famille> familles = familleRepository.findAll();
            analyse.put("nombreFamilles", familles.size());

            Map<String, Integer> membresParFamille = new HashMap<>();
            for (Famille famille : familles) {
                int nbMembres = famille.getMembres() != null ? famille.getMembres().size() : 0;
                membresParFamille.put(famille.getNom() + " (ID:" + famille.getId() + ")", nbMembres);
            }
            analyse.put("membresParFamille", membresParFamille);

        } catch (Exception e) {
            logger.error("Erreur lors de l'analyse: {}", e.getMessage(), e);
            analyse.put("erreur", e.getMessage());
        }

        logger.info("Résultat analyse: {}", analyse);
        logger.info("=== [Analyse données pour debug] Fin ===");

        return analyse;
    }

    // === MÉTHODES CORRIGÉES POUR LES MEMBRES DISPONIBLES ===

    // 29. CORRECTION : Récupérer les membres disponibles pour une famille
    @Transactional(readOnly = true)
    public List<User> getMembresDisponiblesPourFamille(Long familleId, Long gymId) {
        logger.info("🔍 [SERVICE] Recherche des membres disponibles pour famille ID: {} dans gym ID: {}", familleId, gymId);

        try {
            // Vérifier que la famille existe
            Famille famille = familleRepository.findById(familleId)
                    .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID: " + familleId));

            // CORRECTION : Utiliser la méthode du repository qui filtre correctement
            List<User> membresDisponibles = userRepository.findMembresDisponiblesPourFamille(familleId, gymId);

            // FILTRE DE SÉCURITÉ SUPPLÉMENTAIRE
            List<User> membresFiltres = membresDisponibles.stream()
                    .filter(membre -> {
                        boolean estDisponible = membre.getFamille() == null || membre.getFamille().getId().equals(familleId);
                        if (!estDisponible) {
                            logger.warn("🚫 Membre {} {} (ID: {}) filtré - appartient à famille ID: {}",
                                    membre.getNom(), membre.getPrenom(), membre.getId(),
                                    membre.getFamille() != null ? membre.getFamille().getId() : "null");
                        }
                        return estDisponible;
                    })
                    .collect(Collectors.toList());

            logger.info("✅ [SERVICE] {} membres disponibles trouvés pour famille ID: {} ({} après filtrage)",
                    membresDisponibles.size(), familleId, membresFiltres.size());

            // Log pour debug
            for (User membre : membresFiltres) {
                logger.debug("📋 Membre disponible: {} {} (ID: {}, Famille: {})",
                        membre.getNom(), membre.getPrenom(), membre.getId(),
                        membre.getFamille() != null ? membre.getFamille().getId() : "null");
            }

            return membresFiltres;

        } catch (Exception e) {
            logger.error("❌ [SERVICE] Erreur lors de la recherche des membres disponibles: {}", e.getMessage());
            // Fallback : utiliser la méthode alternative
            return getMembresDisponiblesPourFamilleAlternative(familleId, gymId);
        }
    }

    // 30. MÉTHODE ALTERNATIVE si le repository ne fonctionne pas
    @Transactional(readOnly = true)
    public List<User> getMembresDisponiblesPourFamilleAlternative(Long familleId, Long gymId) {
        logger.info("🔍 [ALTERNATIVE] Recherche des membres disponibles pour famille ID: {} dans gym ID: {}", familleId, gymId);

        try {
            // Récupérer tous les membres du gym
            List<User> tousLesMembres = userRepository.findByGymIdAndRole(gymId, Role.MEMBRE);

            // Filtrer manuellement
            List<User> membresDisponibles = tousLesMembres.stream()
                    .filter(membre -> {
                        // Membre sans famille
                        if (membre.getFamille() == null) {
                            return true;
                        }
                        // Membre déjà dans cette famille
                        if (membre.getFamille().getId().equals(familleId)) {
                            return true;
                        }
                        // Membre dans une autre famille - NON DISPONIBLE
                        return false;
                    })
                    .collect(Collectors.toList());

            logger.info("✅ [ALTERNATIVE] {} membres disponibles trouvés sur {} membres totaux",
                    membresDisponibles.size(), tousLesMembres.size());

            return membresDisponibles;

        } catch (Exception e) {
            logger.error("❌ [ALTERNATIVE] Erreur lors de la recherche alternative: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 31. Obtenir les membres sans famille ou dans une famille spécifique
    @Transactional(readOnly = true)
    public List<User> getMembresSansFamilleOuDansFamille(Long familleId) {
        logger.info("🔍 [SERVICE] Recherche des membres sans famille ou dans famille ID: {}", familleId);

        try {
            List<User> membres = userRepository.findMembresSansFamilleOuDansFamille(familleId);

            logger.info("✅ [SERVICE] {} membres trouvés pour famille ID: {}", membres.size(), familleId);

            return membres;

        } catch (Exception e) {
            logger.error("❌ [SERVICE] Erreur lors de la recherche des membres: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 32. Obtenir les statistiques des membres disponibles
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesMembresDisponibles(Long familleId, Long gymId) {
        logger.info("🔍 [SERVICE] Récupération des statistiques des membres disponibles pour famille ID: {}", familleId);

        Map<String, Object> stats = new HashMap<>();

        try {
            Long totalMembres = userRepository.countByRole(Role.MEMBRE);
            Long membresDisponibles = userRepository.countMembresDisponiblesPourFamille(familleId, gymId);
            Long membresDansFamille = userRepository.countByFamilleIsNotNullAndRole(Role.MEMBRE);

            stats.put("totalMembres", totalMembres);
            stats.put("membresDisponibles", membresDisponibles);
            stats.put("membresDansFamille", membresDansFamille);
            stats.put("familleActuelleId", familleId);

            logger.info("✅ [SERVICE] Statistiques récupérées: {}", stats);

        } catch (Exception e) {
            logger.error("❌ [SERVICE] Erreur lors de la récupération des statistiques: {}", e.getMessage());
            stats.put("erreur", e.getMessage());
        }

        return stats;
    }

    // === MÉTHODES UTILITAIRES ===

    // Méthode utilitaire pour obtenir le nom complet d'un utilisateur
    private String getNomComplet(User user) {
        if (user == null) return "Utilisateur inconnu";
        String nom = user.getNom() != null ? user.getNom() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        String nomComplet = (nom + " " + prenom).trim();
        return nomComplet.isEmpty() ? "Sans nom" : nomComplet;
    }

    // Convertir une entité Famille en FamilleDTO
    private FamilleDTO convertToDTO(Famille famille) {
        FamilleDTO dto = new FamilleDTO();
        dto.setId(famille.getId());
        dto.setNom(famille.getNom() != null ? famille.getNom() : "Famille sans nom");

        if (famille.getChefFamille() != null) {
            dto.setChefFamilleId(famille.getChefFamille().getId());
            String nomComplet = getNomComplet(famille.getChefFamille());
            dto.setChefFamilleNomPrenom(nomComplet);
        } else {
            logger.warn("Chef de famille manquant pour famille ID: {}", famille.getId());
            throw new IllegalStateException("Chef de famille manquant pour famille ID: " + famille.getId());
        }

        List<User> membres = famille.getMembres() != null ? famille.getMembres() : Collections.emptyList();
        dto.setMembresId(membres.stream()
                .filter(m -> m.getId() != null)
                .map(User::getId)
                .collect(Collectors.toList()));
        dto.setMembreNomPrenoms(membres.stream()
                .map(this::getNomComplet)
                .filter(nom -> !nom.equals("Sans nom"))
                .collect(Collectors.toList()));

        dto.setGymId(famille.getGym() != null ? famille.getGym().getId() : null);
        return dto;
    }
}