package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import com.cwa.GestionDeSalleDeSportV2.Service.AbonnementService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/abonnements")
@CrossOrigin(origins = "http://localhost:3000")
public class AbonnementController {

    private final Logger logger = LoggerFactory.getLogger(AbonnementController.class);
    private final AbonnementService abonnementService;

    public AbonnementController(AbonnementService abonnementService) {
        this.abonnementService = abonnementService;
    }

    // === NOUVELLES MÉTHODES DTO POUR ÉVITER STACKOVERFLOW ===

    /**
     * Récupère tous les abonnements en DTO (SANS StackOverflowError)
     */
    @GetMapping("/dto")
    public ResponseEntity<List<AbonnementDTO>> getAllAbonnementsDTO() {
        try {
            logger.info("📥 Requête pour récupérer tous les abonnements via DTO");
            List<AbonnementDTO> abonnements = abonnementService.getAllAbonnementsDTO();
            logger.info("✅ {} abonnements retournés avec succès via DTO", abonnements.size());
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les abonnements avec filtres en DTO
     */
    @GetMapping("/dto/filtres")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsDTOByFilters(
            @RequestParam(required = false) Long gymId,
            @RequestParam(required = false) StatutAbonnement statut,
            @RequestParam(required = false) TypeAbonnements type) {
        try {
            logger.info("📥 Requête filtrée - gymId: {}, statut: {}, type: {}", gymId, statut, type);
            List<AbonnementDTO> abonnements = abonnementService.getAbonnementsDTOByFilters(gymId, statut, type);
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors du filtrage des abonnements via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les abonnements par gym en DTO
     */
    @GetMapping("/dto/gym/{gymId}")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsDTOByGymId(@PathVariable Long gymId) {
        try {
            logger.info("📥 Requête abonnements par gym {}", gymId);
            List<AbonnementDTO> abonnements = abonnementService.getAbonnementsDTOByGymId(gymId);
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements par gym via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les abonnements actifs en DTO
     */
    @GetMapping("/dto/actifs")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsActifsDTO() {
        try {
            logger.info("📥 Requête abonnements actifs via DTO");
            List<AbonnementDTO> abonnements = abonnementService.getAbonnementsActifsDTO();
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements actifs via DTO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un abonnement spécifique par ID en DTO
     */
    @GetMapping("/dto/{abonnementId}")
    public ResponseEntity<AbonnementDTO> getAbonnementDTOById(@PathVariable Long abonnementId) {
        try {
            logger.info("📥 Requête abonnement {} via DTO", abonnementId);
            AbonnementDTO abonnement = abonnementService.getAbonnementDTOById(abonnementId);
            return ResponseEntity.ok(abonnement);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération de l'abonnement {} via DTO", abonnementId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Récupère les abonnements d'un membre en DTO
     */
    @GetMapping("/dto/membre/{membreId}")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsDTOByMembreId(@PathVariable Long membreId) {
        try {
            logger.info("📥 Requête abonnements pour le membre {}", membreId);
            // Cette méthode nécessite d'être ajoutée au service
            List<AbonnementDTO> abonnements = abonnementService.getAbonnementsDTOByMembreId(membreId);
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements du membre {}", membreId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les abonnements d'une famille en DTO
     */
    @GetMapping("/dto/famille/{familleId}")
    public ResponseEntity<List<AbonnementDTO>> getAbonnementsDTOByFamilleId(@PathVariable Long familleId) {
        try {
            logger.info("📥 Requête abonnements pour la famille {}", familleId);
            // Cette méthode nécessite d'être ajoutée au service
            List<AbonnementDTO> abonnements = abonnementService.getAbonnementsDTOByFamilleId(familleId);
            return ResponseEntity.ok(abonnements);
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la récupération des abonnements de la famille {}", familleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === MÉTHODES EXISTANTES CORRIGÉES (gardées pour compatibilité) ===

    /**
     * MÉTHODE DE CONVERSION POUR LA COMPATIBILITÉ
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
        dto.setDateMiseEnPause(abonnement.getDatePauseAbonnement());
        dto.setDateResiliation(abonnement.getDateResiliation());

        // Informations du membre
        if (abonnement.getMembre() != null) {
            dto.setMembreId(abonnement.getMembre().getId());
            dto.setNomMembre(abonnement.getMembre().getNom() + " " + abonnement.getMembre().getPrenom());
        }

        // Informations de la famille
        if (abonnement.getFamille() != null) {
            dto.setFamilleId(abonnement.getFamille().getId());
            dto.setNomFamille(abonnement.getFamille().getNom());
        }

        // Informations du gym
        if (abonnement.getGym() != null) {
            dto.setGymId(abonnement.getGym().getId());
            dto.setNomGym(abonnement.getGym().getNom());
        }

        // Informations du type de service
        if (abonnement.getTypeDeService() != null) {
            dto.setTypeDeServiceId(abonnement.getTypeDeService().getId());
            dto.setNomTypeDeService(abonnement.getTypeDeService().getNom());
        }

        return dto;
    }

    // 1. Ajout d'un abonnement par le staff
    @PostMapping("/ajouter")
    public ResponseEntity<String> validerAbonnement(@Valid @RequestBody AbonnementDTO dto) throws MessagingException, AccessDeniedException {
        abonnementService.ajouterAbonnement(dto);
        return new ResponseEntity<>("Abonnement validé avec succès !", HttpStatus.CREATED);
    }

    // 2. Affiche tous les abonnements - CORRIGÉ : Retourne des DTOs
    @GetMapping
    public ResponseEntity<List<AbonnementDTO>> getAllAbonnement() throws AccessDeniedException {
        try {
            List<Abonnement> abonnements = abonnementService.getAllAbonnement();
            if (abonnements == null || abonnements.isEmpty()){
                return ResponseEntity.noContent().build();
            }

            // Conversion en DTOs
            List<AbonnementDTO> abonnementDTOs = abonnements.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(abonnementDTOs);
        } catch (Exception e) {
            logger.error("💥 Erreur dans getAllAbonnement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. Renouvellement de l'abonnement - CORRIGÉ : Retourne un DTO
    @PutMapping("/renouvellement/{id}")
    public ResponseEntity<AbonnementDTO> renouvelerAbonnement(
            @RequestBody RenouvelerAbonnementDTO dto,
            @PathVariable Long id) throws MessagingException, AccessDeniedException {
        Abonnement abonnement = abonnementService.renouvelerAbonnement(id, dto);
        return ResponseEntity.ok(convertToDTO(abonnement));
    }

    // 4. Changement de plan d'abonnement
    @PostMapping("/changement-plan/{id}")
    public ResponseEntity<String> gererChangementPlan(
            @RequestParam LocalDate dateChangement,
            @PathVariable Long id,
            @RequestParam BigDecimal nouveauAbonnement) throws AccessDeniedException {
        ResponseEntity<String> response = abonnementService.gererChangementAbonnement(id, nouveauAbonnement, dateChangement);
        return response;
    }

    // 5. Mettre un abonnement en pause - CORRIGÉ : Retourne un DTO
    @PutMapping("/pause/{id}")
    public ResponseEntity<AbonnementDTO> mettreEnPause(
            @RequestBody PauseAbonnementDTO dto,
            @PathVariable Long id) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.mettreEnPause(id, dto);
        return ResponseEntity.ok(convertToDTO(abonnement));
    }

    // 6. Résilier un abonnement - CORRIGÉ : Retourne un DTO
    @PutMapping("/resilier/{id}")
    public ResponseEntity<AbonnementDTO> resilierAbonnement(@PathVariable Long id) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.resilierAbonnement(id);
        return ResponseEntity.ok(convertToDTO(abonnement));
    }

    // 7. Reprendre l'abonnement - CORRIGÉ : Retourne un DTO
    @PutMapping("/reprendre/{id}")
    public ResponseEntity<AbonnementDTO> reprendreAbonnement(@PathVariable Long id) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.reprendreAbonnement(id);
        return ResponseEntity.ok(convertToDTO(abonnement));
    }

    // 8. Mis à jour du statut d'un abonnement - CORRIGÉ : Retourne un DTO
    @PutMapping("/mis-a-jour-statut/{id}")
    public ResponseEntity<AbonnementDTO> mettreAJourStatut(@PathVariable Long id) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.mettreAJourStatutAutomatiquement(id);
        return ResponseEntity.ok(convertToDTO(abonnement));
    }

    // 9. L'historique des abonnements d'un membre - CORRIGÉ : Retourne des DTOs
    @GetMapping("/historique/{id}")
    public ResponseEntity<List<AbonnementDTO>> getHistoriqueAbonnementParMembre(@PathVariable Long id) throws AccessDeniedException {
        List<Abonnement> historique = abonnementService.getHistoriqueAbonnementParMembre(id);
        List<AbonnementDTO> historiqueDTOs = historique.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiqueDTOs);
    }

    // 10. Historique pour l'app - CORRIGÉ : Retourne des DTOs
    @GetMapping("/historique")
    public ResponseEntity<List<AbonnementDTO>> getHistoriqueAbonnementParMembreApp() throws AccessDeniedException {
        List<Abonnement> historique = abonnementService.getHistoriqueAbonnementParMembreApp();
        List<AbonnementDTO> historiqueDTOs = historique.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiqueDTOs);
    }

    // 11. Récupérer un abonnement par ID - CORRIGÉ : Retourne un DTO
    @GetMapping("/get_abonnement_by_id/{abonnementId}")
    public ResponseEntity<AbonnementDTO> getAbonnementById(@PathVariable Long abonnementId) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.getAbonnementById(abonnementId);
        return ResponseEntity.ok(convertToDTO(abonnement));
    }

    // 12. Supprimer un abonnement
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerAbonnement(@PathVariable Long id) throws AccessDeniedException {
        abonnementService.supprimerAbonnement(id);
        return new ResponseEntity<>("Abonnement supprimé avec succès.", HttpStatus.OK);
    }

    // === STATISTIQUES ===

    @GetMapping("/nombre/journalier")
    public ResponseEntity<Long> getNombreJournalier() throws AccessDeniedException {
        long count = abonnementService.getNombreAbonnementsJournaliers();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/montant/journalier")
    public ResponseEntity<BigDecimal> getMontantJournalier() throws AccessDeniedException {
        BigDecimal montant = abonnementService.getMontantTotalJournalierAbonnements();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/nombre/hebdomadaire")
    public ResponseEntity<Long> getNombreHebdomadaire() throws AccessDeniedException {
        long count = abonnementService.getNombreAbonnementsHebdomadaires();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/montant/hebdomadaire")
    public ResponseEntity<BigDecimal> getMontantHebdomadaire() throws AccessDeniedException {
        BigDecimal montant = abonnementService.getMontantTotalHebdomadaireAbonnements();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/nombre/mensuel")
    public ResponseEntity<Long> getNombreMensuel() throws AccessDeniedException {
        long count = abonnementService.getNombreAbonnementsMensuels();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/montant/mensuel")
    public ResponseEntity<BigDecimal> getMontantMensuel() throws AccessDeniedException {
        BigDecimal montant = abonnementService.getMontantTotalMensuelAbonnements();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/nombre/annuel")
    public ResponseEntity<Long> getNombreAnnuel() throws AccessDeniedException {
        long count = abonnementService.getNombreAbonnementsAnnuels();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/montant/annuel")
    public ResponseEntity<BigDecimal> getMontantAnnuel() throws AccessDeniedException {
        BigDecimal montant = abonnementService.getMontantTotalAnnuelAbonnements();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/nombre/membre/actif")
    public ResponseEntity<Long> nombreTotalMembreActif() throws AccessDeniedException {
        Long nombre = abonnementService.nombreTotalMembreActif();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/nombre/membre/expirer")
    public ResponseEntity<Long> nombreTotalMembreExpirer() throws AccessDeniedException {
        Long nombre = abonnementService.nombreTotalMembreExpirer();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/nombre/membre/bientot-expirer")
    public ResponseEntity<Long> nombreTotalMembreBientotExpirer() throws AccessDeniedException {
        Long nombre = abonnementService.nombreTotalMembreBientotExpirer();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/prix")
    public ResponseEntity<Double> getPrixAbonnement(
            @RequestParam Long typeDeServiceId,
            @RequestParam String genre) throws AccessDeniedException {
        try {
            Double prix = abonnementService.getPrixAbonnement(typeDeServiceId, genre);
            if (prix == null) {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.ok(prix);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // === MÉTHODES UTILITAIRES POUR LE FRONTEND ===

    /**
     * Endpoint de santé pour vérifier que l'API fonctionne
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ API Abonnements fonctionnelle");
    }

    /**
     * Endpoint pour obtenir les types d'abonnements disponibles
     */
    @GetMapping("/types")
    public ResponseEntity<TypeAbonnements[]> getTypesAbonnements() {
        return ResponseEntity.ok(TypeAbonnements.values());
    }

    /**
     * Endpoint pour obtenir les statuts d'abonnements disponibles
     */
    @GetMapping("/statuts")
    public ResponseEntity<StatutAbonnement[]> getStatutsAbonnements() {
        return ResponseEntity.ok(StatutAbonnement.values());
    }
}