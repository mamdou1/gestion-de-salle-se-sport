package com.cwa.GestionDeSalleDeSportV2.DTO;

import java.math.BigDecimal;

public class AssignerCasierDTO {
    private Long salleId;
    private Long membreId;
    private BigDecimal prix;


    public Long getSalleId() {
        return salleId;
    }

    public void setSalleId(Long salleId) {
        this.salleId = salleId;
    }

    public Long getMembreId() {
        return membreId;
    }

    public void setMembreId(Long membreId) {
        this.membreId = membreId;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }
}
