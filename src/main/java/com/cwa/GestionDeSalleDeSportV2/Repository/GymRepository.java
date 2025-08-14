package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GymRepository extends JpaRepository<Gym, Long> {
    @Query("SELECT g.id AS gymId, COUNT(u) AS nombreMembres FROM Gym g LEFT JOIN g.propritaires u GROUP BY g.id")
    List<Object[]> countMembresParGym();

    @Query("SELECT a.gym.id AS gymId, a.statut AS statut, COUNT(DISTINCT a.membre) AS nombreMembres " +
            "FROM Abonnement a " +
            "GROUP BY a.gym.id, a.statut")
    List<Object[]> countMembresParStatutEtGym();
}
