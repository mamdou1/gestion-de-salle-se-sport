package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingUpdateDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CoachingViewDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.CoachingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/coachings")
public class CoachingController {

    private final CoachingService coachingService;

    @Autowired
    public CoachingController(CoachingService coachingService) {
        this.coachingService = coachingService;
    }

    //  1.  Crée une session
    @PostMapping("/ajouter")
    public ResponseEntity<CoachingViewDTO> createCoaching(@Valid @RequestBody CoachingDTO dto) {
        CoachingViewDTO saved = coachingService.createCoaching(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    //  2.  Met à jour une session
    @PutMapping("/mettre_a-jour/{id}")
    public ResponseEntity<CoachingViewDTO> mettreAJourCoaching(@PathVariable Long id, @Valid @RequestBody CoachingUpdateDTO dto) {
        CoachingViewDTO updated = coachingService.mettreAJourCoaching(id, dto);
        return ResponseEntity.ok(updated);
    }

    //  3.  Supprime une session
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoaching(@PathVariable Long id) {
        coachingService.deleteCoaching(id);
        return ResponseEntity.noContent().build();
    }

    //  4.  Récupère une session
    @GetMapping("/{id}")
    public ResponseEntity<CoachingViewDTO> getByIdCoaching(@PathVariable Long id) {
        CoachingViewDTO coaching = coachingService.getByIdCoaching(id);
        return ResponseEntity.ok(coaching);
    }

    //  5.  Cette méthode récupère une liste de coaching (Coaching) associés à une salle de sport spécifique
    //      (gymId) et se déroulant dans une plage de dates donnée (start et end).
    //      Elle est conçue pour alimenter un calendrier (ex. : FullCalendar) dans le frontend,
    //      en fournissant les coaching pertinents pour une période et une salle données.

    @GetMapping("/liste")
    public ResponseEntity<List<CoachingViewDTO>> getCoachingsByGymAndDateRange() {
        List<CoachingViewDTO> coachings = coachingService.getCoachingsByGymAndDateRange();
        return ResponseEntity.ok(coachings);
    }
}