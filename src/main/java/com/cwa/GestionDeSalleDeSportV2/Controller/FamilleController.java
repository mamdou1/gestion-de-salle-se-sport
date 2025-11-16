package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAvecMembresDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleService;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/familles")
public class FamilleController {

    private static final Logger logger = LoggerFactory.getLogger(FamilleController.class);

    @Autowired
    private final FamilleService familleService;
    private final FamilleRepository familleRepository;

    public FamilleController(FamilleService familleService, FamilleRepository familleRepository) {
        this.familleService = familleService;
        this.familleRepository = familleRepository;
    }

    // === GESTION DES FAMILLES ===

    // 1. Création de famille
    @PostMapping("/creer_famille")
    public ResponseEntity<String> creeFamille(@RequestBody FamilleDTO dto) {
        try {
            if (dto == null) {
                logger.error("Données de création de famille manquantes");
                return new ResponseEntity<>("Erreur : Données de création de famille manquantes.", HttpStatus.BAD_REQUEST);
            }
            familleService.creerFamille(dto);
            return new ResponseEntity<>("Famille créée avec succès", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation lors de la création de la famille : {}", e.getMessage());
            return new ResponseEntity<>("Erreur de validation : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            logger.error("Violation de contrainte d'unicité lors de la création de la famille : {}", e.getMessage());
            return new ResponseEntity<>("Une famille avec ce chef ou ce nom existe déjà.", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Erreur inattendue lors de la création de la famille : {}", e.getMessage());
            return new ResponseEntity<>("Erreur inattendue : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. Consulter la liste des familles
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    @GetMapping
    public ResponseEntity<List<FamilleDTO>> consulterFamille() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Principal : {}", auth.getPrincipal());
        logger.info("Authorities : {}", auth.getAuthorities());
        try {
            List<FamilleDTO> familles = familleService.consulterFamille();
            logger.info("Nombre de familles récupérées : {}", familles.size());
            return new ResponseEntity<>(familles, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la récupération des familles : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 3. Supprimer une famille
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<String> supprimerFamille(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                logger.error("ID de famille manquant ou vide");
                return new ResponseEntity<>("Erreur : ID de famille manquant ou vide.", HttpStatus.BAD_REQUEST);
            }
            Long familleId = Long.valueOf(id);
            familleService.supprimerFamille(familleId);
            return new ResponseEntity<>("Famille supprimée avec succès", HttpStatus.OK);
        } catch (NumberFormatException e) {
            logger.error("ID de famille invalide : {}", id);
            return new ResponseEntity<>("Erreur : ID de famille invalide.", HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", id);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Erreur inattendue lors de la suppression de la famille : {}", e.getMessage());
            return new ResponseEntity<>("Erreur inattendue : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Récupérer les détails d'une famille par ID
    @GetMapping("/{id}")
    public ResponseEntity<FamilleDTO> getFamilleById(@PathVariable Long id) {
        logger.info("Requête GET /api/familles/{} reçue", id);
        try {
            FamilleDTO familleDTO = familleService.getFamilleById(id);
            return ResponseEntity.ok(familleDTO);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", id);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la récupération de la famille : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 5. Modifier une famille
    @PutMapping("/{id}")
    public ResponseEntity<String> modifierFamille(@PathVariable String id, @RequestBody FamilleDTO dto) {
        try {
            if (id == null || id.trim().isEmpty()) {
                logger.error("ID de famille manquant ou vide");
                return new ResponseEntity<>("Erreur : ID de famille manquant ou vide.", HttpStatus.BAD_REQUEST);
            }
            if (dto == null) {
                logger.error("Données de modification de famille manquantes");
                return new ResponseEntity<>("Erreur : Données de modification de famille manquantes.", HttpStatus.BAD_REQUEST);
            }
            Long familleId = Long.valueOf(id);
            familleService.modifierFamille(familleId, dto);
            return new ResponseEntity<>("Famille modifiée avec succès", HttpStatus.OK);
        } catch (NumberFormatException e) {
            logger.error("ID de famille invalide : {}", id);
            return new ResponseEntity<>("Erreur : ID de famille invalide.", HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", id);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation lors de la modification de la famille : {}", e.getMessage());
            return new ResponseEntity<>("Erreur de validation : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Erreur inattendue lors de la modification de la famille : {}", e.getMessage());
            return new ResponseEntity<>("Erreur inattendue : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === GESTION DES MEMBRES DANS LES FAMILLES ===

    // 6. Ajouter un membre à une famille (par le staff)
    @PostMapping("/{familleId}/membres")
    public ResponseEntity<String> ajouterMembreAFamille(
            @PathVariable Long familleId,
            @RequestBody MembreDTO membreDTO) {
        try {
            familleService.ajouterMembreAFamille(familleId, membreDTO);
            return new ResponseEntity<>("Membre ajouté à la famille avec succès", HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (MessagingException e) {
            logger.error("Erreur d'envoi d'email : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de l'envoi de la notification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 7. Retirer un membre d'une famille
    @DeleteMapping("/{familleId}/membres/{membreId}")
    public ResponseEntity<String> retirerMembreDeFamille(
            @PathVariable Long familleId,
            @PathVariable Long membreId) {
        try {
            familleService.retirerMembreDeFamille(familleId, membreId);
            return new ResponseEntity<>("Membre retiré de la famille avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("Famille ou membre non trouvé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (MessagingException e) {
            logger.error("Erreur d'envoi d'email : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de l'envoi de la notification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 8. Transférer le rôle de chef de famille
    @PutMapping("/{familleId}/changer-chef/{nouveauChefId}")
    public ResponseEntity<String> changerChefFamille(
            @PathVariable Long familleId,
            @PathVariable Long nouveauChefId) {
        try {
            familleService.changerChefFamille(familleId, nouveauChefId);
            return new ResponseEntity<>("Chef de famille changé avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("Famille ou membre non trouvé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    // 9. Obtenir tous les membres d'une famille
    @GetMapping("/{familleId}/membres")
    public ResponseEntity<List<User>> getMembresDeFamille(@PathVariable Long familleId) {
        try {
            List<User> membres = familleService.getMembresDeFamille(familleId);
            return new ResponseEntity<>(membres, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // 10. Vérifier si un membre appartient à une famille
    @GetMapping("/{familleId}/membres/{membreId}/appartient")
    public ResponseEntity<Boolean> membreAppartientAFamille(
            @PathVariable Long familleId,
            @PathVariable Long membreId) {
        try {
            boolean appartient = familleService.membreAppartientAFamille(familleId, membreId);
            return new ResponseEntity<>(appartient, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
    }

    // === STATISTIQUES ET RAPPORTS ===

    // 11. Obtenir le nombre total de familles
    @GetMapping("/count")
    public ResponseEntity<Long> getNombreTotalFamilles() {
        try {
            long count = familleService.getNombreTotalFamilles();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors du comptage des familles : {}", e.getMessage());
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 12. Obtenir le nombre moyen de membres par famille
    @GetMapping("/stats/moyenne-membres")
    public ResponseEntity<Double> getMoyenneMembresParFamille() {
        try {
            double moyenne = familleService.getMoyenneMembresParFamille();
            return new ResponseEntity<>(moyenne, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors du calcul de la moyenne : {}", e.getMessage());
            return new ResponseEntity<>(0.0, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 13. Obtenir les familles avec le plus de membres
    @GetMapping("/stats/familles-plus-grandes")
    public ResponseEntity<List<FamilleDTO>> getFamillesAvecPlusDeMembres(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<FamilleDTO> familles = familleService.getFamillesAvecPlusDeMembres(limit);
            return new ResponseEntity<>(familles, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la récupération des statistiques : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === RECHERCHE ET FILTRES ===

    // 14. Rechercher des familles par nom
    @GetMapping("/recherche")
    public ResponseEntity<List<FamilleDTO>> rechercherFamillesParNom(
            @RequestParam String nom) {
        try {
            List<FamilleDTO> familles = familleService.rechercherFamillesParNom(nom);
            return new ResponseEntity<>(familles, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la recherche : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 15. Filtrer les familles par chef de famille
    @GetMapping("/filtre/chef")
    public ResponseEntity<List<FamilleDTO>> filtrerFamillesParChef(
            @RequestParam String nomChef) {
        try {
            List<FamilleDTO> familles = familleService.filtrerFamillesParChef(nomChef);
            return new ResponseEntity<>(familles, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors du filtrage : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 16. Filtrer les familles par nombre de membres
    @GetMapping("/filtre/membres")
    public ResponseEntity<List<FamilleDTO>> filtrerFamillesParNombreMembres(
            @RequestParam int minMembres,
            @RequestParam int maxMembres) {
        try {
            List<FamilleDTO> familles = familleService.filtrerFamillesParNombreMembres(minMembres, maxMembres);
            return new ResponseEntity<>(familles, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors du filtrage : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === ENDPOINTS EXISTANTS (gardés pour compatibilité) ===

    // 17. Mettre à jour le téléphone de référence des membres d'une famille
    @PutMapping("/update/membre/telephone_reference/{familleId}")
    public ResponseEntity<String> mettreAJourTelephoneReferenceMembre(@PathVariable String familleId) {
        try {
            if (familleId == null || familleId.trim().isEmpty()) {
                logger.error("ID de famille manquant ou vide");
                return new ResponseEntity<>("Erreur : ID de famille manquant ou vide.", HttpStatus.BAD_REQUEST);
            }
            Long id = Long.valueOf(familleId);
            familleService.mettreAJourTelephoneRefrenceMembre(id);
            return new ResponseEntity<>("Le téléphone de référence des membres de cette famille a été mis à jour avec succès.", HttpStatus.OK);
        } catch (NumberFormatException e) {
            logger.error("ID de famille invalide : {}", familleId);
            return new ResponseEntity<>("Erreur : ID de famille invalide.", HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Erreur inattendue lors de la mise à jour du téléphone : {}", e.getMessage());
            return new ResponseEntity<>("Erreur inattendue : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 18. Récupérer les familles avec leurs membres complets
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    @GetMapping("/with-members")
    public ResponseEntity<List<FamilleAvecMembresDTO>> consulterFamillesAvecMembres() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Récupération des familles avec membres - Principal: {}", auth.getPrincipal());

        try {
            List<Famille> familles = familleRepository.findAllWithChefAndMembres();
            logger.info("Nombre de familles récupérées avec membres: {}", familles.size());

            List<FamilleAvecMembresDTO> dtos = familles.stream()
                    .map(this::convertToFamilleAvecMembresDTO)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(dtos, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la récupération des familles avec membres: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 19. Récupérer une famille spécifique avec ses membres
    @GetMapping("/{id}/with-members")
    public ResponseEntity<FamilleAvecMembresDTO> getFamilleByIdWithMembers(@PathVariable Long id) {
        logger.info("Requête GET /api/familles/{}/with-members reçue", id);

        try {
            Famille famille = familleRepository.findByIdWithMembres(id)
                    .orElseThrow(() -> new EntityNotFoundException("Famille non trouvée avec l'ID: " + id));

            FamilleAvecMembresDTO dto = convertToFamilleAvecMembresDTO(famille);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID: {}", id);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la récupération de la famille avec membres ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 20. Obtenir les statistiques complètes des familles
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesFamilles() {
        try {
            logger.info("=== [CONTROLLER] Récupération des statistiques familles ===");

            Map<String, Object> stats = familleService.getStatistiquesFamilles();

            logger.info("✅ [CONTROLLER] Statistiques retournées: {}", stats);
            return new ResponseEntity<>(stats, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("❌ [CONTROLLER] Erreur lors de la récupération des statistiques: {}", e.getMessage());

            // Retourner des statistiques basiques en cas d'erreur
            Map<String, Object> statsFallback = new HashMap<>();
            statsFallback.put("totalMembres", 0);
            statsFallback.put("totalFamilles", 0);
            statsFallback.put("membresSansFamille", 0);
            statsFallback.put("membresAvecFamille", 0);
            statsFallback.put("moyenneMembresParFamille", 0.0);
            statsFallback.put("coherent", false);
            statsFallback.put("erreur", e.getMessage());

            return new ResponseEntity<>(statsFallback, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === NOUVELLES MÉTHODES CORRIGÉES POUR LES MEMBRES DISPONIBLES ===

    // 21. CORRECTION : Obtenir les membres disponibles pour une famille spécifique
    @GetMapping("/{familleId}/membres-disponibles")
    public ResponseEntity<List<User>> getMembresDisponiblesPourFamille(
            @PathVariable Long familleId,
            @RequestParam Long gymId) {
        try {
            logger.info("🔍 [CONTROLLER] Récupération des membres disponibles pour famille ID: {} dans gym ID: {}", familleId, gymId);

            List<User> membresDisponibles = familleService.getMembresDisponiblesPourFamille(familleId, gymId);

            // CORRECTION : Transformer les données pour le frontend
            List<User> response = membresDisponibles.stream()
                    .map(membre -> {
                        // Créer un objet User avec seulement les informations nécessaires
                        User user = new User();
                        user.setId(membre.getId());
                        user.setNom(membre.getNom());
                        user.setPrenom(membre.getPrenom());
                        user.setEmail(membre.getEmail());
                        user.setTelephone(membre.getTelephone());
                        user.setGenre(membre.getGenre());
                        user.setFamille(membre.getFamille()); // Garder l'info famille pour le statut
                        return user;
                    })
                    .collect(Collectors.toList());

            logger.info("✅ [CONTROLLER] {} membres disponibles retournés pour famille ID: {}", response.size(), familleId);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            logger.error("❌ [CONTROLLER] Famille non trouvée pour l'ID: {}", familleId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("❌ [CONTROLLER] Erreur lors de la récupération des membres disponibles: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 22. Obtenir les membres sans famille ou dans une famille spécifique
    @GetMapping("/{familleId}/membres-sans-famille-ou-dans-famille")
    public ResponseEntity<List<User>> getMembresSansFamilleOuDansFamille(@PathVariable Long familleId) {
        try {
            logger.info("🔍 [CONTROLLER] Récupération des membres sans famille ou dans famille ID: {}", familleId);

            List<User> membres = familleService.getMembresSansFamilleOuDansFamille(familleId);

            logger.info("✅ [CONTROLLER] {} membres trouvés pour famille ID: {}", membres.size(), familleId);
            return new ResponseEntity<>(membres, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("❌ [CONTROLLER] Erreur lors de la récupération des membres: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 23. Obtenir les statistiques des membres disponibles
    @GetMapping("/{familleId}/stats-membres-disponibles")
    public ResponseEntity<Map<String, Object>> getStatsMembresDisponibles(
            @PathVariable Long familleId,
            @RequestParam Long gymId) {
        try {
            Map<String, Object> stats = familleService.getStatistiquesMembresDisponibles(familleId, gymId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("❌ [CONTROLLER] Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 24. Endpoint de debug pour vérifier les membres disponibles
    @GetMapping("/{familleId}/debug-membres-disponibles")
    public ResponseEntity<Map<String, Object>> debugMembresDisponibles(
            @PathVariable Long familleId,
            @RequestParam Long gymId) {

        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("🔍 [DEBUG] Analyse des membres disponibles pour famille ID: {}", familleId);

            // 1. Récupérer via la méthode normale
            List<User> membresDisponibles = familleService.getMembresDisponiblesPourFamille(familleId, gymId);

            // 2. Récupérer tous les membres pour comparaison
            List<User> tousLesMembres = familleService.getMembresDisponiblesPourFamilleAlternative(familleId, gymId);

            // 3. Analyser les statuts
            Map<String, Long> statuts = tousLesMembres.stream()
                    .collect(Collectors.groupingBy(
                            m -> m.getFamille() == null ? "SANS_FAMILLE" :
                                    m.getFamille().getId().equals(familleId) ? "DANS_CETTE_FAMILLE" : "DANS_AUTRE_FAMILLE",
                            Collectors.counting()
                    ));

            result.put("membresDisponibles_count", membresDisponibles.size());
            result.put("tousLesMembres_count", tousLesMembres.size());
            result.put("statuts", statuts);
            result.put("membresDisponibles_details", membresDisponibles.stream()
                    .map(m -> Map.of(
                            "id", m.getId(),
                            "nom", m.getNom(),
                            "prenom", m.getPrenom(),
                            "famille", m.getFamille() != null ? m.getFamille().getId() : "null"
                    ))
                    .collect(Collectors.toList()));

            logger.info("✅ [DEBUG] Analyse terminée: {}", result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ [DEBUG] Erreur lors de l'analyse: {}", e.getMessage());
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // === MÉTHODES UTILITAIRES ===

    // Méthode de conversion (gardée pour compatibilité)
    private FamilleAvecMembresDTO convertToFamilleAvecMembresDTO(Famille famille) {
        FamilleAvecMembresDTO dto = new FamilleAvecMembresDTO();
        dto.setId(famille.getId());
        dto.setNom(famille.getNom());

        if (famille.getChefFamille() != null) {
            dto.setChefFamilleId(famille.getChefFamille().getId());
            dto.setChefFamilleNomPrenom(famille.getChefFamille().getNom() + " " + famille.getChefFamille().getPrenom());
        }

        if (famille.getMembres() != null && !famille.getMembres().isEmpty()) {
            List<FamilleAvecMembresDTO.MembreSimpleDTO> membresDTOs = famille.getMembres().stream()
                    .map(membre -> {
                        FamilleAvecMembresDTO.MembreSimpleDTO membreDTO = new FamilleAvecMembresDTO.MembreSimpleDTO();
                        membreDTO.setId(membre.getId());
                        membreDTO.setNom(membre.getNom());
                        membreDTO.setPrenom(membre.getPrenom());
                        membreDTO.setTelephone(membre.getTelephone());
                        membreDTO.setEmail(membre.getEmail());
                        membreDTO.setGenre(membre.getGenre() != null ? membre.getGenre().name() : "NON_DEFINI");
                        return membreDTO;
                    })
                    .collect(Collectors.toList());

            dto.setMembres(membresDTOs);
            dto.setMembresId(famille.getMembres().stream()
                    .map(membre -> membre.getId())
                    .collect(Collectors.toList()));
        }

        if (famille.getGym() != null) {
            dto.setGymId(famille.getGym().getId());
        }

        return dto;
    }

    // Méthode utilitaire pour convertir Famille en FamilleDTO
    private FamilleDTO convertToDTO(Famille famille) {
        FamilleDTO dto = new FamilleDTO();
        dto.setId(famille.getId());
        dto.setNom(famille.getNom());

        if (famille.getChefFamille() != null) {
            dto.setChefFamilleId(famille.getChefFamille().getId());
            dto.setChefFamilleNomPrenom(famille.getChefFamille().getNom() + " " + famille.getChefFamille().getPrenom());
        }

        if (famille.getMembres() != null) {
            dto.setMembresId(famille.getMembres().stream()
                    .map(User::getId)
                    .collect(Collectors.toList()));
            dto.setMembreNomPrenoms(famille.getMembres().stream()
                    .map(m -> m.getNom() + " " + m.getPrenom())
                    .collect(Collectors.toList()));
        }

        if (famille.getGym() != null) {
            dto.setGymId(famille.getGym().getId());
        }

        return dto;
    }
}