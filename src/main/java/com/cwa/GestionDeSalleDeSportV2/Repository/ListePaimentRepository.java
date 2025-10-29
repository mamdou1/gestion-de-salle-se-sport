package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.DTO.PaiementDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListePaimentRepository extends JpaRepository<ListePaiment, Long> {

    // Méthode existante pour les recherches par type
    List<ListePaiment> findByGymIdAndTypePaiement(Long gymId, TypePaiement typePaiement);

    // Méthode de base - gardez-la
    List<ListePaiment> findByGymId(Long gymId);

    // Méthode avec JOIN FETCH pour charger l'acheteur
    @Query("SELECT p FROM ListePaiment p LEFT JOIN FETCH p.acheteur WHERE p.gym.id = :gymId")
    List<ListePaiment> findByGymIdWithAcheteur(@Param("gymId") Long gymId);

    // Méthode pour récupérer directement les DTOs avec toutes les informations
    @Query("SELECT new com.cwa.GestionDeSalleDeSportV2.DTO.PaiementDTO(" +
            "p.id, " +
            "p.typePaiement.name(), " +
            "p.datePaiement, " +
            "p.montant, " +
            "p.modeDePaiement.name(), " +
            "p.acheteur.id, " +
            "p.acheteur.nom, " +
            "p.acheteur.prenom, " +
            "p.acheteur.telephone, " +
            "p.acheteur.email, " +
            "p.details, " +
            "p.referenceId, " +
            "p.gym.nom, " +
            "p.staffEnregistreur.nom, " +
            "p.staffEnregistreur.prenom) " +
            "FROM ListePaiment p " +
            "LEFT JOIN p.acheteur " +
            "LEFT JOIN p.staffEnregistreur " +
            "LEFT JOIN p.gym " +
            "WHERE p.gym.id = :gymId")
    List<PaiementDTO> findPaiementDTOsByGymId(@Param("gymId") Long gymId);
}