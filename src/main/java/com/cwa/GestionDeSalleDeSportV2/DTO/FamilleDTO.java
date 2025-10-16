package com.cwa.GestionDeSalleDeSportV2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FamilleDTO {

    private Long id; // Ajout pour inclure l'ID de la famille

    @NotBlank(message = "Le nom de la famille est obligatoire")
    private String nom;

    @NotNull(message = "L'ID du chef de famille est obligatoire")
    private Long chefFamilleId;

    private String chefFamilleNomPrenom; // Ajout pour le frontend

    private List<Long> membresId;

    private List<String> membreNomPrenoms; // Ajout pour le frontend

    private Long gymId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Long getChefFamilleId() {
        return chefFamilleId;
    }

    public void setChefFamilleId(Long chefFamilleId) {
        this.chefFamilleId = chefFamilleId;
    }

    public String getChefFamilleNomPrenom() {
        return chefFamilleNomPrenom;
    }

    public void setChefFamilleNomPrenom(String chefFamilleNomPrenom) {
        this.chefFamilleNomPrenom = chefFamilleNomPrenom;
    }

    public List<Long> getMembresId() {
        return membresId;
    }

    public void setMembresId(List<Long> membresId) {
        this.membresId = membresId;
    }

    public List<String> getMembreNomPrenoms() {
        return membreNomPrenoms;
    }

    public void setMembreNomPrenoms(List<String> membreNomPrenoms) {
        this.membreNomPrenoms = membreNomPrenoms;
    }

    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }
}