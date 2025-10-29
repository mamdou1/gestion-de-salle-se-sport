package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.Statistiques;
import com.cwa.GestionDeSalleDeSportV2.Entity.MoisCount;
import com.cwa.GestionDeSalleDeSportV2.Service.StatistiquesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {

    private static final Logger logger = LoggerFactory.getLogger(StatistiquesController.class);

    @Autowired
    private StatistiquesService statistiquesService;

    // === STATISTIQUES GÉNÉRALES ===

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Statistiques> getStatistiques() {
        try {
            Statistiques statistiques = statistiquesService.getStatistiques();
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques générales", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === STATISTIQUES DES ABONNEMENTS FAMILIAUX ===

    @GetMapping("/abonnements-familiaux")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAbonnementsFamiliaux() {
        try {
            Map<String, Object> stats = statistiquesService.getStatistiquesAbonnementsFamiliaux();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des abonnements familiaux", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux/gym/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAbonnementsFamiliauxParGym(@PathVariable Long gymId) {
        try {
            Map<String, Object> stats = statistiquesService.getStatistiquesAbonnementsFamiliauxParGym(gymId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des abonnements familiaux pour le gym {}", gymId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux/evolution-mensuelle")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<List<MoisCount>> getEvolutionAbonnementsFamiliauxMensuels() {
        try {
            List<MoisCount> evolution = statistiquesService.getEvolutionAbonnementsFamiliauxMensuels();
            return ResponseEntity.ok(evolution);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'évolution mensuelle des abonnements familiaux", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getPerformanceAbonnementsFamiliaux() {
        try {
            Map<String, Object> performance = statistiquesService.getPerformanceAbonnementsFamiliaux();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des performances des abonnements familiaux", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === STATISTIQUES DES FAMILLES ===

    @GetMapping("/familles")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFamilles() {
        try {
            Map<String, Object> stats = statistiquesService.getStatistiquesFamilles();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des familles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFamille(@PathVariable Long familleId) {
        try {
            Map<String, Object> stats = statistiquesService.getStatistiquesFamille(familleId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques de la famille {}", familleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles/classement/depenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<List<Map<String, Object>>> getClassementFamillesParDepenses() {
        try {
            List<Map<String, Object>> classement = statistiquesService.getClassementFamillesParDepenses();
            return ResponseEntity.ok(classement);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du classement des familles par dépenses", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles/fidelite")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFideliteFamilles() {
        try {
            Map<String, Object> stats = statistiquesService.getStatistiquesFideliteFamilles();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques de fidélité des familles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === ENDPOINTS COMBINÉS POUR LE DASHBOARD ===

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistiques() {
        try {
            Map<String, Object> dashboard = new java.util.HashMap<>();

            // Statistiques générales
            Statistiques statsGenerales = statistiquesService.getStatistiques();
            dashboard.put("statistiquesGenerales", statsGenerales);

            // Statistiques abonnements familiaux
            Map<String, Object> statsAbonnementsFamiliaux = statistiquesService.getStatistiquesAbonnementsFamiliaux();
            dashboard.put("abonnementsFamiliaux", statsAbonnementsFamiliaux);

            // Statistiques familles
            Map<String, Object> statsFamilles = statistiquesService.getStatistiquesFamilles();
            dashboard.put("familles", statsFamilles);

            // Performance des abonnements familiaux
            Map<String, Object> performance = statistiquesService.getPerformanceAbonnementsFamiliaux();
            dashboard.put("performance", performance);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques du dashboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/gym/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistiquesParGym(@PathVariable Long gymId) {
        try {
            Map<String, Object> dashboard = new java.util.HashMap<>();

            // Statistiques abonnements familiaux pour le gym
            Map<String, Object> statsAbonnementsFamiliaux = statistiquesService.getStatistiquesAbonnementsFamiliauxParGym(gymId);
            dashboard.put("abonnementsFamiliaux", statsAbonnementsFamiliaux);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques du dashboard pour le gym {}", gymId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}