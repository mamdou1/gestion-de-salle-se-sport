package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

// (exemple de salle vestiere 1, vestiere 2, cardio, altophilie, etc)

@Entity
public class Salle {

    @Id
    @GeneratedValue
    private Long id;

    private String nom;

    @ManyToOne
    @JsonManagedReference
    private Gym gym;

    @ManyToOne
    @JsonBackReference
    private Casier casier;


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

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public Casier getCasier() {
        return casier;
    }

    public void setCasier(Casier casier) {
        this.casier = casier;
    }
}