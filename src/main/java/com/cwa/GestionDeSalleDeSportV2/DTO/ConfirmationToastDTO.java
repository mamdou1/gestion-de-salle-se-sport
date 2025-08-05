package com.cwa.GestionDeSalleDeSportV2.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConfirmationToastDTO {
    private String nomComplet;
    private BigDecimal fraisInscription;
    private BigDecimal montantCalcule;
    private String message;


    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public BigDecimal getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(BigDecimal fraisInscription) {
        this.fraisInscription = fraisInscription;
    }

    public BigDecimal getMontantCalcule() {
        return montantCalcule;
    }

    public void setMontantCalcule(BigDecimal montantCalcule) {
        this.montantCalcule = montantCalcule;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}