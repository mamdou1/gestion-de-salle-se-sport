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

    Optional<User> findByTelephone(String telephone);

    List<User> findByDateRetrait(LocalDate aujourdHui);

    List<User> findByRoleIn(List<Role> admin);

    List<User> findByTelephoneOrEmail(String telephone, String email);

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

    long countByRole(Role role);

    long countByFamilleIsNullAndRole(Role role);

    long countByFamilleIsNotNullAndRole(Role role);

    List<User> findByRole(Role role);

    List<User> findByFamilleIsNullAndRole(Role role);

    List<User> findByFamilleIsNotNullAndRole(Role role);

    // =========================================================================
    // MÉTHODES DE REQUÊTES PERSONNALISÉES SIMPLES ET SÛRES
    // =========================================================================

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE")
    Long countTotalMembres();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE AND u.famille IS NULL")
    Long countMembresSansFamille();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE AND u.famille IS NOT NULL")
    Long countMembresAvecFamille();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.gym WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE AND u.famille IS NULL")
    List<User> findMembresSansFamilleWithDetails();

    @Query("SELECT " +
            "COUNT(u) as totalMembres, " +
            "COUNT(CASE WHEN u.famille IS NULL THEN 1 END) as membresSansFamille, " +
            "COUNT(CASE WHEN u.famille IS NOT NULL THEN 1 END) as membresAvecFamille " +
            "FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE")
    Object[] getStatistiquesMembresFamilles();

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

    List<User> findByFamilleIsNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    long countByFamilleIsNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    List<User> findByFamilleIsNotNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    long countByFamilleIsNotNullAndRoleAndTypeDeService(Role role, TypeDeService typeDeService);

    // =========================================================================
    // MÉTHODES SIMPLES POUR LES RAPPORTS
    // =========================================================================

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

    @Query("SELECT u FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND u.famille IS NULL " +
            "ORDER BY u.date_creation DESC")
    List<User> findMembresRecentsSansFamille();

    @Query("SELECT u FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND u.famille IS NULL " +
            "AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :recherche, '%')))")
    List<User> searchMembresSansFamille(@Param("recherche") String recherche);

    // =========================================================================
    // MÉTHODES CORRIGÉES POUR LES MEMBRES DISPONIBLES
    // =========================================================================

    @Query("SELECT u FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND (u.famille IS NULL OR u.famille.id = :familleId) " +
            "AND u.gym.id = :gymId")
    List<User> findMembresDisponiblesPourFamille(@Param("familleId") Long familleId, @Param("gymId") Long gymId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND (u.famille IS NULL OR u.famille.id = :familleId) " +
            "AND u.gym.id = :gymId")
    Long countMembresDisponiblesPourFamille(@Param("familleId") Long familleId, @Param("gymId") Long gymId);

    @Query("SELECT u FROM User u WHERE u.role = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role.MEMBRE " +
            "AND (u.famille IS NULL OR u.famille.id = :familleId)")
    List<User> findMembresSansFamilleOuDansFamille(@Param("familleId") Long familleId);

    List<User> findByGymIdAndRole(Long gymId, Role role);

    List<User> findByGymId(Long gymId);

    // ========================================================================
    // MÉTHODES CORRIGÉES (les seules qui faisaient planter l'application)
    // ========================================================================

    @Query("SELECT u FROM User u WHERE u.date_creation > :date AND u.role = :role")
    List<User> findByDateCreationAfterAndRole(@Param("date") LocalDateTime date, @Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.date_creation > :date " +
            "AND u.gym IN :gyms " +
            "AND u.role = :role")
    long countByDateCreationAfterAndGymInAndRole(
            @Param("date") LocalDateTime date,
            @Param("gyms") List<Gym> gyms,
            @Param("role") Role role);

    // ========================================================================
    // LE RESTE (inchangé)
    // ========================================================================

    List<User> findByNomContainingOrPrenomContainingAndGymInAndRole(String nom, String prenom, List<Gym> gyms, Role role);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    List<User> findByGenreAndGymInAndRole(Genre genre, List<Gym> gyms, Role role);
    List<User> findByTypeDeServiceAndGymIn(TypeDeService typeDeService, List<Gym> gyms);
    List<User> findByFamilleIsNullAndGymInAndRole(List<Gym> gyms, Role role);
    long countByGymInAndRole(List<Gym> gyms, Role role);
    long countByGenreAndGymInAndRole(Genre genre, List<Gym> gyms, Role role);
    long countByFamilleIsNullAndGymInAndRole(List<Gym> gyms, Role role);
}