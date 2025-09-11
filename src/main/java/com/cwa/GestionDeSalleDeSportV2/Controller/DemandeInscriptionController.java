package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.DemandeInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionEnLigneDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Service.DemandeInscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandeInscriptions")
public class DemandeInscriptionController {

    private final DemandeInscriptionService demandeInscriptionService;

    public DemandeInscriptionController(DemandeInscriptionService demandeInscriptionService) {
        this.demandeInscriptionService = demandeInscriptionService;
    }

    @PostMapping("/soumis/{gymId}")
    public ResponseEntity<String> soumettreDemande(@Valid @RequestBody DemandeInscriptionDTO dto, @PathVariable Long gymId){
//        demandeInscriptionService.soumettreDemande(dto);
//        return new  ResponseEntity<>("Demande soumise avec succès. En attente de validation.", HttpStatus.CREATED);
        try {
            demandeInscriptionService.soumettreDemande(gymId, dto);
            return new ResponseEntity<>("Demande soumise avec succès. En attente de validation.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la soumission : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/inscriptin/en-ligne")
    public ResponseEntity<String> inscriptionEnLigne(@Valid @RequestBody InscriptionEnLigneDTO dto){
        demandeInscriptionService.inscriptionEnLigne(dto);
        return new  ResponseEntity<>("Demande soumise avec succès. En attente de validation.", HttpStatus.CREATED);
//        try {
//            demandeInscriptionService.inscriptionEnLigne(dto);
//            return new ResponseEntity<>("Demande soumise avec succès. En attente de validation.", HttpStatus.CREATED);
//        } catch (Exception e) {
//            return new ResponseEntity<>("Erreur lors de la soumission : " + e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
    }

    @GetMapping("/attente")
    public ResponseEntity<List<DemandeInscription>> getDemande(){
        return new  ResponseEntity<>(demandeInscriptionService.getDemandeNonValide(), HttpStatus.OK);
    }
}
