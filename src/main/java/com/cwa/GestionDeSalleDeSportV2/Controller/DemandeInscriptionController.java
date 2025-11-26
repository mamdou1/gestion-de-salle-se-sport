package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.DemandeInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionEnLigneDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Service.DemandeInscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        try {
            demandeInscriptionService.soumettreDemande(gymId, dto);
            return new ResponseEntity<>("Demande soumise avec succès. En attente de validation.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la soumission : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/inscriptin/en-ligne")
    public ResponseEntity<String> inscriptionEnLigne(@Valid @ModelAttribute InscriptionEnLigneDTO dto, @RequestParam(required = false) MultipartFile file) throws IOException {
        try {
            demandeInscriptionService.inscriptionEnLigne(dto, file);
            return new ResponseEntity<>("Inscription en ligne réussie avec succès.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'inscription en ligne : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/attente")
    public ResponseEntity<List<DemandeInscription>> getDemande(){
        try {
            List<DemandeInscription> demandes = demandeInscriptionService.getDemandeNonValide();
            return new ResponseEntity<>(demandes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 🔥 NOUVEL ENDPOINT : Valider une demande d'inscription
    @PostMapping("/valider/{id}")
    public ResponseEntity<String> validerDemandeInscription(@PathVariable Long id) {
        try {
            demandeInscriptionService.validerDemandeInscription(id);
            return new ResponseEntity<>("Demande d'inscription validée avec succès.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la validation : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 🔥 NOUVEL ENDPOINT : Rejeter une demande d'inscription
    @PostMapping("/rejeter/{id}")
    public ResponseEntity<String> rejeterDemandeInscription(@PathVariable Long id) {
        try {
            demandeInscriptionService.rejeterDemandeInscription(id);
            return new ResponseEntity<>("Demande d'inscription rejetée avec succès.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors du rejet : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 🔥 NOUVEL ENDPOINT : Obtenir les détails d'une demande
    @GetMapping("/{id}")
    public ResponseEntity<DemandeInscription> getDemandeById(@PathVariable Long id) {
        try {
            DemandeInscription demande = demandeInscriptionService.getDemandeById(id);
            return new ResponseEntity<>(demande, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 🔥 NOUVEL ENDPOINT : Obtenir toutes les demandes d'un gym
    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<DemandeInscription>> getDemandesByGym(@PathVariable Long gymId) {
        try {
            List<DemandeInscription> demandes = demandeInscriptionService.getDemandesByGym(gymId);
            return new ResponseEntity<>(demandes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 🔥 NOUVEL ENDPOINT : Obtenir les demandes en attente d'un gym
    @GetMapping("/gym/{gymId}/attente")
    public ResponseEntity<List<DemandeInscription>> getDemandesEnAttenteByGym(@PathVariable Long gymId) {
        try {
            List<DemandeInscription> demandes = demandeInscriptionService.getDemandesEnAttenteByGym(gymId);
            return new ResponseEntity<>(demandes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}