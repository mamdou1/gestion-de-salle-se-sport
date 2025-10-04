package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATE")
    private LocalDate dateVente;

    @ManyToOne
    @JsonManagedReference
    private User membre;

    @ManyToOne
    @JsonManagedReference
    private User staff;

    private BigDecimal montantTotal;

    @Enumerated(EnumType.STRING)
    private ModeDePaiement modeDePaiement;

    private Long gym_id; // Lien avec la gym pour multi-salle

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LigneVente> lignes = new ArrayList<>();

    // Calcul dynamique du montant total (pas stocké en base)
//    @PostLoad
//    public void calculerMontantTotal() {
//        montantTotal = lignes.stream()
//                .map(LigneVente::getPrixTotal)
//                .filter(Objects::nonNull) // Filtrer les valeurs null
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDate dateVente) {
        this.dateVente = dateVente;
    }

    public User getMembre() {
        return membre;
    }

    public void setMembre(User membre) {
        this.membre = membre;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }

    public Long getGym_id() {
        return gym_id;
    }

    public void setGym_id(Long gym_id) {
        this.gym_id = gym_id;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneVente> lignes) {
        this.lignes = lignes;
    }
}
