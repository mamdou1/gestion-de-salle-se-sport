package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;

import java.math.BigDecimal;

public class CasierDTO {

    private Long salleId;
    private String numeroDeCasier;
    private BigDecimal prix;


    public Long getSalleId() {
        return salleId;
    }

    public void setSalleId(Long salleId) {
        this.salleId = salleId;
    }

    public String getNumeroDeCasier() {
        return numeroDeCasier;
    }

    public void setNumeroDeCasier(String numeroDeCasier) {
        this.numeroDeCasier = numeroDeCasier;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }
}
