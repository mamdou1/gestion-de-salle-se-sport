package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilleRepository extends JpaRepository<Famille, Long> {

    Famille findByChefFamille(User chefFamille);
}
