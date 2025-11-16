package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    // === MÉTHODES DTO OPTIMISÉES POUR TOUS LES ABONNEMENTS ===

    /**
     * Récupère tous les abonnements en DTO (sans relations circulaires)
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAllAbonnementDTOs();

    /**
     * Récupère les abonnements avec filtres optionnels
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE (:gymId IS NULL OR a.gym.id = :gymId) " +
            "AND (:statut IS NULL OR a.statut = :statut) " +
            "AND (:type IS NULL OR a.types = :type) " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAbonnementsDTOByFilters(@Param("gymId") Long gymId,
                                                    @Param("statut") StatutAbonnement statut,
                                                    @Param("type") TypeAbonnements type);

    /**
     * Récupère les abonnements DTO par gym
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.gym.id = :gymId " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAbonnementsDTOByGymId(@Param("gymId") Long gymId);

    /**
     * Récupère les abonnements DTO par membre (individuels)
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.membre.id = :membreId " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAbonnementsDTOByMembreId(@Param("membreId") Long membreId);

    /**
     * Récupère les abonnements DTO par famille (familiaux)
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.famille.id = :familleId " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAbonnementsDTOByFamilleId(@Param("familleId") Long familleId);

    /**
     * Récupère les abonnements DTO actifs
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.statut = 'EN_COURS' " +
            "ORDER BY a.dateFinAbonnement ASC")
    List<AbonnementDTO> findAbonnementsActifsDTO();

    /**
     * Récupère un abonnement DTO spécifique par ID
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.id = :abonnementId")
    Optional<AbonnementDTO> findAbonnementDTOById(@Param("abonnementId") Long abonnementId);

    // === MÉTHODES SPÉCIFIQUES POUR LES ABONNEMENTS FAMILIAUX ===

    /**
     * Récupère tous les abonnements familiaux en DTO
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.types = 'FAMILIALE' " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAllAbonnementsFamiliauxDTO();

    /**
     * Récupère les abonnements familiaux actifs en DTO
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.types = 'FAMILIALE' AND a.statut = 'EN_COURS' " +
            "ORDER BY a.dateFinAbonnement ASC")
    List<AbonnementDTO> findAbonnementsFamiliauxActifsDTO();

    /**
     * Récupère les abonnements familiaux par gym en DTO
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.types = 'FAMILIALE' AND a.gym.id = :gymId " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAbonnementsFamiliauxDTOByGymId(@Param("gymId") Long gymId);

    // === MÉTHODES SPÉCIFIQUES POUR LES ABONNEMENTS INDIVIDUELS ===

    /**
     * Récupère tous les abonnements individuels en DTO
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.types = 'INDIVIDUEL' " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> findAllAbonnementsIndividuelsDTO();

    /**
     * Récupère les abonnements individuels actifs en DTO
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE a.types = 'INDIVIDUEL' AND a.statut = 'EN_COURS' " +
            "ORDER BY a.dateFinAbonnement ASC")
    List<AbonnementDTO> findAbonnementsIndividuelsActifsDTO();

    // === MÉTHODES DE RECHERCHE AVANCÉE EN DTO ===

    /**
     * Recherche d'abonnements par nom (membre ou famille)
     */
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.AbonnementDTO(" +
            "a.id, a.periodAbonnement, a.types, a.nombreDeMois, a.prixAbonnement, " +
            "a.modeDePaiement, a.statut, a.dateDebutAbonnement, a.dateFinAbonnement, " +
            "a.typeDeService.id) " +
            "FROM Abonnement a " +
            "WHERE (a.types = 'INDIVIDUEL' AND (LOWER(a.membre.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.membre.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) " +
            "OR (a.types = 'FAMILIALE' AND LOWER(a.famille.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY a.dateDebutAbonnement DESC")
    List<AbonnementDTO> searchAbonnementsDTOByName(@Param("searchTerm") String searchTerm);

    // === MÉTHODES EXISTANTES (gardées pour compatibilité) ===

    List<Abonnement> findByMembreOrderByDateDebutAbonnementDesc(User membre);
    List<Abonnement> findByFamilleOrderByDateDebutAbonnementDesc(Famille famille);
    List<Abonnement> findByFamilleId(Long familleId);
    List<Abonnement> findByGymId(Long gymId);
    List<Abonnement> findByTypes(TypeAbonnements types);
    List<Abonnement> findByGymIn(List<Gym> gyms);
    List<Abonnement> findByStatut(StatutAbonnement statut);
    List<Abonnement> findByTypesAndStatut(TypeAbonnements types, StatutAbonnement statut);
    List<Abonnement> findByMembreAndStatut(User membre, StatutAbonnement statut);

    long countByTypesAndStatut(TypeAbonnements types, StatutAbonnement statut);
    long countByTypes(TypeAbonnements types);

    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.statut = 'EN_COURS'")
    long countByMembreActifs();

    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.statut = 'EXPIRE'")
    long countByMembreExpirer();

    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.statut = 'BIENTOT_EXPIRE'")
    long countByMembreBientotExpirer();

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement = :date")
    long countByGymIdAndDate(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement = :date")
    List<Abonnement> findByGymIdAndDate(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement BETWEEN :startOfWeek AND :endOfWeek")
    long countByGymIdAndWeek(@Param("gymId") Long gymId, @Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = YEAR(:date) AND MONTH(a.dateDebutAbonnement) = MONTH(:date)")
    long countByGymIdAndMonth(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = YEAR(:date)")
    long countByGymIdAndYear(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    List<Abonnement> findByGymIdAndDateDebutAbonnementBetween(Long gymId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = :year AND MONTH(a.dateDebutAbonnement) = :month")
    List<Abonnement> findByGymIdAndMonth(@Param("gymId") Long gymId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT a FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = :year")
    List<Abonnement> findByGymIdAndYear(@Param("gymId") Long gymId, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.types = 'FAMILIALE'")
    BigDecimal getChiffreAffairesTotalFamilial();
    // === MÉTHODES MANQUANTES POUR LA GESTION FAMILIALE ===

    /**
     * Trouve les abonnements par famille et statut
     */
    List<Abonnement> findByFamilleAndStatut(Famille famille, StatutAbonnement statut);

    /**
     * Trouve les abonnements par famille ID et statut
     */
    @Query("SELECT a FROM Abonnement a WHERE a.famille.id = :familleId AND a.statut = :statut")
    List<Abonnement> findByFamilleIdAndStatut(@Param("familleId") Long familleId, @Param("statut") StatutAbonnement statut);

    /**
     * Trouve le premier abonnement actif d'une famille
     */
    @Query("SELECT a FROM Abonnement a WHERE a.famille = :famille AND a.statut = 'EN_COURS' ORDER BY a.dateDebutAbonnement DESC")
    Optional<Abonnement> findFirstByFamilleAndStatutActif(@Param("famille") Famille famille);

    /**
     * Trouve les abonnements par statut et date de rappel
     */
    List<Abonnement> findByStatutAndDateRappelFinAbonnement(StatutAbonnement statut, LocalDate dateRappel);

    /**
     * Trouve les abonnements par statut et date de fin avant une date donnée
     */
    List<Abonnement> findByStatutAndDateFinAbonnementBefore(StatutAbonnement statut, LocalDate date);

    /**
     * Trouve les abonnements par type, statut et période de date de fin
     */
    List<Abonnement> findByTypesAndStatutAndDateFinAbonnementBetween(TypeAbonnements types, StatutAbonnement statut, LocalDate startDate, LocalDate endDate);
    // === AJOUTER CES MÉTHODES MANQUANTES ===

/**
 * 🔥 MÉTHODES MANQUANTES POUR LA GESTION FAMILIALE
 */

    /**
     * Trouve les abonnements par famille et type
     */
    List<Abonnement> findByFamilleAndTypes(Famille famille, TypeAbonnements types);

    /**
     * Trouve les abonnements par membre, type et statut
     */
    List<Abonnement> findByMembreAndTypesAndStatut(User membre, TypeAbonnements types, StatutAbonnement statut);

    /**
     * Trouve les abonnements familiaux par famille ID
     */
    @Query("SELECT a FROM Abonnement a WHERE a.famille.id = :familleId AND a.types = 'FAMILIALE'")
    List<Abonnement> findAbonnementsFamiliauxByFamilleId(@Param("familleId") Long familleId);

    /**
     * Trouve les abonnements familiaux actifs par famille ID
     */
    @Query("SELECT a FROM Abonnement a WHERE a.famille.id = :familleId AND a.types = 'FAMILIALE' AND a.statut = 'EN_COURS'")
    List<Abonnement> findAbonnementsFamiliauxActifsByFamilleId(@Param("familleId") Long familleId);

    /**
     * Vérifie si une famille a un abonnement familial actif
     */
    @Query("SELECT COUNT(a) > 0 FROM Abonnement a WHERE a.famille.id = :familleId AND a.types = 'FAMILIALE' AND a.statut = 'EN_COURS'")
    boolean existsAbonnementFamilialActifByFamilleId(@Param("familleId") Long familleId);

    /**
     * Trouve le dernier abonnement familial d'une famille
     */
    @Query("SELECT a FROM Abonnement a WHERE a.famille.id = :familleId AND a.types = 'FAMILIALE' ORDER BY a.dateDebutAbonnement DESC LIMIT 1")
    Optional<Abonnement> findLatestAbonnementFamilialByFamilleId(@Param("familleId") Long familleId);

    /**
     * Trouve les abonnements par membre et type (pour vérifier les doublons)
     */
    List<Abonnement> findByMembreAndTypes(User membre, TypeAbonnements types);

    /**
     * Trouve les abonnements expirant bientôt (dans les 7 jours)
     */
    @Query("SELECT a FROM Abonnement a WHERE a.statut = 'EN_COURS' AND a.dateFinAbonnement BETWEEN :today AND :in7Days")
    List<Abonnement> findAbonnementsExpirantBientot(@Param("today") LocalDate today, @Param("in7Days") LocalDate in7Days);

    /**
     * Compte les abonnements familiaux actifs par gym
     */
    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.types = 'FAMILIALE' AND a.statut = 'EN_COURS'")
    long countAbonnementsFamiliauxActifsByGymId(@Param("gymId") Long gymId);

    /**
     * Trouve les abonnements par période et statut
     */
    List<Abonnement> findByPeriodAbonnementAndStatut(PeriodAbonnement periodAbonnement, StatutAbonnement statut);

// === MÉTHODES POUR LES STATISTIQUES ===

    /**
     * Chiffre d'affaires total des abonnements familiaux par gym
     */
    @Query("SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.types = 'FAMILIALE' AND a.gym.id = :gymId")
    BigDecimal getChiffreAffairesFamilialByGymId(@Param("gymId") Long gymId);

    /**
     * Chiffre d'affaires mensuel des abonnements familiaux
     */
    @Query("SELECT COALESCE(SUM(a.prixAbonnement), 0) FROM Abonnement a WHERE a.types = 'FAMILIALE' AND a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = :year AND MONTH(a.dateDebutAbonnement) = :month")
    BigDecimal getChiffreAffairesFamilialMensuel(@Param("gymId") Long gymId, @Param("year") int year, @Param("month") int month);

    /**
     * ✅ REMPLACER par cette méthode corrigée
     * Compte les membres couverts par des abonnements familiaux actifs
     */
    @Query("SELECT COUNT(DISTINCT m.id) FROM User m " +
            "JOIN m.famille f " +
            "JOIN Abonnement a ON a.famille.id = f.id " +
            "WHERE a.types = 'FAMILIALE' AND a.statut = 'EN_COURS' AND a.gym.id = :gymId")
    Long countMembresCouvertsParAbonnementsFamiliaux(@Param("gymId") Long gymId);
}