package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement,Long> {
    List<Abonnement> findByMembreOrderByDateDebutAbonnementDesc(User membre);

    // List<Abonnement> findByFamilleAndType(Famille famille, TypeAbonnement typeAbonnement);
    //List<Abonnement> findByFamilleAndType(Famille famille, TypeAbonnements typeAbonnements);
    List<Abonnement> findByFamilleAndTypes(Famille famille, TypeAbonnements typeAbonnements);

    List<Abonnement> findByMembreAndStatut(User membre, StatutAbonnement statutAbonnement);

    List<Abonnement> findByGymIn(List<Gym> gyms);

    Abonnement findByFamille(Famille famille);
}
