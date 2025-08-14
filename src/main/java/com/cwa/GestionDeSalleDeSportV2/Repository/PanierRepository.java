package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutPanier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PanierRepository extends JpaRepository<Panier, Long> {
    @Query(value = "SELECT DISTINCT p.* FROM panier p JOIN ligne_vente lv ON lv.panier_id = p.id JOIN produit pr ON pr.id = lv.produit_id WHERE p.statut = :statut AND pr.gym_id = :gymId", nativeQuery = true)
    List<Panier> findByStatutAndProductGym(String statut, Long gymId);

}
