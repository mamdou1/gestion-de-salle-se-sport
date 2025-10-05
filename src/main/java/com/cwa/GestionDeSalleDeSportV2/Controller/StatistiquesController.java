package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.Statistiques;
import com.cwa.GestionDeSalleDeSportV2.Service.StatistiquesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatistiquesController {

    @Autowired
    private StatistiquesService statistiquesService;

    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GERANT')")
    public ResponseEntity<Statistiques> getStatistiques() {
        Statistiques statistiques = statistiquesService.getStatistiques();
        return ResponseEntity.ok(statistiques);
    }
}
