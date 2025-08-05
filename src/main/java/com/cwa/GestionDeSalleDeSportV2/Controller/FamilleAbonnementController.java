package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.ConfirmationToastDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleAbonnementService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/famille_abonnements")
public class FamilleAbonnementController {

    @Autowired
    private final FamilleAbonnementService familleAbonnementService;

    public FamilleAbonnementController(FamilleAbonnementService familleAbonnementService) {
        this.familleAbonnementService = familleAbonnementService;
    }

    //  9.  Crée un abonnement familial
    @PostMapping("/cree_abonnement")
    public ResponseEntity<String> creerAbonnementFamille(@Valid @RequestBody FamilleAbonnementDTO dto) throws MessagingException {
        familleAbonnementService.creerAbonnementFamilial(dto);
        return new ResponseEntity<>("Abonnement familial crée avec succès.", HttpStatus.CREATED);
    }

    //  1.  Ajoute un membre à une famille existante par le chef de famille
    @PostMapping("/ajout-par-chef")
    public ResponseEntity<ConfirmationToastDTO> proposerAjout(@RequestBody @Valid MembreDTO dto) throws MessagingException {
        ConfirmationToastDTO toast = familleAbonnementService.ajoutParChefDeFamille(dto);
        return ResponseEntity.ok(toast);
    }

    //  2.  Ajoute un membre à une famille existante par le chef de famille
    @PostMapping("/ajout-par-staff")
    public ResponseEntity<ConfirmationToastDTO> ajouterMembre(@RequestBody @Valid MembreDTO dto) throws MessagingException {
        ConfirmationToastDTO toast = familleAbonnementService.ajoutDirectParStaff(dto);
        return ResponseEntity.ok(toast);
    }
    //  3.  Demande le retrait d'un membre
    @PostMapping("/demande_retrait/{membreId}")
    public ResponseEntity<ConfirmationToastDTO> demanderRetraitMembre(@PathVariable Long membreId) throws AccessDeniedException, MessagingException {
        ConfirmationToastDTO toast = familleAbonnementService.demenderRetraitMembre(membreId);
        return ResponseEntity.ok(toast);
    }

    //  4.  Confirme le retrait d'un membre
    @PutMapping("/confirmer_retrait/{membreId}")
    public ResponseEntity<Void> confirmerRetraitMembre(@PathVariable Long membreId) throws AccessDeniedException, MessagingException {
        familleAbonnementService.confirmerRetraitMembre(membreId);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> annulerRetraitMembre(@PathVariable Long membreId) throws MessagingException {
        familleAbonnementService.annulerRetraitMembre(membreId);
        return ResponseEntity.ok().build();
    }

}
