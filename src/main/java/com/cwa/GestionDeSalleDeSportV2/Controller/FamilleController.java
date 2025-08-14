package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Service.FamilleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@RequestMapping("/api/familles")
public class FamilleController {

    @Autowired
    private final FamilleService familleService;

    public FamilleController(FamilleService familleService) {
        this.familleService = familleService;
    }

    //  1.  Crééation de famille
    @PostMapping("/creer_famille")
    public ResponseEntity<String> creeFamille(@RequestBody FamilleDTO dto) throws AccessDeniedException {
        familleService.creerFamille(dto);
        return new ResponseEntity<>("Famille créer avec succès ", HttpStatus.CREATED);
    }

    //  2.  Consulter la liste des familles
    @GetMapping
    public List<Famille> consulterFamille(){
        return familleService.consulterFamille();
    }

    //  3.  Supprimer une famille
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<String> supprimerFamille(@PathVariable Long id) throws AccessDeniedException {
        familleService.suprimerFamille(id);
        return new ResponseEntity<>("famille supprimer avec succès", HttpStatus.OK);
    }

    //  4.  Resilier abonnement familial
    @PutMapping("/resilier_abonnement/{familleId}")
    public ResponseEntity<String> resilierAbonnementFamilial(Long familleId) throws AccessDeniedException {
        familleService.resilierAbonnementFamilial(familleId);
        return new ResponseEntity<>("Abonnement familial résilier avec succès", HttpStatus.OK);
    }

    //  5.  Mettre à jour le téléphone de référence des membreS d'une famille
    @PutMapping("/upDate/membre/telehone_reference/{familleId}")
    public ResponseEntity<String> mettreAJourTelephoneRefrenceMembre(@PathVariable Long familleId){
        familleService.mettreAJourTelephoneRefrenceMembre(familleId);
        return new ResponseEntity<>(" le Téléphone de référence des membres de cette famille à été mis à jour a avec succès.", HttpStatus.OK);
    }

}
