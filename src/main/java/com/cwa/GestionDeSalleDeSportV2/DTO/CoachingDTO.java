package com.cwa.GestionDeSalleDeSportV2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoachingDTO {
    private Long gymId;

    @NotNull(message = "L'ID du client est requis")
    private Long clientId;

    @NotNull(message = "L'ID du coach est requis")
    private Long coachId;

    @NotNull(message = "La date de début est requise")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est requise")
    private LocalDateTime dateFin;

    private BigDecimal prix;

    @NotBlank(message = "Le nom du cours est requis")
    private String nomCours;

    @NotBlank(message = "La description du cours est requis")
    private String description;


    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public String getNomCours() {
        return nomCours;
    }

    public void setNomCours(String nomCours) {
        this.nomCours = nomCours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}