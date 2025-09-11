package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;

import java.math.BigInteger;


public class DemandeInscriptionDTO {

    private PeriodAbonnement periodAbonnement;
    private BigInteger nombreDeMois;
    private Long TypeDeService;




    public PeriodAbonnement getPeriodAbonnement() {
        return periodAbonnement;
    }

    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) {
        this.periodAbonnement = periodAbonnement;
    }

    public BigInteger getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(BigInteger nombreDeMois) {
        this.nombreDeMois = nombreDeMois;
    }

    public Long getTypeDeService() {
        return TypeDeService;
    }

    public void setTypeDeService(Long typeDeService) {
        TypeDeService = typeDeService;
    }
}
