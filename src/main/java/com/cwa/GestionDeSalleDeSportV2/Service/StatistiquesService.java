package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Entity.MoisCount;
import com.cwa.GestionDeSalleDeSportV2.Entity.Statistiques;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatistiquesService {
    private static final Logger logger = LoggerFactory.getLogger(StatistiquesService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public Statistiques getStatistiques() {
        logger.info("Début de getStatistiques");
        try {
            // Total membres avec gestion des null
            Long totalMembres = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.role = :role",
                            Long.class
                    )
                    .setParameter("role", Role.MEMBRE)
                    .getSingleResult();
            totalMembres = totalMembres != null ? totalMembres : 0L;
            logger.info("Total membres : {}", totalMembres);

            // Total événements
            Long totalEvenements = entityManager.createQuery(
                    "SELECT COUNT(e) FROM Evenement e",
                    Long.class
            ).getSingleResult();
            totalEvenements = totalEvenements != null ? totalEvenements : 0L;
            logger.info("Total événements : {}", totalEvenements);

            // Total ventes
            Long totalVentes = entityManager.createQuery(
                    "SELECT COUNT(v) FROM Vente v",
                    Long.class
            ).getSingleResult();
            totalVentes = totalVentes != null ? totalVentes : 0L;
            logger.info("Total ventes : {}", totalVentes);

            // Abonnements actifs
            Long abonnementsActifs = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.statut = :statut",
                            Long.class
                    )
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            abonnementsActifs = abonnementsActifs != null ? abonnementsActifs : 0L;
            logger.info("Abonnements actifs : {}", abonnementsActifs);

            // Revenus mensuels
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

            // Membres par mois
            List<MoisCount> membresParMois = new ArrayList<>();
            String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
            for (int i = 1; i <= 12; i++) {
                try {
                    Query nativeQuery = entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM user u WHERE u.role = :role AND YEAR(u.date_creation) = :year AND MONTH(u.date_creation) = :month"
                    );
                    nativeQuery.setParameter("role", Role.MEMBRE.name());
                    nativeQuery.setParameter("year", LocalDate.now().getYear());
                    nativeQuery.setParameter("month", i);
                    Long count = ((Number) nativeQuery.getSingleResult()).longValue();
                    membresParMois.add(new MoisCount(mois[i - 1], count));
                    logger.info("Membres pour {} : {}", mois[i - 1], count);
                } catch (Exception e) {
                    logger.warn("Erreur pour le mois {}: {}", mois[i-1], e.getMessage());
                    membresParMois.add(new MoisCount(mois[i - 1], 0L));
                }
            }

            Statistiques stats = new Statistiques(totalMembres, totalEvenements, totalVentes, abonnementsActifs, revenusMensuels, membresParMois);
            logger.info("Statistiques générées avec succès");
            return stats;
        } catch (Exception e) {
            logger.error("Erreur dans getStatistiques : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques générales", e);
        }
    }

    /**
     * Obtient les statistiques spécifiques aux abonnements familiaux
     */
    public Map<String, Object> getStatistiquesAbonnementsFamiliaux() {
        logger.info("Début de getStatistiquesAbonnementsFamiliaux");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Total des abonnements familiaux
            Long totalAbonnementsFamiliaux = entityManager.createQuery(
                    "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type",
                    Long.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            totalAbonnementsFamiliaux = totalAbonnementsFamiliaux != null ? totalAbonnementsFamiliaux : 0L;
            stats.put("totalAbonnementsFamiliaux", totalAbonnementsFamiliaux);
            logger.info("Total abonnements familiaux : {}", totalAbonnementsFamiliaux);

            // Abonnements familiaux actifs
            Long abonnementsFamiliauxActifs = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            abonnementsFamiliauxActifs = abonnementsFamiliauxActifs != null ? abonnementsFamiliauxActifs : 0L;
            stats.put("abonnementsFamiliauxActifs", abonnementsFamiliauxActifs);
            logger.info("Abonnements familiaux actifs : {}", abonnementsFamiliauxActifs);

            // Abonnements familiaux en pause
            Long abonnementsFamiliauxEnPause = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_PAUSE)
                    .getSingleResult();
            abonnementsFamiliauxEnPause = abonnementsFamiliauxEnPause != null ? abonnementsFamiliauxEnPause : 0L;
            stats.put("abonnementsFamiliauxEnPause", abonnementsFamiliauxEnPause);

            // Abonnements familiaux expirés
            Long abonnementsFamiliauxExpires = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EXPIRE)
                    .getSingleResult();
            abonnementsFamiliauxExpires = abonnementsFamiliauxExpires != null ? abonnementsFamiliauxExpires : 0L;
            stats.put("abonnementsFamiliauxExpires", abonnementsFamiliauxExpires);

            // Abonnements familiaux résiliés
            Long abonnementsFamiliauxResilies = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.RESILIE)
                    .getSingleResult();
            abonnementsFamiliauxResilies = abonnementsFamiliauxResilies != null ? abonnementsFamiliauxResilies : 0L;
            stats.put("abonnementsFamiliauxResilies", abonnementsFamiliauxResilies);

            // Chiffre d'affaires total des abonnements familiaux
            BigDecimal chiffreAffairesFamilial = entityManager.createQuery(
                    "SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.types = :type",
                    BigDecimal.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            chiffreAffairesFamilial = chiffreAffairesFamilial != null ? chiffreAffairesFamilial : BigDecimal.ZERO;
            stats.put("chiffreAffairesFamilial", chiffreAffairesFamilial);
            logger.info("Chiffre d'affaires familial : {}", chiffreAffairesFamilial);

            // Nombre total de familles
            Long totalFamilles = entityManager.createQuery(
                    "SELECT COUNT(f) FROM Famille f",
                    Long.class
            ).getSingleResult();
            totalFamilles = totalFamilles != null ? totalFamilles : 0L;
            stats.put("totalFamilles", totalFamilles);

            // Nombre de familles avec abonnement actif
            Long famillesAvecAbonnementActif = entityManager.createQuery(
                            "SELECT COUNT(DISTINCT a.famille) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            famillesAvecAbonnementActif = famillesAvecAbonnementActif != null ? famillesAvecAbonnementActif : 0L;
            stats.put("famillesAvecAbonnementActif", famillesAvecAbonnementActif);

            // Nombre total de membres familiaux
            Long totalMembresFamiliaux = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.famille IS NOT NULL",
                    Long.class
            ).getSingleResult();
            totalMembresFamiliaux = totalMembresFamiliaux != null ? totalMembresFamiliaux : 0L;
            stats.put("totalMembresFamiliaux", totalMembresFamiliaux);

            // Nombre de membres avec abonnement familial actif
            Long membresAvecAbonnementFamilialActif = entityManager.createQuery(
                            "SELECT COUNT(DISTINCT a.membre) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            membresAvecAbonnementFamilialActif = membresAvecAbonnementFamilialActif != null ? membresAvecAbonnementFamilialActif : 0L;
            stats.put("membresAvecAbonnementFamilialActif", membresAvecAbonnementFamilialActif);

            // Abonnements familiaux expirant bientôt (dans les 7 jours)
            LocalDate aujourdHui = LocalDate.now();
            LocalDate dans7Jours = aujourdHui.plusDays(7);
            Long abonnementsExpirantBientot = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut AND a.dateFinAbonnement BETWEEN :startDate AND :endDate",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .setParameter("startDate", aujourdHui)
                    .setParameter("endDate", dans7Jours)
                    .getSingleResult();
            abonnementsExpirantBientot = abonnementsExpirantBientot != null ? abonnementsExpirantBientot : 0L;
            stats.put("abonnementsExpirantBientot", abonnementsExpirantBientot);

            logger.info("Statistiques abonnements familiaux générées avec succès");
            return stats;

        } catch (Exception e) {
            logger.error("Erreur dans getStatistiquesAbonnementsFamiliaux : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques des abonnements familiaux", e);
        }
    }

    /**
     * Obtient les statistiques des abonnements familiaux par gym
     */
    public Map<String, Object> getStatistiquesAbonnementsFamiliauxParGym(Long gymId) {
        logger.info("Début de getStatistiquesAbonnementsFamiliauxParGym pour le gym ID: {}", gymId);

        try {
            Map<String, Object> stats = new HashMap<>();

            // Informations du gym avec vérification d'existence
            List<String> nomGymList = entityManager.createQuery(
                    "SELECT g.nom FROM Gym g WHERE g.id = :gymId",
                    String.class
            ).setParameter("gymId", gymId).getResultList();

            if (nomGymList.isEmpty()) {
                throw new RuntimeException("Gym non trouvé avec l'ID: " + gymId);
            }

            String nomGym = nomGymList.get(0);
            stats.put("nomGym", nomGym);
            stats.put("gymId", gymId);

            // Total des abonnements familiaux pour ce gym
            Long totalAbonnementsFamiliaux = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.gym.id = :gymId",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("gymId", gymId)
                    .getSingleResult();
            totalAbonnementsFamiliaux = totalAbonnementsFamiliaux != null ? totalAbonnementsFamiliaux : 0L;
            stats.put("totalAbonnementsFamiliaux", totalAbonnementsFamiliaux);

            // Abonnements familiaux actifs pour ce gym
            Long abonnementsFamiliauxActifs = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut AND a.gym.id = :gymId",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .setParameter("gymId", gymId)
                    .getSingleResult();
            abonnementsFamiliauxActifs = abonnementsFamiliauxActifs != null ? abonnementsFamiliauxActifs : 0L;
            stats.put("abonnementsFamiliauxActifs", abonnementsFamiliauxActifs);

            // Chiffre d'affaires des abonnements familiaux pour ce gym
            BigDecimal chiffreAffairesFamilial = entityManager.createQuery(
                            "SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.types = :type AND a.gym.id = :gymId",
                            BigDecimal.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("gymId", gymId)
                    .getSingleResult();
            chiffreAffairesFamilial = chiffreAffairesFamilial != null ? chiffreAffairesFamilial : BigDecimal.ZERO;
            stats.put("chiffreAffairesFamilial", chiffreAffairesFamilial);

            // Nombre de familles avec abonnement actif pour ce gym
            Long famillesAvecAbonnementActif = entityManager.createQuery(
                            "SELECT COUNT(DISTINCT a.famille) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut AND a.gym.id = :gymId",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .setParameter("gymId", gymId)
                    .getSingleResult();
            famillesAvecAbonnementActif = famillesAvecAbonnementActif != null ? famillesAvecAbonnementActif : 0L;
            stats.put("famillesAvecAbonnementActif", famillesAvecAbonnementActif);

            logger.info("Statistiques abonnements familiaux par gym générées avec succès pour le gym: {}", nomGym);
            return stats;

        } catch (Exception e) {
            logger.error("Erreur dans getStatistiquesAbonnementsFamiliauxParGym : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques des abonnements familiaux pour le gym " + gymId, e);
        }
    }

    /**
     * Obtient l'évolution mensuelle des abonnements familiaux
     */
    public List<MoisCount> getEvolutionAbonnementsFamiliauxMensuels() {
        logger.info("Début de getEvolutionAbonnementsFamiliauxMensuels");

        try {
            List<MoisCount> evolution = new ArrayList<>();
            String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

            int anneeCourante = LocalDate.now().getYear();

            for (int i = 1; i <= 12; i++) {
                try {
                    Query nativeQuery = entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM abonnement a WHERE a.types = 'FAMILIALE' AND YEAR(a.date_debut_abonnement) = :year AND MONTH(a.date_debut_abonnement) = :month"
                    );
                    nativeQuery.setParameter("year", anneeCourante);
                    nativeQuery.setParameter("month", i);

                    Long count = ((Number) nativeQuery.getSingleResult()).longValue();
                    evolution.add(new MoisCount(mois[i - 1], count));
                    logger.info("Abonnements familiaux pour {} {} : {}", mois[i - 1], anneeCourante, count);
                } catch (Exception e) {
                    logger.warn("Erreur pour le mois {}: {}", mois[i-1], e.getMessage());
                    evolution.add(new MoisCount(mois[i - 1], 0L));
                }
            }

            logger.info("Évolution mensuelle des abonnements familiaux générée avec succès");
            return evolution;

        } catch (Exception e) {
            logger.error("Erreur dans getEvolutionAbonnementsFamiliauxMensuels : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération de l'évolution mensuelle des abonnements familiaux", e);
        }
    }

    /**
     * Obtient les statistiques de performance des abonnements familiaux
     */
    public Map<String, Object> getPerformanceAbonnementsFamiliaux() {
        logger.info("Début de getPerformanceAbonnementsFamiliaux");

        try {
            Map<String, Object> performance = new HashMap<>();

            // Taux de renouvellement (approximatif)
            Long totalRenouvellements = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.dateDebutAbonnement > :dateLimite",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("dateLimite", LocalDate.now().minusMonths(1))
                    .getSingleResult();
            totalRenouvellements = totalRenouvellements != null ? totalRenouvellements : 0L;

            Long totalAbonnementsActifs = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            totalAbonnementsActifs = totalAbonnementsActifs != null ? totalAbonnementsActifs : 0L;

            Double tauxRenouvellement = totalAbonnementsActifs > 0 ?
                    (double) totalRenouvellements / totalAbonnementsActifs * 100 : 0.0;
            performance.put("tauxRenouvellement", Math.round(tauxRenouvellement * 100.0) / 100.0);

            // Revenu moyen par famille
            BigDecimal chiffreAffairesTotal = entityManager.createQuery(
                    "SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.types = :type",
                    BigDecimal.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            chiffreAffairesTotal = chiffreAffairesTotal != null ? chiffreAffairesTotal : BigDecimal.ZERO;

            Long totalFamillesAvecAbonnement = entityManager.createQuery(
                    "SELECT COUNT(DISTINCT a.famille) FROM Abonnement a WHERE a.types = :type",
                    Long.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            totalFamillesAvecAbonnement = totalFamillesAvecAbonnement != null ? totalFamillesAvecAbonnement : 0L;

            BigDecimal revenuMoyenParFamille = totalFamillesAvecAbonnement > 0 ?
                    chiffreAffairesTotal.divide(BigDecimal.valueOf(totalFamillesAvecAbonnement), 2, java.math.RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            performance.put("revenuMoyenParFamille", revenuMoyenParFamille);

            // Taux de résiliation
            Long totalResilies = entityManager.createQuery(
                            "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.RESILIE)
                    .getSingleResult();
            totalResilies = totalResilies != null ? totalResilies : 0L;

            Long totalAbonnements = entityManager.createQuery(
                    "SELECT COUNT(a) FROM Abonnement a WHERE a.types = :type",
                    Long.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            totalAbonnements = totalAbonnements != null ? totalAbonnements : 0L;

            Double tauxResiliation = totalAbonnements > 0 ?
                    (double) totalResilies / totalAbonnements * 100 : 0.0;
            performance.put("tauxResiliation", Math.round(tauxResiliation * 100.0) / 100.0);

            logger.info("Performance des abonnements familiaux calculée avec succès");
            return performance;

        } catch (Exception e) {
            logger.error("Erreur dans getPerformanceAbonnementsFamiliaux : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des performances des abonnements familiaux", e);
        }
    }

    /**
     * Obtient les statistiques générales sur les familles
     */
    public Map<String, Object> getStatistiquesFamilles() {
        logger.info("Début de getStatistiquesFamilles");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Total des familles
            Long totalFamilles = entityManager.createQuery(
                    "SELECT COUNT(f) FROM Famille f",
                    Long.class
            ).getSingleResult();
            totalFamilles = totalFamilles != null ? totalFamilles : 0L;
            stats.put("totalFamilles", totalFamilles);
            logger.info("Total familles : {}", totalFamilles);

            // Familles avec abonnement actif
            Long famillesAvecAbonnementActif = entityManager.createQuery(
                            "SELECT COUNT(DISTINCT a.famille) FROM Abonnement a WHERE a.types = :type AND a.statut = :statut",
                            Long.class
                    )
                    .setParameter("type", TypeAbonnements.FAMILIALE)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .getSingleResult();
            famillesAvecAbonnementActif = famillesAvecAbonnementActif != null ? famillesAvecAbonnementActif : 0L;
            stats.put("famillesAvecAbonnementActif", famillesAvecAbonnementActif);

            // Familles sans abonnement actif
            Long famillesSansAbonnementActif = totalFamilles - famillesAvecAbonnementActif;
            stats.put("famillesSansAbonnementActif", famillesSansAbonnementActif);

            // Répartition par nombre de membres
            List<Object[]> repartitionMembres = entityManager.createQuery(
                    "SELECT COUNT(f), SIZE(f.membres) FROM Famille f GROUP BY SIZE(f.membres) ORDER BY SIZE(f.membres)",
                    Object[].class
            ).getResultList();

            Map<String, Long> repartitionParTaille = new HashMap<>();
            for (Object[] result : repartitionMembres) {
                Long count = (Long) result[0];
                Integer taille = (Integer) result[1];
                repartitionParTaille.put(taille + " membre(s)", count);
            }
            stats.put("repartitionParTaille", repartitionParTaille);

            // Taille moyenne des familles
            Double tailleMoyenne = entityManager.createQuery(
                    "SELECT AVG(SIZE(f.membres)) FROM Famille f",
                    Double.class
            ).getSingleResult();
            stats.put("tailleMoyenneFamille", tailleMoyenne != null ? Math.round(tailleMoyenne * 100.0) / 100.0 : 0.0);

            // Famille avec le plus de membres
            Integer maxMembres = entityManager.createQuery(
                    "SELECT MAX(SIZE(f.membres)) FROM Famille f",
                    Integer.class
            ).getSingleResult();
            stats.put("maxMembresParFamille", maxMembres != null ? maxMembres : 0);

            // Évolution des créations de familles par mois
            List<MoisCount> creationsFamillesParMois = getCreationsFamillesParMois();
            stats.put("creationsFamillesParMois", creationsFamillesParMois);

            logger.info("Statistiques familles générées avec succès");
            return stats;

        } catch (Exception e) {
            logger.error("Erreur dans getStatistiquesFamilles : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques des familles", e);
        }
    }

    /**
     * Obtient les créations de familles par mois
     */
    private List<MoisCount> getCreationsFamillesParMois() {
        List<MoisCount> creations = new ArrayList<>();
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

        int anneeCourante = LocalDate.now().getYear();

        for (int i = 1; i <= 12; i++) {
            try {
                Query nativeQuery = entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM famille f WHERE YEAR(f.date_creation) = :year AND MONTH(f.date_creation) = :month"
                );
                nativeQuery.setParameter("year", anneeCourante);
                nativeQuery.setParameter("month", i);

                Long count = ((Number) nativeQuery.getSingleResult()).longValue();
                creations.add(new MoisCount(mois[i - 1], count));
            } catch (Exception e) {
                logger.warn("Erreur pour le mois {}: {}", mois[i-1], e.getMessage());
                creations.add(new MoisCount(mois[i - 1], 0L));
            }
        }

        return creations;
    }

    /**
     * Obtient les statistiques détaillées d'une famille spécifique
     */
    public Map<String, Object> getStatistiquesFamille(Long familleId) {
        logger.info("Début de getStatistiquesFamille pour l'ID: {}", familleId);

        try {
            Map<String, Object> stats = new HashMap<>();

            // Informations de base de la famille avec vérification d'existence
            List<Object[]> infosFamilleList = entityManager.createQuery(
                    "SELECT f.nom, f.dateCreation, SIZE(f.membres) FROM Famille f WHERE f.id = :familleId",
                    Object[].class
            ).setParameter("familleId", familleId).getResultList();

            if (infosFamilleList.isEmpty()) {
                throw new RuntimeException("Famille non trouvée avec l'ID: " + familleId);
            }

            Object[] infosFamille = infosFamilleList.get(0);
            stats.put("nomFamille", infosFamille[0]);
            stats.put("dateCreation", infosFamille[1]);
            stats.put("nombreMembres", infosFamille[2]);

            // Abonnements de la famille
            Long totalAbonnements = entityManager.createQuery(
                    "SELECT COUNT(a) FROM Abonnement a WHERE a.famille.id = :familleId",
                    Long.class
            ).setParameter("familleId", familleId).getSingleResult();
            totalAbonnements = totalAbonnements != null ? totalAbonnements : 0L;
            stats.put("totalAbonnements", totalAbonnements);

            // Abonnement actuel
            List<Abonnement> abonnementsActifs = entityManager.createQuery(
                            "SELECT a FROM Abonnement a WHERE a.famille.id = :familleId AND a.statut = :statut ORDER BY a.dateDebutAbonnement DESC",
                            Abonnement.class
                    )
                    .setParameter("familleId", familleId)
                    .setParameter("statut", StatutAbonnement.EN_COURS)
                    .setMaxResults(1)
                    .getResultList();

            if (!abonnementsActifs.isEmpty()) {
                Abonnement abonnementActuel = abonnementsActifs.get(0);
                stats.put("abonnementActuelDateDebut", abonnementActuel.getDateDebutAbonnement());
                stats.put("abonnementActuelDateFin", abonnementActuel.getDateFinAbonnement());
                stats.put("abonnementActuelPrix", abonnementActuel.getPrixAbonnement());
            }

            // Dépenses totales de la famille
            BigDecimal depensesTotales = entityManager.createQuery(
                    "SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.famille.id = :familleId",
                    BigDecimal.class
            ).setParameter("familleId", familleId).getSingleResult();
            depensesTotales = depensesTotales != null ? depensesTotales : BigDecimal.ZERO;
            stats.put("depensesTotales", depensesTotales);

            // Historique des abonnements
            List<Object[]> historiqueAbonnements = entityManager.createQuery(
                    "SELECT a.dateDebutAbonnement, a.dateFinAbonnement, a.prixAbonnement, a.statut FROM Abonnement a WHERE a.famille.id = :familleId ORDER BY a.dateDebutAbonnement DESC",
                    Object[].class
            ).setParameter("familleId", familleId).getResultList();
            stats.put("historiqueAbonnements", historiqueAbonnements);

            // Membres de la famille avec leurs statistiques
            List<Map<String, Object>> statsMembres = getStatistiquesMembresFamille(familleId);
            stats.put("membres", statsMembres);

            logger.info("Statistiques détaillées de la famille {} générées avec succès", familleId);
            return stats;

        } catch (Exception e) {
            logger.error("Erreur dans getStatistiquesFamille : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques de la famille " + familleId, e);
        }
    }

    /**
     * Obtient les statistiques des membres d'une famille - CORRIGÉ
     */
    private List<Map<String, Object>> getStatistiquesMembresFamille(Long familleId) {
        List<Map<String, Object>> membresStats = new ArrayList<>();

        // CORRECTION : Utilisation de la syntaxe JPQL correcte
        List<Object[]> membres = entityManager.createQuery(
                "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, u.genre, u.dateDeNaissance FROM User u WHERE u.famille.id = :familleId",
                Object[].class
        ).setParameter("familleId", familleId).getResultList();

        for (Object[] membre : membres) {
            Map<String, Object> statsMembre = new HashMap<>();
            statsMembre.put("id", membre[0]);
            statsMembre.put("nom", membre[1]);
            statsMembre.put("prenom", membre[2]);
            statsMembre.put("email", membre[3]);
            statsMembre.put("telephone", membre[4]);
            statsMembre.put("genre", membre[5]);
            statsMembre.put("dateNaissance", membre[6]);

            // Abonnements du membre
            Long abonnementsMembre = entityManager.createQuery(
                    "SELECT COUNT(a) FROM Abonnement a WHERE a.membre.id = :membreId",
                    Long.class
            ).setParameter("membreId", membre[0]).getSingleResult();
            abonnementsMembre = abonnementsMembre != null ? abonnementsMembre : 0L;
            statsMembre.put("nombreAbonnements", abonnementsMembre);

            membresStats.add(statsMembre);
        }

        return membresStats;
    }

    /**
     * Obtient le classement des familles par dépenses
     */
    public List<Map<String, Object>> getClassementFamillesParDepenses() {
        logger.info("Début de getClassementFamillesParDepenses");

        try {
            List<Map<String, Object>> classement = new ArrayList<>();

            List<Object[]> resultats = entityManager.createQuery(
                    "SELECT f.id, f.nom, COUNT(DISTINCT a.membre), COALESCE(SUM(a.prixAbonnement), 0) " +
                            "FROM Famille f LEFT JOIN f.membres m LEFT JOIN Abonnement a ON a.famille = f " +
                            "GROUP BY f.id, f.nom " +
                            "ORDER BY COALESCE(SUM(a.prixAbonnement), 0) DESC",
                    Object[].class
            ).setMaxResults(10).getResultList();

            int rang = 1;
            for (Object[] resultat : resultats) {
                Map<String, Object> familleClassement = new HashMap<>();
                familleClassement.put("rang", rang++);
                familleClassement.put("id", resultat[0]);
                familleClassement.put("nom", resultat[1]);
                familleClassement.put("nombreMembres", resultat[2]);
                familleClassement.put("depensesTotales", resultat[3]);

                classement.add(familleClassement);
            }

            logger.info("Classement des familles par dépenses généré avec succès");
            return classement;

        } catch (Exception e) {
            logger.error("Erreur dans getClassementFamillesParDepenses : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération du classement des familles par dépenses", e);
        }
    }

    /**
     * Obtient les statistiques de fidélité des familles
     */
    public Map<String, Object> getStatistiquesFideliteFamilles() {
        logger.info("Début de getStatistiquesFideliteFamilles");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Durée moyenne d'abonnement des familles
            Double dureeMoyenneAbonnement = entityManager.createQuery(
                    "SELECT AVG(a.nombreDeMois) FROM Abonnement a WHERE a.types = :type",
                    Double.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            stats.put("dureeMoyenneAbonnement", dureeMoyenneAbonnement != null ? Math.round(dureeMoyenneAbonnement * 100.0) / 100.0 : 0.0);

            // Taux de renouvellement des familles
            Long famillesAvecRenouvellement = entityManager.createQuery(
                    "SELECT COUNT(DISTINCT a1.famille) FROM Abonnement a1 WHERE a1.types = :type AND EXISTS " +
                            "(SELECT a2 FROM Abonnement a2 WHERE a2.famille = a1.famille AND a2.id != a1.id)",
                    Long.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            famillesAvecRenouvellement = famillesAvecRenouvellement != null ? famillesAvecRenouvellement : 0L;

            Long totalFamillesAvecAbonnement = entityManager.createQuery(
                    "SELECT COUNT(DISTINCT a.famille) FROM Abonnement a WHERE a.types = :type",
                    Long.class
            ).setParameter("type", TypeAbonnements.FAMILIALE).getSingleResult();
            totalFamillesAvecAbonnement = totalFamillesAvecAbonnement != null ? totalFamillesAvecAbonnement : 0L;

            Double tauxRenouvellement = totalFamillesAvecAbonnement > 0 ?
                    (double) famillesAvecRenouvellement / totalFamillesAvecAbonnement * 100 : 0.0;
            stats.put("tauxRenouvellementFamilles", Math.round(tauxRenouvellement * 100.0) / 100.0);

            // Familles les plus anciennes
            List<Object[]> famillesAnciennes = entityManager.createQuery(
                    "SELECT f.nom, f.dateCreation, COUNT(a) " +
                            "FROM Famille f LEFT JOIN f.membres m LEFT JOIN Abonnement a ON a.famille = f " +
                            "GROUP BY f.id, f.nom, f.dateCreation " +
                            "ORDER BY f.dateCreation ASC",
                    Object[].class
            ).setMaxResults(5).getResultList();

            List<Map<String, Object>> topFamillesAnciennes = new ArrayList<>();
            for (Object[] famille : famillesAnciennes) {
                Map<String, Object> familleInfo = new HashMap<>();
                familleInfo.put("nom", famille[0]);
                familleInfo.put("dateCreation", famille[1]);
                familleInfo.put("nombreAbonnements", famille[2]);
                topFamillesAnciennes.add(familleInfo);
            }
            stats.put("famillesLesPlusAnciennes", topFamillesAnciennes);

            logger.info("Statistiques de fidélité des familles générées avec succès");
            return stats;

        } catch (Exception e) {
            logger.error("Erreur dans getStatistiquesFideliteFamilles : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques de fidélité des familles", e);
        }
    }

    /**
     * Méthode de diagnostic pour tester les connexions aux entités
     */
    public Map<String, String> testConnexions() {
        Map<String, String> tests = new HashMap<>();

        try {
            Long countUsers = entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
            tests.put("User", "OK - " + countUsers + " utilisateurs");
        } catch (Exception e) {
            tests.put("User", "ERREUR: " + e.getMessage());
        }

        try {
            Long countAbonnements = entityManager.createQuery("SELECT COUNT(a) FROM Abonnement a", Long.class)
                    .getSingleResult();
            tests.put("Abonnement", "OK - " + countAbonnements + " abonnements");
        } catch (Exception e) {
            tests.put("Abonnement", "ERREUR: " + e.getMessage());
        }

        try {
            Long countFamilles = entityManager.createQuery("SELECT COUNT(f) FROM Famille f", Long.class)
                    .getSingleResult();
            tests.put("Famille", "OK - " + countFamilles + " familles");
        } catch (Exception e) {
            tests.put("Famille", "ERREUR: " + e.getMessage());
        }

        try {
            Long countGyms = entityManager.createQuery("SELECT COUNT(g) FROM Gym g", Long.class)
                    .getSingleResult();
            tests.put("Gym", "OK - " + countGyms + " gyms");
        } catch (Exception e) {
            tests.put("Gym", "ERREUR: " + e.getMessage());
        }

        return tests;
    }
}