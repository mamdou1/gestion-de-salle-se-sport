package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeIncriptionRepository extends JpaRepository<DemandeInscription, Long> {
    List<DemandeInscription> findByEstValideeFalse();
    List<DemandeInscription> findByGym(Gym gym);

    List<DemandeInscription> findByGymAndEstValideeFalse(Gym gym);

    List<DemandeInscription> findByUserId(Long userId);
}
