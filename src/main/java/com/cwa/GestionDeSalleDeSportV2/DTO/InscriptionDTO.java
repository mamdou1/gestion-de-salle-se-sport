package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.AnotationPersonnaliser.AgeConstraint;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;

import java.time.LocalDate;

public class InscriptionDTO {

    private String nomGym;
    private String adresseGym;
    private String telephoneGym;
    private  String emailGym;
    private String proprietaireGym;
//    private String description;
//    private byte[] photo;


    private String nomAdmin;
    private String prenomAdmin;
    private String adresseAdmin;
    private String emailAdmin;
    private Genre genre;

    @AgeConstraint(min = 16, max = 80, message = "l'âge doit être comprise entre 16 et 80 ans")
    private String date_de_naissance;


    private String telephoneAdmin;
    private String passwordAdmin;


    // 🔧 Constructeur par défaut nécessaire à la désérialisation
    public InscriptionDTO() {
    }

    public InscriptionDTO(Genre genre) {
        this.genre = genre;
    }

    public String getNomGym() {
        return nomGym;
    }

    public void setNomGym(String nomGym) {
        this.nomGym = nomGym;
    }

    public String getAdresseGym() {
        return adresseGym;
    }

    public void setAdresseGym(String adresseGym) {
        this.adresseGym = adresseGym;
    }

    public String getTelephoneGym() {
        return telephoneGym;
    }

    public void setTelephoneGym(String telephoneGym) {
        this.telephoneGym = telephoneGym;
    }

    public String getProprietaireGym() {
        return proprietaireGym;
    }

    public void setProprietaireGym(String proprietaireGym) {
        this.proprietaireGym = proprietaireGym;
    }

    public String getEmailGym() {
        return emailGym;
    }

    public void setEmailGym(String emailGym) {
        this.emailGym = emailGym;
    }

    public String getNomAdmin() {
        return nomAdmin;
    }

    public void setNomAdmin(String nomAdmin) {
        this.nomAdmin = nomAdmin;
    }

    public String getPrenomAdmin() {
        return prenomAdmin;
    }

    public void setPrenomAdmin(String prenomAdmin) {
        this.prenomAdmin = prenomAdmin;
    }

    public String getAdresseAdmin() {
        return adresseAdmin;
    }

    public void setAdresseAdmin(String adresseAdmin) {
        this.adresseAdmin = adresseAdmin;
    }

    public String getEmailAdmin() {
        return emailAdmin;
    }

    public void setEmailAdmin(String emailAdmin) {
        this.emailAdmin = emailAdmin;
    }

    public String getTelephoneAdmin() {
        return telephoneAdmin;
    }

    public void setTelephoneAdmin(String telephoneAdmin) {
        this.telephoneAdmin = telephoneAdmin;
    }

    public String getPasswordAdmin() {
        return passwordAdmin;
    }

    public void setPasswordAdmin(String passwordAdmin) {
        this.passwordAdmin = passwordAdmin;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getDate_de_naissance() {
        return date_de_naissance;
    }

    public void setDate_de_naissance(String date_de_naissance) {
        this.date_de_naissance = date_de_naissance;
    }

//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public byte[] getPhoto() {
//        return photo;
//    }
//
//    public void setPhoto(byte[] photo) {
//        this.photo = photo;
//    }
}
