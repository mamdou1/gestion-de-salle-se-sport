package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import com.cwa.GestionDeSalleDeSportV2.Service.ListePaimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
public class ListePaimentController {

    private final ListePaimentService ListePaimentService;

    @Autowired
    public ListePaimentController(ListePaimentService ListePaimentService) {
        this.ListePaimentService = ListePaimentService;
    }

    /**
     * Récupère tous les paiements du gym de l'utilisateur connecté.
     * @return Liste de tous les paiements.
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé.
     */
    @GetMapping
    public ResponseEntity<List<ListePaiment>> getAllPaiements() throws AccessDeniedException {
        List<ListePaiment> paiements = ListePaimentService.getAllPaiements();
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    /**
     * Récupère les paiements filtrés par type.
     * @param type Type de paiement (ABONNEMENT, FRAIS_INSCRIPTION, CASIER, VENTE).
     * @return Liste filtrée des paiements.
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ListePaiment>> getPaiementsByType(@PathVariable String type) throws AccessDeniedException {
        List<ListePaiment> paiements = ListePaimentService.getPaiementsByType(type);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    /**
     * Récupère les paiements pour une période spécifique.
     * @param dateDebut Date de début.
     * @param dateFin Date de fin.
     * @return Liste filtrée des paiements.
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé.
     */
    @GetMapping("/periode")
    public ResponseEntity<List<ListePaiment>> getPaiementsByPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) throws AccessDeniedException {
        List<ListePaiment> paiements = ListePaimentService.getPaiementsByPeriode(dateDebut, dateFin);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    /**
     * Calcule le total des paiements pour une période spécifique.
     * @param dateDebut Date de début.
     * @param dateFin Date de fin.
     * @return Montant total des paiements.
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé.
     */
    @GetMapping("/total-periode")
    public ResponseEntity<BigDecimal> getTotalPaiementsByPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) throws AccessDeniedException {
        BigDecimal total = ListePaimentService.getTotalPaiementsByPeriode(dateDebut, dateFin);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    /**
     * Récupère les statistiques de paiements par type.
     * @return Map avec le total par type de paiement.
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé.
     */
    @GetMapping("/statistiques")
    public ResponseEntity<Map<TypePaiement, BigDecimal>> getStatistiquesPaiements() throws AccessDeniedException {
        Map<TypePaiement, BigDecimal> statistiques = ListePaimentService.getStatistiquesPaiements();
        return new ResponseEntity<>(statistiques, HttpStatus.OK);
    }
}