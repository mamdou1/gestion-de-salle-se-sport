package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutEvent;

import java.time.LocalDateTime;

public class EvennenemtUpdateDTO {

    private String nom;
    private String description;
    private StatutEvent statutEvent;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Long createdById;


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StatutEvent getStatutEvent() {
        return statutEvent;
    }

    public void setStatutEvent(StatutEvent statutEvent) {
        this.statutEvent = statutEvent;
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

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}
