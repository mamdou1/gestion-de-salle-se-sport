package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.EvenementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.EvenementViewDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.EvenementService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    private final EvenementService evenementService;

    @Autowired
    public EvenementController(EvenementService evenementService) {
        this.evenementService = evenementService;
    }


    //  1.  Crée un événement
    @PostMapping("/ajouter")
    public ResponseEntity<EvenementViewDTO> createEvenement(@Valid @RequestBody EvenementDTO dto) throws MessagingException {
        EvenementViewDTO saved = evenementService.createEvenement(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    //  2.  Met à jour un événement
    @PutMapping("/mettre_a_jour/{id}")
    public ResponseEntity<EvenementViewDTO> mettreAJourEvenement(@PathVariable Long id, @Valid @RequestBody EvenementDTO dto) throws MessagingException {
        EvenementViewDTO updated = evenementService.mettreAJourEvenement(id, dto);
        return ResponseEntity.ok(updated);
    }

    //  3.   Supprime un événement
    @DeleteMapping("/spprimer/{id}")
    public ResponseEntity<Void> deleteEvenement(@PathVariable Long id) throws MessagingException {
        evenementService.deleteEvenement(id);
        return ResponseEntity.noContent().build();
    }

    //  4.  Récupère un événement
    @GetMapping("/getById/{id}")
    public ResponseEntity<EvenementViewDTO> getByIdEvenement(@PathVariable Long id) {
        EvenementViewDTO evenement = evenementService.getByIdEvenement(id);
        return ResponseEntity.ok(evenement);
    }

    //  5.  Cette méthode récupère une liste d'événements (Evenement) associés à une salle de sport spécifique
    //      (gymId) et se déroulant dans une plage de dates donnée (start et end).
    //      Elle est conçue pour alimenter un calendrier (ex. : FullCalendar) dans le frontend,
    //      en fournissant les événements pertinents pour une période et une salle données.
    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<EvenementViewDTO>> getEvenementsByGymAndDateRange(
            LocalDateTime start,
            LocalDateTime end,
            @PathVariable Long gymId
    ) {
        List<EvenementViewDTO> evenements = evenementService.getEvenementsByGymAndDateRange(gymId, start, end);
        return ResponseEntity.ok(evenements);
    }
}