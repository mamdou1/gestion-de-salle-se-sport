package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FamilleRepository extends JpaRepository<Famille, Long> {

    Famille findByChefFamille(User chefFamille);

    @Query("SELECT DISTINCT f FROM Famille f LEFT JOIN FETCH f.membres WHERE f.id = :id")
    Optional<Famille> findByIdWithMembres(@Param("id") Long id);

    @Query("SELECT DISTINCT f FROM Famille f LEFT JOIN FETCH f.membres")
    List<Famille> findAllWithMembres();

    @Query("SELECT DISTINCT f FROM Famille f LEFT JOIN FETCH f.membres LEFT JOIN FETCH f.chefFamille")
    List<Famille> findAllWithChefAndMembres();

    @Query("SELECT COUNT(fm) FROM User fm WHERE fm.famille.id = :familleId")
    Long countMembresByFamilleId(@Param("familleId") Long familleId);

    @Query("SELECT f FROM Famille f WHERE f.gym.id = :gymId")
    List<Famille> findByGymId(@Param("gymId") Long gymId);

    boolean existsByNomAndGymId(String nom, Long gymId);

    boolean existsByNomAndGymIdAndIdNot(String nom, Long gymId, Long excludeFamilleId);

    @Query("SELECT f FROM Famille f WHERE LOWER(f.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Famille> findByNomContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT f, COUNT(m) as nombreMembres FROM Famille f LEFT JOIN f.membres m GROUP BY f")
    List<Object[]> findAllWithMembreCount();

    @Query("SELECT f FROM Famille f WHERE f.chefFamille.id = :chefFamilleId")
    Optional<Famille> findByChefFamilleId(@Param("chefFamilleId") Long chefFamilleId);

    boolean existsByChefFamilleId(Long userId);

    @Query("SELECT DISTINCT f FROM Famille f JOIN f.membres m WHERE m.statut = 'ACTIF'")
    List<Famille> findAllWithActiveMembres();

    @Query("SELECT COUNT(f) FROM Famille f WHERE f.gym.id = :gymId")
    Long countByGymId(@Param("gymId") Long gymId);

    List<Famille> findByGym(Gym gym);
}