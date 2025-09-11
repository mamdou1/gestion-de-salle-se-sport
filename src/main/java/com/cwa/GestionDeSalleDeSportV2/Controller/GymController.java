package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.StaffDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.staffsDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Service.GymService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/gyms")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    //  1.  Consulter les gym
    @GetMapping
    public ResponseEntity<List<Gym>> ConsulterGymListe() throws AccessDeniedException {
        List<Gym> gyms = gymService.ConsulterGymListe();
        return new ResponseEntity<>(gyms, HttpStatus.OK);
    }

    //  2.  getById sur list gym
    @GetMapping("/{gymId}")
    public ResponseEntity<Gym> getGymById(@PathVariable Long gymId) throws AccessDeniedException {
        Gym gym = gymService.getGymById(gymId);
        return new ResponseEntity<>(gym, HttpStatus.OK);
    }

    //  3.  Ajouter un membre à l'éauipe technique
    @PostMapping("/ajouter/equipe")
    public ResponseEntity<String> ajouterMembreEquipeTech(@RequestBody staffsDTO dto) throws AccessDeniedException, MessagingException {
        gymService.ajouterUnMembreEquipeTech(dto);
        return new ResponseEntity<>("Membre ajouter avec succès à l'équipe techmique", HttpStatus.CREATED);
    }

    @GetMapping("/membres/nombre")
    public ResponseEntity<Map<Long, Long>> getNombreMembresParGym() throws AccessDeniedException {
        Map<Long, Long> result = gymService.getNombreMembresParGym();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/membres/par-salle-statut")
    public ResponseEntity<Map<Long, Map<StatutAbonnement, Long>>> getNombreParStatutEtGym() throws AccessDeniedException{
        Map<Long, Map<StatutAbonnement, Long>> result = gymService.getNombreMembresParStatutEtGym();
        return ResponseEntity.ok(result);
    }

}
