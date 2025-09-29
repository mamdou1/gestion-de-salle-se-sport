package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement,Long> {
    List<Abonnement> findByMembreOrderByDateDebutAbonnementDesc(User membre);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement = :date")
    long countByGymIdAndDate(Long gymId, LocalDate date);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement >= :startOfWeek AND a.dateDebutAbonnement <= :endOfWeek")
    long countByGymIdAndWeek(Long gymId, LocalDate startOfWeek, LocalDate endOfWeek);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = YEAR(:date) AND MONTH(a.dateDebutAbonnement) = MONTH(:date)")
    long countByGymIdAndMonth(Long gymId, LocalDate date);

    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = YEAR(:date)")
    long countByGymIdAndYear(Long gymId, LocalDate date);

    List<Abonnement> findByGymId(Long gymId);
    List<Abonnement> findByFamilleAndTypes(Famille famille, TypeAbonnements typeAbonnements);
    List<Abonnement> findByMembreAndStatut(User membre, StatutAbonnement statutAbonnement);
    List<Abonnement> findByGymIn(List<Gym> gyms);
    Abonnement findByFamille(Famille famille);
}
