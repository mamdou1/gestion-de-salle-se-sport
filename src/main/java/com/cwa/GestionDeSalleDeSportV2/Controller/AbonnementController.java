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
    @PostMapping("/ajouter")
    public ResponseEntity<String> validerAbonnement(@Valid @RequestBody AbonnementDTO dto) throws MessagingException, AccessDeniedException {
        abonnementService.ajouterAbonnement(dto);
        return new  ResponseEntity<>("Abonnement valider avec succès !", HttpStatus.CREATED);
    }

    // 2.  Affiche tout les abonnement
    @GetMapping
    public ResponseEntity<List<Abonnement>> getAllAbonnement() throws AccessDeniedException {
        List<Abonnement> abonnements = abonnementService.getAllAbonnement();
        if (abonnements == null || abonnements.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(abonnements);
    }

    //  3.  Renouvellement de l'abonnement
    @PutMapping("/renouvellement/{id}")
    public ResponseEntity<String> renouvelerAbonnement(@RequestBody RenouvelerAbonnementDTO dto, @PathVariable Long id) throws MessagingException, AccessDeniedException {
        abonnementService.renouvelerAbonnement(id, dto);
        return new ResponseEntity<>("Abonnement renouveller avec succès.", HttpStatus.CREATED);
    }

    //  4.  changement de plan d'abonnement (calcul montant à payer)
    @PostMapping("/changement-plan/{id}")
    public ResponseEntity<String> gererChangementPlan(LocalDate dateChangement /* formt "yyyy-MM-dd"*/ ,@PathVariable Long id,
                                                      @RequestParam BigDecimal nouveauAbonnement) throws AccessDeniedException {
        abonnementService.gererChangementAbonnement(id, nouveauAbonnement,dateChangement);
        return new ResponseEntity<>("Changement reussie.", HttpStatus.CREATED);
    }

    // 5.  Mettre un abonnement en pause
    @PutMapping("/pause/{id}")
    public ResponseEntity<Abonnement> mettreEnPause(@RequestBody PauseAbonnementDTO dto, @PathVariable Long idAbonnement) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.mettreEnPause(idAbonnement, dto);
        return ResponseEntity.ok(abonnement);
    }

    @PutMapping("/resilier/{id}")
    public ResponseEntity<String> resilierAbonnement(@PathVariable Long id) throws AccessDeniedException {
        abonnementService.resilierAbonnement(id);
        return new ResponseEntity<>("l'abonnement à été resilier avec succès.", HttpStatus.OK);
    }

    //  6.  Reprendre l'abonnement
    @PutMapping("/reprendre/{id}")
    public ResponseEntity<Abonnement> reprendreAbonnement(@PathVariable Long idAbonnement) throws AccessDeniedException {
        Abonnement abonnement = abonnementService.reprendreAbonnement(idAbonnement);
        return ResponseEntity.ok(abonnement);
    }
    //  7.  Mis à jour du statut d'un abonnement
    @PutMapping("/mis-a-jour-statut/{id}")
    public ResponseEntity<String> mettreAJourStatut(@PathVariable Long id) throws AccessDeniedException {
        abonnementService.mettreAJourStatutAutomatiquement(id);
        return new ResponseEntity<>("Abonnement mis à jour avec succès", HttpStatus.CREATED);
    }

    //  8.  L'historique des abonnements d'un membre
    @GetMapping("/historique/{id}")
    public ResponseEntity<List<Abonnement>> getHistoriqueAbonnementParMembre(@PathVariable Long id) throws AccessDeniedException {

        List<Abonnement> historique  = abonnementService.getHistoriqueAbonnementParMembre(id);
        return ResponseEntity.ok(historique );
    }

    @GetMapping("/historique")
    public ResponseEntity<List<Abonnement>> getHistoriqueAbonnementParMembreApp() throws AccessDeniedException {
        List<Abonnement> historique = abonnementService.getHistoriqueAbonnementParMembreApp();
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/get_abonnement_by_id/{abonnementId}")
    public ResponseEntity<Abonnement> getAbonnementById(@PathVariable Long abonnementId) throws AccessDeniedException {
        return ResponseEntity.ok(abonnementService.getAbonnementById(abonnementId));
    }

    //  9.  Supprimer un membre
    @DeleteMapping
    public ResponseEntity<String> supprimerAbonnement(@PathVariable Long membreId) throws AccessDeniedException {
        abonnementService.supprimerAbonnement(membreId);
        return new ResponseEntity<>("Membre supprimer avec succès.", HttpStatus.OK);
    }


    // 🔹 JOURNALIER
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

    // 🔹 HEBDOMADAIRE
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

    // 🔹 MENSUEL
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

    // 🔹 ANNUEL
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
            // Logique pour récupérer le prix (exemple simplifié)
            Double prix = abonnementService.getPrixAbonnement(typeDeServiceId, genre);
            if (prix == null) {
                return ResponseEntity.badRequest().body(null); // Retourne null si pas de prix trouvé
            }
            return ResponseEntity.ok(prix);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Gestion des erreurs serveur
        }
    }

}
