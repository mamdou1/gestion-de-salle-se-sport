package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import com.cwa.GestionDeSalleDeSportV2.Service.ListePaimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
public class ListePaimentController {

    private final ListePaimentService listePaimentService;

    @Autowired
    public ListePaimentController(ListePaimentService listePaimentService) {
        this.listePaimentService = listePaimentService;
    }

    @GetMapping
    public ResponseEntity<List<ListePaiment>> getAllPaiements() throws AccessDeniedException {
        System.out.println("GET /api/paiements called");
        List<ListePaiment> paiements = listePaimentService.getAllPaiements();
        System.out.println("Returning payments: " + paiements);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ListePaiment>> getPaiementsByType(@PathVariable String type) throws AccessDeniedException {
        System.out.println("GET /api/paiements/type/" + type + " called");
        List<ListePaiment> paiements = listePaimentService.getPaiementsByType(type);
        System.out.println("Returning payments for type " + type + ": " + paiements);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping("/periode")
    public ResponseEntity<List<ListePaiment>> getPaiementsByPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) throws AccessDeniedException {
        System.out.println("GET /api/paiements/periode called with dateDebut=" + dateDebut + ", dateFin=" + dateFin);
        List<ListePaiment> paiements = listePaimentService.getPaiementsByPeriode(dateDebut, dateFin);
        System.out.println("Returning payments for period: " + paiements);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping("/total-periode")
    public ResponseEntity<BigDecimal> getTotalPaiementsByPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) throws AccessDeniedException {
        System.out.println("GET /api/paiements/total-periode called with dateDebut=" + dateDebut + ", dateFin=" + dateFin);
        BigDecimal total = listePaimentService.getTotalPaiementsByPeriode(dateDebut, dateFin);
        System.out.println("Returning total: " + total);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<TypePaiement, BigDecimal>> getStatistiquesPaiements() throws AccessDeniedException {
        System.out.println("GET /api/paiements/statistiques called");
        Map<TypePaiement, BigDecimal> statistiques = listePaimentService.getStatistiquesPaiements();
        System.out.println("Returning statistiques: " + statistiques);
        return new ResponseEntity<>(statistiques, HttpStatus.OK);
    }
}