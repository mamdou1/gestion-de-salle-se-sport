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

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    // Historique des abonnements d'un membre trié par date de début décroissante
    List<Abonnement> findByMembreOrderByDateDebutAbonnementDesc(User membre);

    // Comptage des abonnements par jour
    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement = :date")
    long countByGymIdAndDate(Long gymId, LocalDate date);

    // Récupération des abonnements par jour
    @Query("SELECT a FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement = :date")
    List<Abonnement> findByGymIdAndDate(Long gymId, LocalDate date);

    // Comptage des abonnements par semaine
    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND a.dateDebutAbonnement BETWEEN :startOfWeek AND :endOfWeek")
    long countByGymIdAndWeek(Long gymId, LocalDate startOfWeek, LocalDate endOfWeek);

    // Comptage des abonnements par mois
    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = YEAR(:date) AND MONTH(a.dateDebutAbonnement) = MONTH(:date)")
    long countByGymIdAndMonth(Long gymId, LocalDate date);

    // Comptage des abonnements par année
    @Query("SELECT COUNT(DISTINCT a) FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = YEAR(:date)")
    long countByGymIdAndYear(Long gymId, LocalDate date);

    // Récupération des abonnements par ID de gym
    List<Abonnement> findByGymId(Long gymId);

    // Récupération des abonnements par famille et type
    List<Abonnement> findByFamilleAndTypes(Famille famille, TypeAbonnements typeAbonnements);

    // Récupération des abonnements par membre et statut
    List<Abonnement> findByMembreAndStatut(User membre, StatutAbonnement statutAbonnement);

    // Récupération des abonnements pour une liste de gyms
    List<Abonnement> findByGymIn(List<Gym> gyms);

    // Récupération d'un abonnement par famille
    Optional<Abonnement> findByFamille(Famille famille);

    // Récupération des abonnements par gym
    List<Abonnement> findByGym(Gym gym);

    // Comptage des membres actifs
    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.statut = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement.EN_COURS")
    long countByMembreActifs();

    // Comptage des membres expirés
    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.statut = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement.EXPIRE")
    long countByMembreExpirer();

    // Comptage des membres bientôt expirés
    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.statut = com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement.BIENTOT_EXPIRE")
    long countByMembreBientotExpirer();

    // Récupération des abonnements par membre et type
    List<Abonnement> findByMembreAndTypes(User membre, TypeAbonnements typeAbonnements);

    // Récupération des abonnements entre deux dates pour un gym
    List<Abonnement> findByGymIdAndDateDebutAbonnementBetween(Long gymId, LocalDate startDate, LocalDate endDate);

    // Récupération des abonnements pour un mois spécifique
    @Query("SELECT a FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = :year AND MONTH(a.dateDebutAbonnement) = :month")
    List<Abonnement> findByGymIdAndMonth(Long gymId, int year, int month);

    // Récupération des abonnements pour une année spécifique
    @Query("SELECT a FROM Abonnement a WHERE a.gym.id = :gymId AND YEAR(a.dateDebutAbonnement) = :year")
    List<Abonnement> findByGymIdAndYear(Long gymId, int year);
}