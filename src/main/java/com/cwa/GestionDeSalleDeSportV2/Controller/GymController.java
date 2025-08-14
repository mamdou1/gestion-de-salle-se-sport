package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Service.GymService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<Gym>> ConsulterGymListe(){
        List<Gym> gyms = gymService.ConsulterGymListe();
        return new ResponseEntity<>(gyms, HttpStatus.OK);
    }

    //  2.  getById sur list gym
    @GetMapping("/{gymId}")
    public ResponseEntity<Gym> getGymById(@PathVariable Long gymId){
        Gym gym = gymService.getGymById(gymId);
        return new ResponseEntity<>(gym, HttpStatus.OK);
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
