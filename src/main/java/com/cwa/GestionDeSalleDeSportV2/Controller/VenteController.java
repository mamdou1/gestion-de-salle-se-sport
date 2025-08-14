package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.VenteDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.VenteManuelDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Vente;
import com.cwa.GestionDeSalleDeSportV2.Service.VenteService;
import jakarta.mail.MessagingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/ventes")
public class VenteController {

    private final VenteService venteService;

    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    // 1. Valider un panier et créer une vente
    @PostMapping("/valider-panier/{panierId}")
    public ResponseEntity<Vente> validerPanierEtCreerVente(@RequestBody VenteDTO dto, @PathVariable Long panierId) throws AccessDeniedException, MessagingException {
        Vente vente = venteService.validerPanierEtCreerVente(panierId, dto);
        return new ResponseEntity<>(vente, HttpStatus.CREATED);
    }

    @PostMapping("/enregistrer-manuelle")
    public ResponseEntity<Vente> enregistrerVenteManuelle(@RequestBody VenteManuelDTO dto) throws AccessDeniedException, MessagingException {
        Vente vente = venteService.enregistrerVenteManuelle(dto);
        return new ResponseEntity<>(vente, HttpStatus.CREATED);

    }

    // 2. Lister les ventes
    @GetMapping("/lister")
    public ResponseEntity<List<Vente>> listerVentes() throws AccessDeniedException {
        List<Vente> ventes = venteService.listerVentes();
        return ResponseEntity.ok(ventes);
    }

    //  3. Consulter le detail
    @GetMapping("{venteId}")
    public ResponseEntity<Vente> consulterDetailVente(@PathVariable Long venteId) throws AccessDeniedException {
        Vente vente = venteService.consulterDetailVente(venteId);
        return ResponseEntity.ok(vente);
    }

    @GetMapping("/statistiques/nombre/journalier")
    public ResponseEntity<Long> getNombreVentesJournalieres() throws AccessDeniedException {
        Long nombre = venteService.getNombreVentesJournalieres();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/statistiques/montant/journalier")
    public ResponseEntity<BigDecimal> getMontantTotalJournalier() throws AccessDeniedException {
        BigDecimal montant = venteService.getMontantTotalJournalier();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/statistiques/nombre/hebdomadaire")
    public ResponseEntity<Long> getNombreVentesHebdomadaires() throws AccessDeniedException {
        Long nombre = venteService.getNombreVentesHebdomadaires();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/statistiques/montant/hebdomadaire")
    public ResponseEntity<BigDecimal> getMontantTotalHebdomadaire() throws AccessDeniedException {
        BigDecimal montant = venteService.getMontantTotalHebdomadaire();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/statistiques/nombre/mensuel")
    public ResponseEntity<Long> getNombreVentesMensuelles() throws AccessDeniedException {
        Long nombre = venteService.getNombreVentesMensuelles();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/statistiques/montant/mensuel")
    public ResponseEntity<BigDecimal> getMontantTotalMensuel() throws AccessDeniedException {
        BigDecimal montant = venteService.getMontantTotalMensuel();
        return ResponseEntity.ok(montant);
    }

    @GetMapping("/statistiques/nombre/annuel")
    public ResponseEntity<Long> getNombreVentesAnnuelles() throws AccessDeniedException {
        Long nombre = venteService.getNombreVentesAnnuelles();
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/statistiques/montant/annuel")
    public ResponseEntity<BigDecimal> getMontantTotalAnnuel() throws AccessDeniedException {
        BigDecimal montant = venteService.getMontantTotalAnnuel();
        return ResponseEntity.ok(montant);
    }
}

