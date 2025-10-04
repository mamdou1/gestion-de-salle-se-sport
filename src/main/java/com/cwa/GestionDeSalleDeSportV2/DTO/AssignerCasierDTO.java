package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;

import java.math.BigDecimal;

public class AssignerCasierDTO {
    private Long membreId;
    private ModeDePaiement modeDePaiement;
    private Long nombreDeMois;


    public Long getMembreId() {
        return membreId;
    }

    public void setMembreId(Long membreId) {
        this.membreId = membreId;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }

    public long getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(long nombreDeMois) {
        this.nombreDeMois = nombreDeMois;
    }
}
