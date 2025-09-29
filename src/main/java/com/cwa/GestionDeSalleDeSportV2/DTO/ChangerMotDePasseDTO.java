package com.cwa.GestionDeSalleDeSportV2.DTO;

import lombok.Data;

@Data
public class ChangerMotDePasseDTO {
    private String ancienMotDePasse;
    private String nouveauMotDePasse;
    private String confirmerMotDePasse;


    public String getAncienMotDePasse() {
        return ancienMotDePasse;
    }

    public void setAncienMotDePasse(String ancienMotDePasse) {
        this.ancienMotDePasse = ancienMotDePasse;
    }

    public String getNouveauMotDePasse() {
        return nouveauMotDePasse;
    }

    public void setNouveauMotDePasse(String nouveauMotDePasse) {
        this.nouveauMotDePasse = nouveauMotDePasse;
    }

    public String getConfirmerMotDePasse() {
        return confirmerMotDePasse;
    }

    public void setConfirmerMotDePasse(String confirmerMotDePasse) {
        this.confirmerMotDePasse = confirmerMotDePasse;
    }
}
