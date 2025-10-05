package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/familles")
public class FamilleController {

    @Autowired
    private final FamilleService familleService;



    public FamilleController(FamilleService familleService) {
        this.familleService = familleService;
    }

    // 1. Création de famille
    @PostMapping("/creer_famille")
    public ResponseEntity<String> creeFamille(@RequestBody FamilleDTO dto) {
        try {
            familleService.creerFamille(dto);
            return new ResponseEntity<>("Famille créée avec succès", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }




    // 2. Consulter la liste des familles
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    @GetMapping
    public ResponseEntity<List<Famille>> consulterFamille() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Principal : " + auth.getPrincipal());
        System.out.println("Authorities : " + auth.getAuthorities());



        // ➕ Log du contexte de sécurité
        System.out.println("Principal : " + auth.getPrincipal());
        System.out.println("Authorities : " + auth.getAuthorities());



        try {
            List<Famille> familles = familleService.consulterFamille();
            return new ResponseEntity<>(familles, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
    }


    // 3. Supprimer une famille
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<String> supprimerFamille(@PathVariable Long id) {
        try {
            familleService.supprimerFamille(id);
            return new ResponseEntity<>("Famille supprimée avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 4. Résilier abonnement familial
    @PutMapping("/resilier_abonnement/{familleId}")
    public ResponseEntity<String> resilierAbonnementFamilial(@PathVariable Long familleId) {
        try {
            familleService.resilierAbonnementFamilial(familleId);
            return new ResponseEntity<>("Abonnement familial résilié avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 5. Mettre à jour le téléphone de référence des membres d'une famille
    @PutMapping("/update/membre/telephone_reference/{familleId}")
    public ResponseEntity<String> mettreAJourTelephoneReferenceMembre(@PathVariable Long familleId) {
        try {
            familleService.mettreAJourTelephoneRefrenceMembre(familleId);
            return new ResponseEntity<>("Le téléphone de référence des membres de cette famille a été mis à jour avec succès.", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 6. Récupérer les détails d'une famille par ID
    @GetMapping("/{id}")
    public ResponseEntity<FamilleDTO> getFamilleById(@PathVariable Long id) {
        try {
            FamilleDTO familleDTO = familleService.getFamilleById(id);
            return new ResponseEntity<>(familleDTO, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
    }

    // 7. Modifier une famille
    @PutMapping("/{id}")
    public ResponseEntity<String> modifierFamille(@PathVariable Long id, @RequestBody FamilleDTO dto) {
        try {
            familleService.modifierFamille(id, dto);
            return new ResponseEntity<>("Famille modifiée avec succès", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
