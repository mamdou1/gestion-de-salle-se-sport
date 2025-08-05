package com.cwa.GestionDeSalleDeSportV2.DTO;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class FamilleAbonnementDTO {

    @NotNull(message = "L'ID de la famille est obligatoire")
    private Long familleId;

    @NotNull(message = "Le tarif pour homme est obligatoire")
    @Min(value = 0, message = "Le tarif pour homme doit être positif")
    private BigDecimal tarifHomme;

    @NotNull(message = "Le tarif pour femme est obligatoire")
    @Min(value = 0, message = "le tarif pour femme doit être positif")
    private BigDecimal tarifFemme;

    @NotNull(message = "Le réduction par personne est obligatoire")
    @Min(value = 0, message = "la réduction par personne doit être positif")
    private BigDecimal reductionParPersonne;

    @NotNull(message = "Le nobre de mois est obligatoire")
    @Min(value = 1, message = "Le nombre de mois doit être au moins 1")
    private Integer nombreMois;

    @NotNull( message = "Le mode payement est obligatoire")
    private ModeDePaiement modeDePaiement;


    public Long getFamilleId() {
        return familleId;
    }

    public void setFamilleId(Long familleId) {
        this.familleId = familleId;
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

    public BigDecimal getReductionParPersonne() {
        return reductionParPersonne;
    }

    public void setReductionParPersonne(BigDecimal reductionParPersonne) {
        this.reductionParPersonne = reductionParPersonne;
    }

    public Integer getNombreMois() {
        return nombreMois;
    }

    public void setNombreMois(Integer nombreMois) {
        this.nombreMois = nombreMois;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }
}
