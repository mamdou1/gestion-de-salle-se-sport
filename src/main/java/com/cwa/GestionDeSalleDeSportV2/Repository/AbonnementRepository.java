package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface AbonnementRepository extends JpaRepository<Abonnement,Long> {
    List<Abonnement> findByMembreOrderByDateDebutAbonnementDesc(User membre);

    List<Abonnement> findByFamilleAndType(Famille famille, TypeAbonnement typeAbonnement);

    List<Abonnement> findByMembreAndStatut(User membre, StatutAbonnement statutAbonnement);
}
