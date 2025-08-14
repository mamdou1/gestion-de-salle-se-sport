package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleAbonnementService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/famille_abonnements")
public class FamilleAbonnementController {

    @Autowired
    private final FamilleAbonnementService familleAbonnementService;

    public FamilleAbonnementController(FamilleAbonnementService familleAbonnementService) {
        this.familleAbonnementService = familleAbonnementService;
    }

    //  1.  Crée un abonnement familial
    @PostMapping("/cree_abonnement")
    public ResponseEntity<String> creerAbonnementFamille(@Valid @RequestBody FamilleAbonnementDTO dto) throws MessagingException, AccessDeniedException {
        familleAbonnementService.creerAbonnementFamilial(dto);
        return new ResponseEntity<>("Abonnement familial crée avec succès.", HttpStatus.CREATED);
    }

    //  2.  Ajoute un membre à une famille existante par le chef de famille
    @PostMapping("/ajout-par-chef")
    public ResponseEntity<String> proposerAjout(@RequestBody @Valid MembreDTO dto) throws MessagingException, AccessDeniedException {
        familleAbonnementService.ajoutParChefDeFamille(dto);
        return ResponseEntity.ok("Proposition d'ajout soumise avec succès. Attendez la validation du staff.");
    }

    //  2.1.  Liste des demandes d'ajout de chef de famille
    @GetMapping("/demandes-en-attente")
    public ResponseEntity<List<User>> listeDemandesAjoutParChefDeFamille() throws AccessDeniedException {
        return new ResponseEntity<>(familleAbonnementService.listeDemandesAjoutParChefDeFamille(), HttpStatus.OK);
    }


    //  3.  Ajoute un membre à une famille existante par le staff
    @PostMapping("/ajout-par-staff")
    public ResponseEntity<String> ajouterMembre(@RequestBody @Valid MembreDTO dto) throws MessagingException, AccessDeniedException {
        familleAbonnementService.ajoutDirectParStaff(dto);
        return ResponseEntity.ok("Membre ajouté avec succès.");
    }

    //  4.  Demande le retrait d'un membre
    @PostMapping("/demande_retrait/{membreId}")
    public ResponseEntity<Void> retraitMembre(@PathVariable Long membreId) throws AccessDeniedException, MessagingException {
        familleAbonnementService.retraitMembre(membreId);
        return ResponseEntity.ok().build();
    }

    //  4.1  liste de demandes de rétrait
    @GetMapping
    public ResponseEntity<List<Famille>> listeDemandeRetrait() throws AccessDeniedException {
        return new  ResponseEntity<>(familleAbonnementService.listeDesRetrait(),HttpStatus.OK);
    }

    //  5.  Confirme l' ajout d'un membre
    @PostMapping("/confirmation-ajout-membre/{membreId}")
    public ResponseEntity<String> confirmationAjoutMembre(@PathVariable Long membreId) throws AccessDeniedException, MessagingException {
        familleAbonnementService.confirmationAjoutMembre(membreId);
        return ResponseEntity.ok("Ajout du membre confirmé avec succès.");
    }

    //  6.  Annule l' ajout d'un membre
    @PostMapping("/annulation-ajout-membre/{membreId}")
    public ResponseEntity<String> annulationAjoutMembre(@PathVariable Long membreId) throws AccessDeniedException, MessagingException {
        familleAbonnementService.annulationAjoutMembre(membreId);
        return ResponseEntity.ok("Ajout du membre annulé avec succès.");
    }

    //  7.  Annule le retrait d'un membre
    @PutMapping("/annuler-retrait/{membreId}")
    public ResponseEntity<String> annulerRetraitMembre(@PathVariable Long membreId) throws MessagingException, AccessDeniedException {
        familleAbonnementService.annulerRetraitMembre(membreId);
        return new ResponseEntity<>("Le retrait a été annuler avec succès.", HttpStatus.OK);
    }

}
