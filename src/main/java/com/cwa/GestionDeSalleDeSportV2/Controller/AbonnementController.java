package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Service.AbonnementService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
public class AbonnementController {

    private final AbonnementService abonnementService;

    public AbonnementController(AbonnementService abonnementService) {
        this.abonnementService = abonnementService;
    }

    // 1.  Ajout 'dun abonnement par le staff
    @PostMapping("/valider")
    public ResponseEntity<String> validerAbonnement(@Valid @RequestBody AbonnementDTO dto) throws MessagingException {
        abonnementService.ajouterAbonnement(dto);
        return new  ResponseEntity<>("Abonnement valider avec succès !", HttpStatus.CREATED);
    }

    // 2.  Affiche tout les abonnement
    @GetMapping
    public ResponseEntity<List<Abonnement>> getAllAbonnement(){
        return ResponseEntity.ok(abonnementService.getAllAbonnement());
    }

    //  3.  Renouvellement de l'abonnement
    @PostMapping("/renouvellement/{id}")
    public ResponseEntity<String> renouvelerAbonnement(@RequestBody RenouvelerAbonnementDTO dto, @PathVariable Long id) throws MessagingException {
        abonnementService.renouvelerAbonnement(id, dto.getAjoutMois(), dto.getNouveauAbonnement());
        return new ResponseEntity<>("Abonnement renouveller avec succès.", HttpStatus.CREATED);
    }

    //  4.  changement de plan d'abonnement (calcul montant à payer)
    @PostMapping("/changement-plan/{id}")
    public ResponseEntity<String> gererChangementPlan(LocalDate dateChangement /* formt "yyyy-MM-dd"*/ ,@PathVariable Long id,
                                                      @RequestParam BigDecimal nouveauAbonnement){
        abonnementService.gererChangementAbonnement(id, nouveauAbonnement,dateChangement);
        return new ResponseEntity<>("Changement reussie.", HttpStatus.CREATED);
    }

    // 5.  Mettre un abonnement en pause
    @PutMapping("/pause/{id}")
    public ResponseEntity<Abonnement> mettreEnPause(int joursAbsence, @PathVariable Long idAbonnement) {
        Abonnement abonnement = abonnementService.mettreEnPause(idAbonnement, joursAbsence);
        return ResponseEntity.ok(abonnement);
    }

    //  6.  Reprendre l'abonnement
    @PutMapping("/reprendre/{id}")
    public ResponseEntity<Abonnement> reprendreAbonnement(@PathVariable Long idAbonnement) {
        Abonnement abonnement = abonnementService.reprendreAbonnement(idAbonnement);
        return ResponseEntity.ok(abonnement);
    }
    //  7.  Mis à jour du statut d'un abonnement
    @PutMapping("/mis-a-jour-statut/{id}")
    public ResponseEntity<String> mettreAJourStatut(@PathVariable Long id){
        abonnementService.mettreAJourStatutAutomatiquement(id);
        return new ResponseEntity<>("Abonnement mis à jour avec succès", HttpStatus.CREATED);
    }

    //  8.  L'historique des abonnements d'un membre
    @GetMapping("/historique/{id}")
    public ResponseEntity<List<Abonnement>> getHistoriqueAbonnementParMembre(@PathVariable Long id){

        List<Abonnement> historique  = abonnementService.getHistoriqueAbonnementParMembre(id);
        return ResponseEntity.ok(historique );
    }

}
