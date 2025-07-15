package com.cwa.GestionDeSalleDeSportV2.DTO;

import java.math.BigDecimal;

public class RenouvelerAbonnementDTO {
    private Long id;
    private Integer ajoutMois;
    private Double nouveauAbonnement;

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
