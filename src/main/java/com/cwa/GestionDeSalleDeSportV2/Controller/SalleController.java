package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.SalleDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Salle;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.SalleRepository;
import com.cwa.GestionDeSalleDeSportV2.Service.SalleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salles")
public class SalleController {

    private final SalleService salleService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public SalleController(SalleService salleService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.salleService = salleService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }


    //  1.  Ajouter salle
    @PostMapping("/ajouter")
    public ResponseEntity<Salle> ajouterSalle(@RequestBody SalleDTO dto){
        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Salle salle = salleService.ajouterSalle(staff.getGym(), dto.getNom());
        return new ResponseEntity<>(salle, HttpStatus.CREATED);
    }

    //  2.  Consulter tout les salle d'un gym
    @GetMapping("/gym")
    public ResponseEntity<List<Salle>> ListerSalleParGym(){
        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        return new ResponseEntity<>(salleService.ListerSalleParGym(staff.getGym()), HttpStatus.OK);
    }

    //  3.  getById controller
    @GetMapping("/{id}")
    public  ResponseEntity<Salle> getSalleById(@PathVariable Long id){
        return new ResponseEntity<>(salleService.getSalleById(id), HttpStatus.OK);
    }

    //  4.  Modifier salle
    @PutMapping("/{id}")
    public ResponseEntity<Salle> madifierSalle(@RequestBody SalleDTO dto ,@PathVariable Long id){
        return new ResponseEntity<>(salleService.modifierSalle(id, dto.getNom()), HttpStatus.CREATED);
    }

    //  5.  Supprimer un salle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimeerSalle(@PathVariable Long id){
        salleService.supprimerSalle(id);
        return ResponseEntity.noContent().build();
    }
}
