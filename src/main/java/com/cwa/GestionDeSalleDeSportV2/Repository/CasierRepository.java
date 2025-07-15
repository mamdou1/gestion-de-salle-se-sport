package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Casier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutCasier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Salle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface CasierRepository extends JpaRepository<Casier, Long> {

    List<Casier> findByGymAndStatut(Gym gym, StatutCasier statut);

    List<Casier> findBySalle(Salle salle);

    List<Casier> findBySalleAndStatut(Salle salle, StatutCasier statutCasier);

    Optional<Casier> findByNumeroDeCasierAndSalle(String numeroDeCasier, Salle salle);
}
