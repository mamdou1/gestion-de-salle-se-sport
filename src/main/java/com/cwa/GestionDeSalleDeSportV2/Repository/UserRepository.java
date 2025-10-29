package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //User findByUsername(String username);

    Optional<User> findByTelephone(String telephone);

    List<User> findByDateRetrait(LocalDate aujourdHui);

    List<User> findByRoleIn(List<Role> admin);

    Optional<User> findByTelephoneOrEmail(String telephone, String email);

    List<User> findByStatutAndRole(StatutMembre statutMembre, Role role);

    List<User> findByGymIn(List<Gym> userGyms);

    Page<User> findByGymInAndRoleIn(List<Gym> gyms, List<Role> roles, Pageable pageable);

    User findByEmail(String email);

    User findByEmailIgnoreCase(String email);

    List<User> findByGymAndFraisInscriptionPayerTrue(Gym gym);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = MEMBRE")
    Long countByMembre();

    @Query("SELECT u FROM User u WHERE u.famille.id = :familleId")
    List<User> findByFamilleId(@Param("familleId") Long familleId);

    // =========================================================================
    // MÉTHODES ESSENTIELLES POUR LE COMPTAGE CORRECT DES MEMBRES SANS FAMILLE
    // =========================================================================

    /**
     * Compte tous les membres (utilisateurs avec rôle MEMBRE)
     */
    long countByRole(Role role);

    /**
     * Compte les membres sans famille - MÉTHODE LA PLUS IMPORTANTE
     */
    long countByFamilleIsNullAndRole(Role role);

    /**
     * Compte les membres avec famille
     */
    long countByFamilleIsNotNullAndRole(Role role);

    /**
     * Trouve tous les membres (utilisateurs avec rôle MEMBRE)
     */
    List<User> findByRole(Role role);

    /**
     * Trouve tous les membres sans famille
     */
    List<User> findByFamilleIsNullAndRole(Role role);

    /**
     * Trouve tous les membres avec famille
     */
    List<User> findByFamilleIsNotNullAndRole(Role role);

    // =========================================================================
    // MÉTHODES DE REQUÊTES PERSONNALISÉES SIMPLES ET SÛRES
    // =========================================================================

    /**
     * Compte le nombre total de membres avec une requête JPQL
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE")
    Long countTotalMembres();

    /**
     * Compte le nombre de membres sans famille avec une requête JPQL
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE AND u.famille IS NULL")
    Long countMembresSansFamille();

    /**
     * Compte le nombre de membres avec famille avec une requête JPQL
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE AND u.famille IS NOT NULL")
    Long countMembresAvecFamille();

    /**
     * Trouve les membres sans famille avec leurs détails complets
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.gym WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE AND u.famille IS NULL")
    List<User> findMembresSansFamilleWithDetails();

    /**
     * Statistiques détaillées sur les membres et les familles
     */
    @Query("SELECT " +
            "COUNT(u) as totalMembres, " +
            "COUNT(CASE WHEN u.famille IS NULL THEN 1 END) as membresSansFamille, " +
            "COUNT(CASE WHEN u.famille IS NOT NULL THEN 1 END) as membresAvecFamille " +
            "FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE")
    Object[] getStatistiquesMembresFamilles();

    /**
     * Vérification de cohérence des données entre membres et familles
     */
    @Query("SELECT " +
            "f.id as familleId, " +
            "f.nom as familleNom, " +
            "COUNT(u) as nombreMembres " +
            "FROM Famille f LEFT JOIN f.membres u " +
            "GROUP BY f.id, f.nom " +
            "ORDER BY nombreMembres DESC")
    List<Object[]> getRepartitionMembresParFamille();

    // =========================================================================
    // MÉTHODES POUR LA GESTION DES ABONNEMENTS ET SERVICES
    // =========================================================================

    /**
     * Trouve les membres sans famille avec un type de service spécifique
     */
    List<User> findByFamilleIsNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    /**
     * Compte les membres sans famille avec un type de service spécifique
     */
    long countByFamilleIsNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    /**
     * Trouve les membres avec famille avec un type de service spécifique
     */
    List<User> findByFamilleIsNotNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    /**
     * Compte les membres avec famille avec un type de service spécifique
     */
    long countByFamilleIsNotNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    // =========================================================================
    // MÉTHODES SIMPLES POUR LES RAPPORTS
    // =========================================================================

    /**
     * Rapport détaillé des membres par famille et statut
     */
    @Query("SELECT " +
            "CASE WHEN u.famille IS NULL THEN 'Sans Famille' ELSE f.nom END as groupe, " +
            "u.statut as statut, " +
            "COUNT(u) as nombre " +
            "FROM User u " +
            "LEFT JOIN u.famille f " +
            "WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "GROUP BY groupe, u.statut " +
            "ORDER BY groupe, u.statut")
    List<Object[]> getRapportMembresParFamilleEtStatut();

    /**
     * Membres récents sans famille (pour les tableaux de bord)
     */
    @Query("SELECT u FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND u.famille IS NULL " +
            "ORDER BY u.date_creation DESC")
    List<User> findMembresRecentsSansFamille();

    /**
     * Recherche de membres sans famille par nom ou prénom
     */
    @Query("SELECT u FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND u.famille IS NULL " +
            "AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :recherche, '%')))")
    List<User> searchMembresSansFamille(@Param("recherche") String recherche);
}