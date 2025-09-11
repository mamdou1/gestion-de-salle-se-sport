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

import java.nio.file.AccessDeniedException;
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
    public ResponseEntity<String> ajouterCasierParStaff(@RequestBody CasierDTO dto) throws AccessDeniedException {
        casierService.AjouterCasier(dto);
        return new ResponseEntity<>("Casier ajouter avec succès.", HttpStatus.CREATED);
    }

    //  2.  Ajouter un casier à un membre dans la salle du staff
    @PostMapping("/assigner")
    public ResponseEntity<String> assignerCasier(@RequestBody AssignerCasierDTO dto) throws AccessDeniedException {
        casierService.assignerCasier(dto);
        return new ResponseEntity<>("Casier assigner avec succès.", HttpStatus.CREATED);
    }

    //  3.  Voir les casiers disponibles dans une salle
    @GetMapping("/disponibles/salle/{salleId}")
    public ResponseEntity<List<Casier>> getCasierDisponibleDansSalle(@PathVariable Long salleId) throws AccessDeniedException {
        return ResponseEntity.ok(casierService.getCasierDisponibleDansSalle(salleId));
    }

    //  4.  Voir tous les casiers (dispo ou occupés) dans une salle
    @GetMapping("/salle/{salleId}")
    public ResponseEntity<List<Casier>> getTousLesCasiersDisponibleDansSalle(@PathVariable Long salleId) throws AccessDeniedException {
        return ResponseEntity.ok(casierService.getTousLesCasiersDisponibleDansSalle(salleId));
    }

    //  5.  Tout les casier disponibles dans le gym
    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<Casier>> getTousCasierDisponibleDansGym(@PathVariable Long gymId) throws AccessDeniedException {
        return ResponseEntity.ok(casierService.getTousCasierDisponibleDansGym(gymId));
    }

    //  6.  getCasierById
    @GetMapping("/{casierId}")
    public ResponseEntity<Casier> getCasierById(@PathVariable Long casierId) throws AccessDeniedException {
        Casier casier = casierService.getCasierById(casierId);

        return new ResponseEntity<>(casier, HttpStatus.OK);
    }

    //  7.  liste Casier
    @GetMapping
    public ResponseEntity<List<Casier>> listeCasier() throws AccessDeniedException {
        List<Casier> casiers = casierService.listeCasier();
        return new ResponseEntity<>(casiers, HttpStatus.OK);
    }
}
