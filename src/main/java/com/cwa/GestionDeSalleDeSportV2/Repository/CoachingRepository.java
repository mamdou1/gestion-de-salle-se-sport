package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Coaching;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CoachingRepository extends JpaRepository<Coaching, Long> {
    List<Coaching> findByGymIdAndDateDebutGreaterThanEqualAndDateFinLessThanEqual(Long gymId, LocalDateTime start, LocalDateTime end);
}
