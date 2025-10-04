package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RenouvelerAbonnementDTO {

    @NotNull(message = "Mode de paiement requis")
    private PeriodAbonnement periodAbonnement;
    @NotNull(message = "Mode de paiement requis")
    private Integer ajoutMois;
    @NotNull(message = "Mode de paiement requis")
    private ModeDePaiement modeDePaiement;
//    private Double nouveauAbonnement;

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

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }
}
