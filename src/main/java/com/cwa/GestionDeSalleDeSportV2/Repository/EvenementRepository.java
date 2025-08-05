package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByGymIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(Long gymId, LocalDateTime start, LocalDateTime end);
}
