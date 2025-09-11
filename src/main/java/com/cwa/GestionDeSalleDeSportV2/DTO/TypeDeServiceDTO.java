package com.cwa.GestionDeSalleDeSportV2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeDeServiceDTO {

    @NotBlank(message = "Le nom du service est obligatoire")
    private String nom;

    @Positive(message = "Le tarif pour homme doit être positif")
    private BigDecimal tarifHomme;

    @Positive(message = "Le tarif pour femme doit être positif")
    private BigDecimal tarifFemme;

    @Positive(message = "Le tarif unique doit être positif")
    private BigDecimal tarifUnique;

    // Champ optionnel pour flexibilité (peut être omis si déduit du staff)
    private Long gymId;

    @Positive(message = "Les frais d'inscription doivent être positif")
    private BigDecimal fraisInscription;


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

    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }

    public BigDecimal getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(BigDecimal fraisInscription) {
        this.fraisInscription = fraisInscription;
    }
}