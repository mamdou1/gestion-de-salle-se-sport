package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleAbonnementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/famille_abonnements")
@CrossOrigin(origins = "http://localhost:3000")
public class FamilleAbonnementController {

    private static final Logger logger = LoggerFactory.getLogger(FamilleAbonnementController.class);

    private final FamilleAbonnementService familleAbonnementService;
    private final FamilleRepository familleRepository;

    public FamilleAbonnementController(
            FamilleAbonnementService familleAbonnementService,
            FamilleRepository familleRepository) {
        this.familleAbonnementService = familleAbonnementService;
        this.familleRepository = familleRepository;
    }

    // === NOUVELLES MÉTHODES DTO POUR ÉVITER STACKOVERFLOW ===

    /**
     * Récupère tous les abonnements familiaux en DTO (SANS StackOverflowError)
     */
    @GetMapping("/dto")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> getAllAbonnementsFamiliauxDTO() {
        try {
            logger.info("📥 Requête pour récupérer tous les abonnements familiaux via DTO");
            List<AbonnementDTO> abonnements = familleAbonnementService.getAllAbonnementsFamiliauxDTO();
            logger.info("✅ {} abonnements familiaux retournés avec succès via DTO", abonnements.size());
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements familiaux via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les abonnements familiaux actifs en DTO
     */
    @GetMapping("/dto/actifs")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsFamiliauxActifsDTO() {
        try {
            logger.info("📥 Requête abonnements familiaux actifs via DTO");
            List<AbonnementDTO> abonnements = familleAbonnementService.getAbonnementsFamiliauxActifsDTO();
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements familiaux actifs via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les abonnements familiaux par gym en DTO
     */
    @GetMapping("/dto/gym/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsFamiliauxDTOByGymId(@PathVariable Long gymId) {
        try {
            logger.info("📥 Requête abonnements familiaux par gym {} via DTO", gymId);
            List<AbonnementDTO> abonnements = familleAbonnementService.getAbonnementsFamiliauxDTOByGymId(gymId);
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements familiaux par gym via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === MÉTHODES EXISTANTES CORRIGÉES ===

    /**
     * MÉTHODE DE CONVERSION CORRIGÉE - Utilise les champs RÉELS de l'entité
     */
    /**
     * MÉTHODE DE CONVERSION CORRIGÉE - Avec type de service
     */
    private AbonnementDTO convertToDTO(Abonnement abonnement) {
        AbonnementDTO dto = new AbonnementDTO();
        dto.setId(abonnement.getId());
        dto.setPeriodAbonnement(abonnement.getPeriodAbonnement());
        dto.setTypes(abonnement.getTypes());
        dto.setNombreDeMois(abonnement.getNombreDeMois());
        dto.setPrixAbonnement(abonnement.getPrixAbonnement());
        dto.setModeDePaiement(abonnement.getModeDePaiement());
        dto.setStatut(abonnement.getStatut());
        dto.setDateDebutAbonnement(abonnement.getDateDebutAbonnement());
        dto.setDateFinAbonnement(abonnement.getDateFinAbonnement());
        dto.setDateRappelFinAbonnement(abonnement.getDateRappelFinAbonnement());

        // 🔥 CORRECTION : Utiliser le champ RÉEL datePauseAbonnement
        dto.setDateMiseEnPause(abonnement.getDatePauseAbonnement());

        dto.setDateResiliation(abonnement.getDateResiliation());

        // Pour les abonnements individuels
        if (abonnement.getMembre() != null) {
            dto.setMembreId(abonnement.getMembre().getId());
            dto.setNomMembre(abonnement.getMembre().getNom() + " " + abonnement.getMembre().getPrenom());
        }

        // Pour les abonnements familiaux
        if (abonnement.getFamille() != null) {
            dto.setFamilleId(abonnement.getFamille().getId());
            dto.setNomFamille(abonnement.getFamille().getNom());

            // Optionnel : Afficher le chef de famille comme "membre principal"
            if (abonnement.getFamille().getChefFamille() != null && abonnement.getMembre() == null) {
                dto.setNomMembre("Famille " + abonnement.getFamille().getNom());
            }
        }

        // Informations du gym
        if (abonnement.getGym() != null) {
            dto.setGymId(abonnement.getGym().getId());
            dto.setNomGym(abonnement.getGym().getNom());
        }

        // 🔥 AJOUT : Informations du type de service
        if (abonnement.getTypeDeService() != null) {
            dto.setTypeDeServiceId(abonnement.getTypeDeService().getId());
            dto.setNomTypeDeService(abonnement.getTypeDeService().getNom());
        }

        return dto;
    }

    // === GESTION DES ABONNEMENTS FAMILIAUX ===

    // 1. Créer un abonnement familial - CORRIGÉ
    @PostMapping("/cree_abonnement")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<?> creerAbonnementFamilial(@RequestBody FamilleAbonnementDTO dto) {
        try {
            logger.info("🎯 === DÉBUT CREATION ABONNEMENT FAMILIAL ===");
            logger.info("📥 DTO REÇU: {}", dto.toString());

            // 🔥 SUPPRIMER la vérification du gymId
            // if (dto.getGymId() != null) {
            //     logger.warn("⚠️ gymId fourni dans le DTO sera ignoré - utilisation du gym de l'utilisateur connecté");
            // }

            // 🔥 VALIDATION MANUELLE RENFORCÉE (sans gymId)
            if (dto.getFamilleId() == null) {
                logger.error("❌ familleId est null");
                return ResponseEntity.badRequest().body(Map.of("message", "L'ID de la famille est obligatoire"));
            }

            if (dto.getTarifHomme() == null || dto.getTarifHomme().compareTo(BigDecimal.ZERO) < 0) {
                logger.error("❌ tarifHomme invalide: {}", dto.getTarifHomme());
                return ResponseEntity.badRequest().body(Map.of("message", "Le tarif pour homme est invalide"));
            }

            if (dto.getTarifFemme() == null || dto.getTarifFemme().compareTo(BigDecimal.ZERO) < 0) {
                logger.error("❌ tarifFemme invalide: {}", dto.getTarifFemme());
                return ResponseEntity.badRequest().body(Map.of("message", "Le tarif pour femme est invalide"));
            }

            if (dto.getNombreMois() == null || dto.getNombreMois().compareTo(BigDecimal.ONE) < 0) {
                logger.error("❌ nombreMois invalide: {}", dto.getNombreMois());
                return ResponseEntity.badRequest().body(Map.of("message", "Le nombre de mois doit être au moins 1"));
            }

            if (dto.getModeDePaiement() == null) {
                logger.error("❌ modeDePaiement est null");
                return ResponseEntity.badRequest().body(Map.of("message", "Le mode de paiement est obligatoire"));
            }

            if (dto.getPeriodAbonnement() == null) {
                logger.error("❌ periodAbonnement est null");
                return ResponseEntity.badRequest().body(Map.of("message", "La période d'abonnement est obligatoire"));
            }

            logger.info("✅ Validation DTO réussie");

            // Appel du service
            List<Abonnement> abonnements = familleAbonnementService.creerAbonnementFamilialEtRetourner(dto);
            List<AbonnementDTO> abonnementDTOs = abonnements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            logger.info("✅ Abonnement familial créé avec succès - {} abonnements générés", abonnementDTOs.size());

            return new ResponseEntity<>(abonnementDTOs, HttpStatus.CREATED);

        } catch (EntityNotFoundException e) {
            logger.error("❌ Ressource non trouvée: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ressource non trouvée: " + e.getMessage()));

        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur de validation: " + e.getMessage()));

        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Accès refusé: " + e.getMessage()));

        } catch (MessagingException e) {
            logger.error("❌ Erreur d'envoi d'email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de l'envoi des notifications"));

        } catch (Exception e) {
            logger.error("💥 ERREUR INATTENDUE lors de la création: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur interne: " + e.getMessage()));
        }
    }

    // 🔥 ENDPOINT DE DEBUG TEMPORAIRE
    @PostMapping("/debug-creation")
    public ResponseEntity<?> debugCreation(@RequestBody Map<String, Object> rawData) {
        try {
            logger.info("🧪 === MODE DEBUG ===");
            logger.info("📥 DONNÉES BRUTES REÇUES:");

            for (Map.Entry<String, Object> entry : rawData.entrySet()) {
                logger.info("   🔹 {}: {} (type: {})",
                        entry.getKey(), entry.getValue(),
                        entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
            }

            // Conversion manuelle pour debug
            FamilleAbonnementDTO dto = new FamilleAbonnementDTO();

            // Conversion avec gestion d'erreur
            try {
                dto.setFamilleId(Long.valueOf(rawData.get("familleId").toString()));
                dto.setPeriodAbonnement(PeriodAbonnement.valueOf(rawData.get("periodAbonnement").toString()));
                dto.setTarifHomme(new BigDecimal(rawData.get("tarifHomme").toString()));
                dto.setTarifFemme(new BigDecimal(rawData.get("tarifFemme").toString()));
                dto.setReductionParPersonne(new BigDecimal(rawData.get("reductionParPersonne").toString()));
                dto.setNombreMois(new BigDecimal(rawData.get("nombreMois").toString()));
                dto.setModeDePaiement(ModeDePaiement.valueOf(rawData.get("modeDePaiement").toString()));

                if (rawData.containsKey("typeDeServiceId")) {
                    dto.setTypeDeServiceId(Long.valueOf(rawData.get("typeDeServiceId").toString()));
                }



            } catch (Exception conversionError) {
                logger.error("❌ Erreur conversion: {}", conversionError.getMessage());
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Erreur conversion: " + conversionError.getMessage()));
            }

            logger.info("✅ DTO CONVERTI: {}", dto.toString());
            logger.info("✅ DTO VALIDE: {}", dto.estValide());

            return ResponseEntity.ok(Map.of(
                    "message", "Debug réussi",
                    "dto", dto.toString(),
                    "estValide", dto.estValide()
            ));

        } catch (Exception e) {
            logger.error("💥 ERREUR DEBUG: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur debug: " + e.getMessage()));
        }
    }

    // 2. Souscrire un abonnement familial - CORRIGÉ
    @PostMapping("/souscrire")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> souscrireAbonnementFamilial(@RequestBody FamilleAbonnementDTO dto) {
        try {
            logger.info("📥 Souscription d'abonnement familial pour la famille {}", dto.getFamilleId());
            List<Abonnement> abonnements = familleAbonnementService.creerAbonnementFamilialEtRetourner(dto);
            List<AbonnementDTO> abonnementDTOs = abonnements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("✅ Abonnement familial souscrit avec succès - {} abonnements générés", abonnementDTOs.size());
            return new ResponseEntity<>(abonnementDTOs, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", dto.getFamilleId());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        } catch (MessagingException e) {
            logger.error("❌ Erreur d'envoi d'email : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la souscription : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 3. Résilier un abonnement familial
    @PutMapping("/resilier/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> resilierAbonnement(@PathVariable Long familleId) {
        try {
            logger.info("📥 Résiliation d'abonnement familial pour la famille {}", familleId);
            familleAbonnementService.resilierAbonnement(familleId);
            logger.info("✅ Abonnement familial résilié avec succès");
            return new ResponseEntity<>("Abonnement familial résilié avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la résiliation : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la résiliation : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Renouveler un abonnement familial
    @PutMapping("/renouveler/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> renouvelerAbonnement(
            @PathVariable Long familleId,
            @RequestBody FamilleAbonnementDTO dto) {
        try {
            logger.info("📥 Renouvellement d'abonnement familial pour la famille {}", familleId);
            familleAbonnementService.renouvelerAbonnementFamilial(familleId, dto);
            logger.info("✅ Abonnement familial renouvelé avec succès");
            return new ResponseEntity<>("Abonnement familial renouvelé avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (MessagingException e) {
            logger.error("❌ Erreur d'envoi d'email : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de l'envoi des notifications", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors du renouvellement : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors du renouvellement : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 5. Obtenir le statut de l'abonnement d'une famille
    @GetMapping("/statut/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<AbonnementDTO> getStatutAbonnement(@PathVariable Long familleId) {
        try {
            logger.info("📥 Récupération du statut d'abonnement pour la famille {}", familleId);
            AbonnementDTO abonnement = familleAbonnementService.getStatutAbonnement(familleId);
            return new ResponseEntity<>(abonnement, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération du statut : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 6. Obtenir l'historique des abonnements d'une famille
    @GetMapping("/historique/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> getHistoriqueAbonnements(@PathVariable Long familleId) {
        try {
            logger.info("📥 Récupération de l'historique des abonnements pour la famille {}", familleId);
            List<AbonnementDTO> historique = familleAbonnementService.getHistoriqueAbonnements(familleId);
            return new ResponseEntity<>(historique, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération de l'historique : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 7. Mettre à jour un abonnement existant
    @PutMapping("/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> mettreAJourAbonnement(
            @PathVariable Long familleId,
            @RequestBody FamilleAbonnementDTO dto) {
        try {
            logger.info("📥 Mise à jour d'abonnement familial pour la famille {}", familleId);
            familleAbonnementService.mettreAJourAbonnement(familleId, dto);
            logger.info("✅ Abonnement familial mis à jour avec succès");
            return new ResponseEntity<>("Abonnement mis à jour avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la mise à jour : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la mise à jour : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 8. Vérifier si une famille a un abonnement actif
    @GetMapping("/est-actif/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Boolean> estAbonnementActif(@PathVariable Long familleId) {
        try {
            logger.info("📥 Vérification d'abonnement actif pour la famille {}", familleId);
            boolean estActif = familleAbonnementService.estAbonnementActif(familleId);
            return new ResponseEntity<>(estActif, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la vérification : {}", e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 9. Obtenir la date d'expiration de l'abonnement
    @GetMapping("/date-expiration/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> getDateExpiration(@PathVariable Long familleId) {
        try {
            logger.info("📥 Récupération de la date d'expiration pour la famille {}", familleId);
            String dateExpiration = familleAbonnementService.getDateExpiration(familleId);
            return new ResponseEntity<>(dateExpiration, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération de la date d'expiration : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la récupération", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === STATISTIQUES DES ABONNEMENTS ===

    // 10. Obtenir le nombre total d'abonnements familiaux actifs
    @GetMapping("/stats/abonnements-actifs")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Long> getNombreAbonnementsActifs() {
        try {
            logger.info("📥 Récupération du nombre d'abonnements familiaux actifs");
            long count = familleAbonnementService.getNombreAbonnementsActifs();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors du comptage des abonnements actifs : {}", e.getMessage());
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 11. Obtenir les abonnements expirant bientôt
    @GetMapping("/stats/expirant-bientot")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsExpirantBientot() {
        try {
            logger.info("📥 Récupération des abonnements familiaux expirant bientôt");
            List<AbonnementDTO> abonnements = familleAbonnementService.getAbonnementsExpirantBientot();
            return new ResponseEntity<>(abonnements, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération des abonnements expirant bientôt : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === ENDPOINTS POUR LA GESTION DES PAUSES ET REPRISES ===

    // 12. Mettre un abonnement familial en pause
    @PutMapping("/pause/{abonnementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> mettreEnPause(@PathVariable Long abonnementId) {
        try {
            logger.info("📥 Mise en pause de l'abonnement familial {}", abonnementId);
            familleAbonnementService.mettreEnPause(abonnementId);
            logger.info("✅ Abonnement familial mis en pause avec succès");
            return new ResponseEntity<>("Abonnement familial mis en pause avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Abonnement non trouvé pour l'ID : {}", abonnementId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la mise en pause : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la mise en pause : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 13. Reprendre un abonnement familial en pause
    @PutMapping("/reprendre/{abonnementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> reprendreAbonnement(@PathVariable Long abonnementId) {
        try {
            logger.info("📥 Reprise de l'abonnement familial {}", abonnementId);
            familleAbonnementService.reprendreAbonnement(abonnementId);
            logger.info("✅ Abonnement familial repris avec succès");
            return new ResponseEntity<>("Abonnement familial repris avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Abonnement non trouvé pour l'ID : {}", abonnementId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur de validation : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            logger.error("❌ Accès refusé : {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la reprise : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la reprise : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 14. Mettre à jour automatiquement le statut d'un abonnement
    @PutMapping("/mis-a-jour-statut/{abonnementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> mettreAJourStatutAutomatiquement(@PathVariable Long abonnementId) {
        try {
            logger.info("📥 Mise à jour automatique du statut de l'abonnement {}", abonnementId);
            familleAbonnementService.mettreAJourStatutAutomatiquement(abonnementId);
            logger.info("✅ Statut de l'abonnement mis à jour avec succès");
            return new ResponseEntity<>("Statut de l'abonnement mis à jour avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Abonnement non trouvé pour l'ID : {}", abonnementId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la mise à jour du statut : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la mise à jour du statut : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === ENDPOINTS EXISTANTS POUR LA COMPATIBILITÉ ===

    // 15. Obtenir tous les abonnements familiaux (compatibilité)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> getAllAbonnementsFamiliaux() {
        try {
            logger.info("📥 Récupération de tous les abonnements familiaux (compatibilité)");
            List<AbonnementDTO> abonnements = familleAbonnementService.getAllAbonnementsFamiliaux();
            return new ResponseEntity<>(abonnements, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération des abonnements familiaux : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 16. Obtenir les abonnements familiaux par gym (compatibilité)
    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsFamiliauxParGym(@PathVariable Long gymId) {
        try {
            logger.info("📥 Récupération des abonnements familiaux par gym {} (compatibilité)", gymId);
            List<AbonnementDTO> abonnements = familleAbonnementService.getAbonnementsFamiliauxParGym(gymId);
            return new ResponseEntity<>(abonnements, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération des abonnements familiaux par gym : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 17. Obtenir les statistiques des abonnements familiaux
    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAbonnementsFamiliaux() {
        try {
            logger.info("📥 Récupération des statistiques des abonnements familiaux");
            Map<String, Object> statistiques = familleAbonnementService.getStatistiquesAbonnementsFamiliaux();
            return new ResponseEntity<>(statistiques, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération des statistiques : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 18. Vérifier et notifier les abonnements expirés
    @PostMapping("/verifier-expirations")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<String> verifierEtNotifierAbonnementsExpires() {
        try {
            logger.info("📥 Vérification et notification des abonnements expirés");
            familleAbonnementService.verifierEtNotifierAbonnementsExpires();
            logger.info("✅ Vérification des abonnements expirés terminée");
            return new ResponseEntity<>("Vérification des abonnements expirés terminée", HttpStatus.OK);
        } catch (MessagingException e) {
            logger.error("❌ Erreur d'envoi d'email lors de la vérification : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de l'envoi des notifications", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la vérification : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la vérification : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 19. Supprimer un abonnement familial
    @DeleteMapping("/{abonnementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<String> supprimerAbonnement(@PathVariable Long abonnementId) {
        try {
            logger.info("📥 Suppression de l'abonnement familial {}", abonnementId);
            familleAbonnementService.supprimerAbonnement(abonnementId);
            logger.info("✅ Abonnement familial supprimé avec succès");
            return new ResponseEntity<>("Abonnement familial supprimé avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Abonnement non trouvé pour l'ID : {}", abonnementId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la suppression : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la suppression : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 20. Obtenir les abonnements familiaux expirés
    @GetMapping("/expires")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsExpires() {
        try {
            logger.info("📥 Récupération des abonnements familiaux expirés");
            List<AbonnementDTO> abonnements = familleAbonnementService.getAbonnementsExpirantBientot();
            return new ResponseEntity<>(abonnements, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération des abonnements expirés : {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 21. Mettre à jour les statuts d'une famille
    @PutMapping("/mettre-a-jour-statuts/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<String> mettreAJourStatutsFamille(@PathVariable Long familleId) {
        try {
            logger.info("📥 Mise à jour des statuts pour la famille {}", familleId);
            familleAbonnementService.mettreAJourStatutsFamille(familleId);
            logger.info("✅ Statuts mis à jour avec succès");
            return new ResponseEntity<>("Statuts mis à jour avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la mise à jour des statuts : {}", e.getMessage());
            return new ResponseEntity<>("Erreur lors de la mise à jour des statuts : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === ENDPOINTS UTILITAIRES ===

    /**
     * Endpoint de santé pour vérifier que l'API fonctionne
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ API Abonnements Familiaux fonctionnelle");
    }

    /**
     * Endpoint pour obtenir les types d'abonnements familiaux disponibles
     */
    @GetMapping("/types")
    public ResponseEntity<String[]> getTypesAbonnements() {
        String[] types = {"FAMILIALE"};
        return ResponseEntity.ok(types);
    }

    /**
     * Récupérer les membres d'une famille (CORRIGÉ - sans erreur de type)
     */
    @GetMapping("/famille/{familleId}/membres")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<?> getMembresFamille(@PathVariable Long familleId) {
        try {
            logger.info("📥 Récupération des membres de la famille {}", familleId);

            Famille famille = familleRepository.findById(familleId)
                    .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

            // 🔥 CORRECTION : Utiliser une liste simple d'objets
            List<Object> membresSimplifies = famille.getMembres().stream()
                    .map(membre -> {
                        Map<String, Object> membreMap = new HashMap<>();
                        membreMap.put("id", membre.getId());
                        membreMap.put("nom", membre.getNom());
                        membreMap.put("prenom", membre.getPrenom());
                        membreMap.put("genre", membre.getGenre().toString()); // Convertir enum en String
                        membreMap.put("email", membre.getEmail());
                        return membreMap;
                    })
                    .collect(Collectors.toList());

            // 🔥 CORRECTION : Créer une Map simple
            Map<String, Object> response = new HashMap<>();
            response.put("familleId", familleId);
            response.put("nomFamille", famille.getNom());
            response.put("membres", membresSimplifies);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur récupération membres famille: {}", e.getMessage());

            // 🔥 CORRECTION : Retour d'erreur simple
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}