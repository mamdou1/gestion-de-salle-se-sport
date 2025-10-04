package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.ListePaiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListePaimentRepository extends JpaRepository<ListePaiment, Long> {
    List<ListePaiment> findByGymId(Long gymId);
}