package com.cwa.GestionDeSalleDeSportV2.Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Statistiques {
    @Id
    private Long id;

    private Long totalMembres;
    private Long totalAbonnements;
    private Long totalPaiements;
    private Long totalServices;
    private Double moyenneMontant;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoisCount> moisCounts = new ArrayList<>();

    // Constructeurs
    public Statistiques() {
    }

    public Statistiques(Long totalMembres, Long totalAbonnements, Long totalPaiements, Long totalServices,
                        Double moyenneMontant, List<MoisCount> moisCounts) {
        this.totalMembres = totalMembres;
        this.totalAbonnements = totalAbonnements;
        this.totalPaiements = totalPaiements;
        this.totalServices = totalServices;
        this.moyenneMontant = moyenneMontant;
        this.moisCounts = moisCounts;
    }
}