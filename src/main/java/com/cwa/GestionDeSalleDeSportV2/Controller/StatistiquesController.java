package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.MoisCount;
import com.cwa.GestionDeSalleDeSportV2.Entity.Statistiques;
import com.cwa.GestionDeSalleDeSportV2.Service.StatistiquesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {

    private static final Logger logger = LoggerFactory.getLogger(StatistiquesController.class);

    @Autowired
    private StatistiquesService statistiquesService;

    // ENDPOINT PRINCIPAL - CORRIGÉ (PLUS DE FORÇAGE "MOIS")
    @GetMapping("/simplifiees")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesSimplifiees(
            @RequestParam(required = false) String periode,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {

        try {
            logger.info("REQUÊTE STATISTIQUES SIMPLIFIÉES → periode={}, date={}, dateDebut={}, dateFin={}",
                    periode, date, dateDebut, dateFin);

            // ON NE FORCE PLUS RIEN ICI
            // → Si aucun paramètre → le service renvoie tout l'historique (GLOBAL)
            // → Si periode=ANNEE → année en cours
            // → Si periode=GLOBAL → tout l'historique

            Map<String, Object> stats = statistiquesService.getStatistiquesSimplifiees(
                    periode,      // peut être null → service gère le cas
                    date,
                    dateDebut,
                    dateFin
            );

            logger.info("RÉPONSE STATISTIQUES SIMPLIFIÉES → {} membres, {} familles, revenu={}",
                    stats.get("totalMembres"), stats.get("totalFamilles"), stats.get("revenuTotal"));

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("ERREUR ENDPOINT /simplifiees", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur serveur");
            error.put("message", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(error);
        }
    }

    // TOUS LES AUTRES ENDPOINTS (intacts)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Statistiques> getStatistiques() {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiques());
        } catch (Exception e) {
            logger.error("Erreur getStatistiques", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/globales")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesGlobales() {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiquesGlobales());
        } catch (Exception e) {
            logger.error("Erreur getStatistiquesGlobales", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAbonnementsFamiliaux() {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiquesAbonnementsFamiliaux());
        } catch (Exception e) {
            logger.error("Erreur abonnements familiaux", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux/gym/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAbonnementsFamiliauxParGym(@PathVariable Long gymId) {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiquesAbonnementsFamiliauxParGym(gymId));
        } catch (Exception e) {
            logger.error("Erreur stats gym {}", gymId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux/evolution-mensuelle")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<List<MoisCount>> getEvolutionAbonnementsFamiliauxMensuels() {
        try {
            return ResponseEntity.ok(statistiquesService.getEvolutionAbonnementsFamiliauxMensuels());
        } catch (Exception e) {
            logger.error("Erreur évolution mensuelle", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/abonnements-familiaux/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getPerformanceAbonnementsFamiliaux() {
        try {
            return ResponseEntity.ok(statistiquesService.getPerformanceAbonnementsFamiliaux());
        } catch (Exception e) {
            logger.error("Erreur performance abonnements", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFamilles() {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiquesFamilles());
        } catch (Exception e) {
            logger.error("Erreur stats familles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles/{familleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFamille(@PathVariable Long familleId) {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiquesFamille(familleId));
        } catch (Exception e) {
            logger.error("Erreur stats famille {}", familleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles/classement/depenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<List<Map<String, Object>>> getClassementFamillesParDepenses() {
        try {
            return ResponseEntity.ok(statistiquesService.getClassementFamillesParDepenses());
        } catch (Exception e) {
            logger.error("Erreur classement dépenses", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/familles/fidelite")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFideliteFamilles() {
        try {
            return ResponseEntity.ok(statistiquesService.getStatistiquesFideliteFamilles());
        } catch (Exception e) {
            logger.error("Erreur fidélité familles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistiques() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("statistiquesGenerales", statistiquesService.getStatistiques());
            dashboard.put("abonnementsFamiliaux", statistiquesService.getStatistiquesAbonnementsFamiliaux());
            dashboard.put("familles", statistiquesService.getStatistiquesFamilles());
            dashboard.put("performance", statistiquesService.getPerformanceAbonnementsFamiliaux());
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Erreur dashboard global", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/gym/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistiquesParGym(@PathVariable Long gymId) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("abonnementsFamiliaux", statistiquesService.getStatistiquesAbonnementsFamiliauxParGym(gymId));
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Erreur dashboard gym {}", gymId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/comparaison")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERANT', 'RECEPTIONNISTE')")
    public ResponseEntity<Map<String, Object>> getComparaisonStatistiques(
            @RequestParam String periode1,
            @RequestParam String periode2) {
        try {
            Map<String, Object> stats1 = statistiquesService.getStatistiquesSimplifiees(periode1, null, null, null);
            Map<String, Object> stats2 = statistiquesService.getStatistiquesSimplifiees(periode2, null, null, null);
            Map<String, Object> comparaison = new HashMap<>();
            comparaison.put("periode1", stats1);
            comparaison.put("periode2", stats2);
            return ResponseEntity.ok(comparaison);
        } catch (Exception e) {
            logger.error("Erreur comparaison", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}