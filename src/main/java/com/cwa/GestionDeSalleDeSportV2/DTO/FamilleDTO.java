package com.cwa.GestionDeSalleDeSportV2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FamilleDTO {

    @NotBlank(message = "Le nom de la famille est obligatoire")
    private String nom;

    @NotNull(message = "L'ID du chef de famille est obligatoire")
    private Long chefFamilleId;

    private List<Long> membresId;

    private Long gymId; // Ajout pour associer à un gym


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

    public List<Long> getMembresId() {
        return membresId;
    }

    public void setMembresId(List<Long> membresId) {
        this.membresId = membresId;
    }

    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }
}

