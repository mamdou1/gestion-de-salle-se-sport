package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.AnotationPersonnaliser.AgeConstraint;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class MembreDTO {

    private Long MembreId;

    @NotBlank(message = "Le nom est obligatoire")
    private String nomMembre;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenomMembre;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String emailMembre;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String numeroTelephoneMembre;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresseMembre;

    @NotNull(message = "Le genre est obligatoire")
    private Genre genreMembre;

    @NotBlank(message = "La date de naissance est obligatoire")
    @AgeConstraint(min = 16, max = 80, message = "l'âge doit être comprise entre 16 et 80 ans")
    private String date_de_naissanceMembre;

    private Role role;

    private String passwordMembre;

   // @NotNull(message = "Les frais d'inscription sont obligatoires")
    private BigDecimal fraisInscriptionMembre;

    @NotNull(message = "Le type de service est obligatoire")
    private Long typeDeService;

    private Long chefFamilleId;

    private Long gymId;

    private List<Long> gymsIds;

    public Long getMembreId() {
        return MembreId;
    }

    public void setMembreId(Long membreId) {
        MembreId = membreId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDate_de_naissanceMembre() {
        return date_de_naissanceMembre;
    }

    public void setDate_de_naissanceMembre(String date_de_naissanceMembre) {
        this.date_de_naissanceMembre = date_de_naissanceMembre;
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

    public BigDecimal getFraisInscriptionMembre() {
        return fraisInscriptionMembre;
    }

    public void setFraisInscriptionMembre(BigDecimal fraisInscriptionMembre) {
        this.fraisInscriptionMembre = fraisInscriptionMembre;
    }

    public Long getChefFamilleId() {
        return chefFamilleId;
    }

    public void setChefFamilleId(Long chefFamilleId) {
        this.chefFamilleId = chefFamilleId;
    }

    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }

    public List<Long> getGymsIds() {
        return gymsIds;
    }

    public void setGymsIds(List<Long> gymsIds) {
        this.gymsIds = gymsIds;
    }

    public Long getTypeDeService() {
        return typeDeService;
    }

    public void setTypeDeService(Long typeDeService) {
        this.typeDeService = typeDeService;
    }
}
