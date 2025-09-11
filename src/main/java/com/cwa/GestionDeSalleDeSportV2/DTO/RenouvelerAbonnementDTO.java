package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;

import java.math.BigDecimal;

public class RenouvelerAbonnementDTO {

    private PeriodAbonnement periodAbonnement;
    private Integer ajoutMois;
    private Double nouveauAbonnement;

    public PeriodAbonnement getPeriodAbonnement() {
        return periodAbonnement;
    }

    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) {
        this.periodAbonnement = periodAbonnement;
    }

    public Integer getAjoutMois() {
        return ajoutMois;
    }

    public void setAjoutMois(Integer ajoutMois) {
        this.ajoutMois = ajoutMois;
    }

    public Double getNouveauAbonnement() {
        return nouveauAbonnement;
    }

    public void setNouveauAbonnement(Double nouveauAbonnement) {
        this.nouveauAbonnement = nouveauAbonnement;
    }
}
