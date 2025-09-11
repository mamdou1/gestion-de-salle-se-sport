package com.cwa.GestionDeSalleDeSportV2.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeDeService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du service est obligatoire")
    private String nom;

    @Positive(message = "Le tarif pour homme doit être positif")
    private BigDecimal tarifHomme;

    @Positive(message = "Le tarif pour femme doit être positif")
    private BigDecimal tarifFemme;

    @Positive(message = "Le tarif unique doit être positif")
    private BigDecimal tarifUnique;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym; // Lien avec la gym pour multi-salle

    private BigDecimal fraisInscription;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public BigDecimal getTarifHomme() {
        return tarifHomme;
    }

    public void setTarifHomme(BigDecimal tarifHomme) {
        this.tarifHomme = tarifHomme;
    }

    public BigDecimal getTarifFemme() {
        return tarifFemme;
    }

    public void setTarifFemme(BigDecimal tarifFemme) {
        this.tarifFemme = tarifFemme;
    }

    public BigDecimal getTarifUnique() {
        return tarifUnique;
    }

    public void setTarifUnique(BigDecimal tarifUnique) {
        this.tarifUnique = tarifUnique;
    }

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public BigDecimal getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(BigDecimal fraisInscription) {
        this.fraisInscription = fraisInscription;
    }
}