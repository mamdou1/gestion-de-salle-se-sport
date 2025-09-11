package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.AbonnementGym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbonnementGymRepository extends JpaRepository<AbonnementGym, Long> {
    List<AbonnementGym> findByGymOrderByDateDebutAbonnementDesc(Gym gym);
}
