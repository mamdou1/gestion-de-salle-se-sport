package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente,Long> {

    @Query("SELECT COUNT(DISTINCT v) FROM Vente v JOIN v.lignes l JOIN l.produit p WHERE p.gym.id = :gymId AND v.dateVente = :date")
    Long countByGymIdAndDate(Long gymId, LocalDate date);

    @Query("SELECT COUNT(DISTINCT v) FROM Vente v JOIN v.lignes l JOIN l.produit p WHERE p.gym.id = :gymId AND v.dateVente >= :startOfWeek AND v.dateVente <= :endOfWeek")
    Long countByGymIdAndWeek(Long gymId, LocalDate startOfWeek, LocalDate endOfWeek);

    @Query("SELECT COUNT(DISTINCT v) FROM Vente v JOIN v.lignes l JOIN l.produit p WHERE p.gym.id = :gymId AND YEAR(v.dateVente) = YEAR(:date) AND MONTH(v.dateVente) = MONTH(:date)")
    Long countByGymIdAndMonth(Long gymId, LocalDate date);

    @Query("SELECT COUNT(DISTINCT v) FROM Vente v JOIN v.lignes l JOIN l.produit p WHERE p.gym.id = :gymId AND YEAR(v.dateVente) = YEAR(:date)")
    Long countByGymIdAndYear(Long gymId, LocalDate date);

    @Query("SELECT v FROM Vente v JOIN v.lignes l JOIN l.produit p WHERE p.gym.id = :gymId")

    List<Vente> findByProductGymId(Long gymId);
    // Nouvelles méthodes pour filtrer par staff
    List<Vente> findByStaffGym(Gym gym);

    @Query("SELECT v FROM Vente v WHERE v.staff.gym = :gym")
    List<Vente> findByGymViaStaff(@Param("gym") Gym gym);

    // Méthode pour récupérer les ventes d'un staff spécifique
    List<Vente> findByStaff(User staff);
}
