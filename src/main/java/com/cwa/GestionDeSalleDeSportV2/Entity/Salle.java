package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// (exemple de salle vestiere 1, vestiere 2, cardio, altophilie, etc)

@Entity
public class Salle {

    @Id
    @GeneratedValue
    private Long id;

    private String nom;

    @ManyToOne
    @JsonBackReference
    private Gym gym;


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
}