package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByGymIdAndDateDebutGreaterThanEqualAndDateFinLessThanEqual(Long gymId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(e) FROM Evenement e WHERE e.statutEvent = TERMINER")
    Long countByEvennement();
}
