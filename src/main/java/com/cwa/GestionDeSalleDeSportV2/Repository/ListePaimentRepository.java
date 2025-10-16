package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListePaimentRepository extends JpaRepository<ListePaiment, Long> {
    List<ListePaiment> findByGymId(Long gymId);
    List<ListePaiment> findByGymIdAndTypePaiement(Long gymId, TypePaiement typePaiement);
}