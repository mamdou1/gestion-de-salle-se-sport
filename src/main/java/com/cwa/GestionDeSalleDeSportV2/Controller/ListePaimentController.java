package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.PaiementDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import com.cwa.GestionDeSalleDeSportV2.Service.ListePaimentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paiements")
public class ListePaimentController {

    private static final Logger logger = LoggerFactory.getLogger(ListePaimentController.class);

    private final ListePaimentService listePaimentService;

    @Autowired
    public ListePaimentController(ListePaimentService listePaimentService) {
        this.listePaimentService = listePaimentService;
        logger.info("ListePaimentController initialized");
    }

    @GetMapping
    public ResponseEntity<List<PaiementDTO>> getAllPaiements() throws AccessDeniedException {
        logger.info("GET /api/paiements called");
        List<ListePaiment> paiements = listePaimentService.getAllPaiements();
        logger.debug("Retrieved {} ListePaiment entities", paiements.size());
        List<PaiementDTO> paiementDTOs = paiements.stream()
                .map(this::mapToPaiementDTO)
                .collect(Collectors.toList());
        logger.debug("Mapped {} ListePaiment to {} PaiementDTOs", paiements.size(), paiementDTOs.size());
        return new ResponseEntity<>(paiementDTOs, HttpStatus.OK);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByType(@PathVariable String type) throws AccessDeniedException {
        logger.info("GET /api/paiements/type/{} called", type);
        try {
            TypePaiement.valueOf(type);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid typePaiement: {}", type);
            throw new IllegalArgumentException("Invalid typePaiement: " + type);
        }
        List<ListePaiment> paiements = listePaimentService.getPaiementsByType(type);
        logger.debug("Retrieved {} ListePaiment entities for type {}", paiements.size(), type);
        List<PaiementDTO> paiementDTOs = paiements.stream()
                .map(this::mapToPaiementDTO)
                .collect(Collectors.toList());
        logger.debug("Mapped {} ListePaiment to {} PaiementDTOs for type {}", paiements.size(), paiementDTOs.size(), type);
        return new ResponseEntity<>(paiementDTOs, HttpStatus.OK);
    }

    @GetMapping("/periode")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) throws AccessDeniedException {
        logger.info("GET /api/paiements/periode called with dateDebut={}, dateFin={}", dateDebut, dateFin);
        List<ListePaiment> paiements = listePaimentService.getPaiementsByPeriode(dateDebut, dateFin);
        logger.debug("Retrieved {} ListePaiment entities for period {} to {}", paiements.size(), dateDebut, dateFin);
        List<PaiementDTO> paiementDTOs = paiements.stream()
                .map(this::mapToPaiementDTO)
                .collect(Collectors.toList());
        logger.debug("Mapped {} ListePaiment to {} PaiementDTOs for period {} to {}", paiements.size(), paiementDTOs.size(), dateDebut, dateFin);
        return new ResponseEntity<>(paiementDTOs, HttpStatus.OK);
    }

    @GetMapping("/total-periode")
    public ResponseEntity<BigDecimal> getTotalPaiementsByPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) throws AccessDeniedException {
        logger.info("GET /api/paiements/total-periode called with dateDebut={}, dateFin={}", dateDebut, dateFin);
        BigDecimal total = listePaimentService.getTotalPaiementsByPeriode(dateDebut, dateFin);
        logger.debug("Returning total: {}", total);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<TypePaiement, BigDecimal>> getStatistiquesPaiements() throws AccessDeniedException {
        logger.info("GET /api/paiements/statistiques called");
        Map<TypePaiement, BigDecimal> statistiques = listePaimentService.getStatistiquesPaiements();
        logger.debug("Returning statistiques: {}", statistiques);
        return new ResponseEntity<>(statistiques, HttpStatus.OK);
    }

    private PaiementDTO mapToPaiementDTO(ListePaiment paiement) {
        logger.debug("Mapping ListePaiment id={} to PaiementDTO", paiement.getId());
        PaiementDTO dto = new PaiementDTO(
                paiement.getId(),
                paiement.getTypePaiement() != null ? paiement.getTypePaiement().name() : null,
                paiement.getDatePaiement(),
                paiement.getMontant(),
                paiement.getModeDePaiement() != null ? paiement.getModeDePaiement().name() : null,
                paiement.getAcheteur() != null ? paiement.getAcheteur().getId() : null,
                paiement.getAcheteur() != null ? paiement.getAcheteur().getNom() : null,
                paiement.getAcheteur() != null ? paiement.getAcheteur().getPrenom() : null,
                paiement.getAcheteur() != null ? paiement.getAcheteur().getTelephone() : null,
                paiement.getAcheteur() != null ? paiement.getAcheteur().getEmail() : null,
                paiement.getDetails(),
                paiement.getReferenceId(),
                paiement.getGym() != null ? paiement.getGym().getNom() : null,
                paiement.getStaffEnregistreur() != null ? paiement.getStaffEnregistreur().getNom() : null,
                paiement.getStaffEnregistreur() != null ? paiement.getStaffEnregistreur().getPrenom() : null
        );
        logger.trace("Created PaiementDTO: id={}, typePaiement={}, montant={}", dto.getId(), dto.getTypePaiement(), dto.getMontant());
        return dto;
    }
}