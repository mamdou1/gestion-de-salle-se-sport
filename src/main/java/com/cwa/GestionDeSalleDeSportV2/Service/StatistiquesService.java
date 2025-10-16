package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Entity.MoisCount;
import com.cwa.GestionDeSalleDeSportV2.Entity.Statistiques;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatistiquesService {
    private static final Logger logger = LoggerFactory.getLogger(StatistiquesService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public Statistiques getStatistiques() {
        logger.info("Début de getStatistiques");
        try {
            Long totalMembres = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.role = :role",
                            Long.class
                    )
                    .setParameter("role", Role.MEMBRE)
                    .getSingleResult();
            logger.info("Total membres : {}", totalMembres);

            Long totalEvenements = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Evenement e",
                    Long.class
            ).getSingleResult();
            logger.info("Total événements : {}", totalEvenements);

            Long totalVentes = entityManager.createQuery(
                    "SELECT COUNT(v) FROM Vente v",
                    Long.class
            ).getSingleResult();
            logger.info("Total ventes : {}", totalVentes);

            Long abonnementsActifs = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.statut = :statut",
                            Long.class
                    )
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            logger.info("Abonnements actifs : {}", abonnementsActifs);

            BigDecimal revenusMensuelsBigDecimal = entityManager.createQuery(
                            "SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.statut = :statut AND FUNCTION('YEAR', a.dateDebutAbonnement) = :year AND FUNCTION('MONTH', a.dateDebutAbonnement) = :month",
                            BigDecimal.class
                    )
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .setParameter("year", LocalDate.now().getYear())
                    .setParameter("month", LocalDate.now().getMonthValue())
                    .getSingleResult();
            Double revenusMensuels = revenusMensuelsBigDecimal != null ? revenusMensuelsBigDecimal.doubleValue() : 0.0;
            logger.info("Revenus mensuels : {}", revenusMensuels);

            List<MoisCount> membresParMois = new ArrayList<>();
            String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
            for (int i = 1; i <= 12; i++) {
                Query nativeQuery = entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM user u WHERE u.role = :role AND YEAR(u.date_creation) = :year AND MONTH(u.date_creation) = :month"
                );
                nativeQuery.setParameter("role", Role.MEMBRE.name());
                nativeQuery.setParameter("year", LocalDate.now().getYear());
                nativeQuery.setParameter("month", i);
                Long count = ((Number) nativeQuery.getSingleResult()).longValue();
                membresParMois.add(new MoisCount(mois[i - 1], count));
                logger.info("Membres pour {} : {}", mois[i - 1], count);
            }

            Statistiques stats = new Statistiques(totalMembres, totalEvenements, totalVentes, abonnementsActifs, revenusMensuels, membresParMois);
            logger.info("Statistiques générées : {}", stats);
            return stats;
        } catch (Exception e) {
            logger.error("Erreur dans getStatistiques : {}", e.getMessage(), e);
            throw e;
        }
    }
}