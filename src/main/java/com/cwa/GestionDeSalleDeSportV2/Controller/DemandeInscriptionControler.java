package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.DemandeInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Service.DemandeInscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandeIncscripions")
public class DemandeInscriptionControler {

    private final DemandeInscriptionService demandeInscriptionService;

    public DemandeInscriptionControler(DemandeInscriptionService demandeInscriptionService) {
        this.demandeInscriptionService = demandeInscriptionService;
    }

    @PostMapping("/soumis")
    public ResponseEntity<String> soumettreDemande(@RequestBody DemandeInscriptionDTO dto){
        demandeInscriptionService.soumettreDemande(dto);
        return new  ResponseEntity<>("Demande soumise avec succès. En attente de validation.", HttpStatus.CREATED);
    }

    @GetMapping("/attente")
    public ResponseEntity<List<DemandeInscription>> getDemande(){
        return new  ResponseEntity<>(demandeInscriptionService.getDemandeNonValide(), HttpStatus.CREATED);
    }
}
