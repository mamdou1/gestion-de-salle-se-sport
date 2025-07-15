package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.AssignerCasierDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CasierDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Casier;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Service.CasierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/casiers")
public class CasierController {

    private final CasierService casierService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public CasierController(CasierService casierService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.casierService = casierService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    //  1.  Ajouter un casier à une salle
    @PostMapping("/ajouter")
    public ResponseEntity<String> ajouterCasierParStaff(@RequestBody CasierDTO dto){

        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        casierService.AjouterCasier(staff,dto.getSalleId(), dto.getNumeroDeCasier(), dto.getPrix());
        return new ResponseEntity<>("Casier ajouter avec succès.", HttpStatus.CREATED);
    }

    //  2.  Ajouter un casier à un membre dans la salle du staff
    @PostMapping("/assigner")
    public ResponseEntity<String> assignerCasier(@RequestBody AssignerCasierDTO dto){

        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();;
        casierService.assignerCasier(staff, dto.getSalleId(), dto.getMembreId(), dto.getPrix());
        return new ResponseEntity<>("Casier assigner avec succès.", HttpStatus.CREATED);
    }

    //  3.  Voir les casiers disponibles dans une salle
    @GetMapping("/disponibles/salle/{salleId}")
    public ResponseEntity<List<Casier>> getCasierDisponibleDansSalle(@PathVariable Long salleId){
        return ResponseEntity.ok(casierService.getCasierDisponibleDansSalle(salleId));
    }

    //  4.  Voir tous les casiers (dispo ou occupés) dans une salle
    @GetMapping("/salle/{salleId}")
    public ResponseEntity<List<Casier>> getTousLesCasiersDisponibleDansSalle(@PathVariable Long salleId){
        return ResponseEntity.ok(casierService.getTousLesCasiersDisponibleDansSalle(salleId));
    }

    //  5.  Tout les casier disponibles dans le gym
    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<Casier>> getTousCasierDisponibleDansGym(@PathVariable Long gymId){
        return ResponseEntity.ok(casierService.getTousCasierDisponibleDansGym(gymId));
    }
}
