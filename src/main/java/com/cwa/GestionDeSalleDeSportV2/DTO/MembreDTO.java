package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;

import java.time.LocalDate;

public class MembreDTO {

    private String nomMembre;
    private String prenomMembre;
    private String emailMembre;
    private String numeroTelephoneMembre;
    private String adresseMembre;
    private Genre genreMembre;
    private LocalDate getDate_de_naissanceMembre;
    private Role role;
    private String passwordMembre;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getGetDate_de_naissanceMembre() {
        return getDate_de_naissanceMembre;
    }

    public void setGetDate_de_naissanceMembre(LocalDate getDate_de_naissanceMembre) {
        this.getDate_de_naissanceMembre = getDate_de_naissanceMembre;
    }

    public String getNomMembre() {
        return nomMembre;
    }

    public void setNomMembre(String nomMembre) {
        this.nomMembre = nomMembre;
    }

    public String getPrenomMembre() {
        return prenomMembre;
    }

    public void setPrenomMembre(String prenomMembre) {
        this.prenomMembre = prenomMembre;
    }

    public String getEmailMembre() {
        return emailMembre;
    }

    public void setEmailMembre(String emailMembre) {
        this.emailMembre = emailMembre;
    }

    public String getNumeroTelephoneMembre() {
        return numeroTelephoneMembre;
    }

    public void setNumeroTelephoneMembre(String numeroTelephoneMembre) {
        this.numeroTelephoneMembre = numeroTelephoneMembre;
    }

    public String getAdresseMembre() {
        return adresseMembre;
    }

    public void setAdresseMembre(String adresseMembre) {
        this.adresseMembre = adresseMembre;
    }

    public Genre getGenreMembre() {
        return genreMembre;
    }

    public void setGenreMembre(Genre genreMembre) {
        this.genreMembre = genreMembre;
    }

    public String getPasswordMembre() {
        return passwordMembre;
    }

    public void setPasswordMembre(String passwordMembre) {
        this.passwordMembre = passwordMembre;
    }
}
