package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;

import java.time.LocalDate;

public class StaffDTO {

    private String nomStaff;
    private String prenomStaff;
    private String emailStaff;
    private String numeroTelephoneStaff;
    private String adresseStaff;
    private Genre genreStaff;
    private String passwordStaff;
    private Role roleStaff;
    private String date_de_naissanceStaff;
    public String getNomStaff() {
        return nomStaff;
    }

    public void setNomStaff(String nomStaff) {
        this.nomStaff = nomStaff;
    }

    public String getPrenomStaff() {
        return prenomStaff;
    }

    public void setPrenomStaff(String prenomStaff) {
        this.prenomStaff = prenomStaff;
    }

    public String getEmailStaff() {
        return emailStaff;
    }

    public void setEmailStaff(String emailStaff) {
        this.emailStaff = emailStaff;
    }

    public String getNumeroTelephoneStaff() {
        return numeroTelephoneStaff;
    }

    public void setNumeroTelephoneStaff(String numeroTelephoneStaff) {
        this.numeroTelephoneStaff = numeroTelephoneStaff;
    }

    public String getAdresseStaff() {
        return adresseStaff;
    }

    public void setAdresseStaff(String adresseStaff) {
        this.adresseStaff = adresseStaff;
    }

    public Genre getGenreStaff() {
        return genreStaff;
    }

    public void setGenreStaff(Genre genreStaff) {
        this.genreStaff = genreStaff;
    }

    public String getPasswordStaff() {
        return passwordStaff;
    }

    public void setPasswordStaff(String passwordStaff) {
        this.passwordStaff = passwordStaff;
    }

    public Role getRoleStaff() {
        return roleStaff;
    }

    public void setRoleStaff(Role roleStaff) {
        this.roleStaff = roleStaff;
    }

    public String getDate_de_naissanceStaff() {
        return date_de_naissanceStaff;
    }

    public void setDate_de_naissanceStaff(String date_de_naissanceStaff) {
        this.date_de_naissanceStaff = date_de_naissanceStaff;
    }
}
