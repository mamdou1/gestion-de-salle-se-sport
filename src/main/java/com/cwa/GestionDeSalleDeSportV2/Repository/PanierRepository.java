package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PanierRepository extends JpaRepository<Panier, Long> {

    @Query(value = "SELECT DISTINCT p.* FROM panier p " +
            "JOIN ligne_vente lv ON lv.panier_id = p.id " +
            "JOIN produit pr ON pr.id = lv.produit_id " +
            "WHERE p.statut = :statut AND pr.gym_id = :gymId",
            nativeQuery = true)
    List<Panier> findByStatutAndProductGym(String statut, Long gymId);

    // HISTORIQUE POUR LE MEMBRE (Flutter)
    List<Panier> findByMembreIdOrderByDateCreationDesc(Long membreId);

    // Bonus : exclure les paniers en brouillon (si tu as un statut "BROUILLON")
    default List<Panier> findSentPaniersByMembreId(Long membreId) {
        return findByMembreIdAndStatutNotOrderByDateCreationDesc(membreId, "BROUILLON");
    }

    List<Panier> findByMembreIdAndStatutNotOrderByDateCreationDesc(Long membreId, String statut);

    // Bonus : compteur pour badge "paniers en attente"
    long countByMembreIdAndStatut(Long membreId, String statut);
}