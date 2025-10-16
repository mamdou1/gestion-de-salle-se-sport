package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleService;
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

import java.util.List;

@RestController
@RequestMapping("/api/familles")
public class FamilleController {

    private static final Logger logger = LoggerFactory.getLogger(FamilleController.class);

    @Autowired
    private final FamilleService familleService;

    public FamilleController(FamilleService familleService) {
        this.familleService = familleService;
    }

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

    // 4. Résilier abonnement familial
    @PutMapping("/resilier_abonnement/{familleId}")
    public ResponseEntity<String> resilierAbonnementFamilial(@PathVariable String familleId) {
        try {
            if (familleId == null || familleId.trim().isEmpty()) {
                logger.error("ID de famille manquant ou vide");
                return new ResponseEntity<>("Erreur : ID de famille manquant ou vide.", HttpStatus.BAD_REQUEST);
            }
            Long id = Long.valueOf(familleId);
            familleService.resilierAbonnementFamilial(id);
            return new ResponseEntity<>("Abonnement familial résilié avec succès", HttpStatus.OK);
        } catch (NumberFormatException e) {
            logger.error("ID de famille invalide : {}", familleId);
            return new ResponseEntity<>("Erreur : ID de famille invalide.", HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            logger.error("Famille non trouvée pour l'ID : {}", familleId);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Erreur inattendue lors de la résiliation de l'abonnement : {}", e.getMessage());
            return new ResponseEntity<>("Erreur inattendue : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 5. Mettre à jour le téléphone de référence des membres d'une famille
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

    // 6. Récupérer les détails d'une famille par ID
    @GetMapping("/{id}")
    public ResponseEntity<FamilleDTO> getFamilleById(@PathVariable Long id) {
        logger.info("Requête GET /api/familles/{} reçue", id);
        FamilleDTO familleDTO = familleService.getFamilleById(id);
        return ResponseEntity.ok(familleDTO);
    }

    // 7. Modifier une famille
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
}