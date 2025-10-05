package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Entity.MoisCount;
import com.cwa.GestionDeSalleDeSportV2.Entity.Statistiques;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatistiquesService {

    @PersistenceContext
    private EntityManager entityManager;

    public Statistiques getStatistiques() {
        // Récupérer les statistiques actuelles
        Long totalMembres = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.role = :role",
                        Long.class
                )
                .setParameter("role", Role.MEMBRE)
                .getSingleResult();
        Long totalEvenements = entityManager.createQuery(
                "SELECT COUNT(e) FROM Evenement e",
                Long.class
        ).getSingleResult();
        Long totalVentes = entityManager.createQuery(
                "SELECT COUNT(v) FROM Vente v",
                Long.class
        ).getSingleResult();
        Long abonnementsActifs = entityManager.createQuery(
                        "SELECT COUNT(a) FROM Abonnement a WHERE a.statut = :statut",
                        Long.class
                )
                .setParameter("statut", StatutAbonnement.EN_COURS)
                .getSingleResult();
        Double revenusMensuels = entityManager.createQuery(
                        "SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.statut = :statut AND YEAR(a.dateDebutAbonnement) = :year AND MONTH(a.dateDebutAbonnement) = :month",
                        Double.class
                )
                .setParameter("statut", StatutAbonnement.EN_COURS)
                .setParameter("year", LocalDate.now().getYear())
                .setParameter("month", LocalDate.now().getMonthValue())
                .getSingleResult();

        // Récupérer les inscriptions par mois pour l'année en cours (uniquement Role.MEMBRE)
        List<MoisCount> membresParMois = new ArrayList<>();
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        for (int i = 1; i <= 12; i++) {
            Long count = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.role = :role AND YEAR(u.date_creation) = :year AND MONTH(u.date_creation) = :month",
                            Long.class
                    )
                    .setParameter("role", Role.MEMBRE)
                    .setParameter("year", LocalDate.now().getYear())
                    .setParameter("month", i)
                    .getSingleResult();
            membresParMois.add(new MoisCount(mois[i - 1], count));
        }

        return new Statistiques(totalMembres, totalEvenements, totalVentes, abonnementsActifs, revenusMensuels, membresParMois);
    }
}
