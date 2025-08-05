package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Famille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la famille est obligatoire")
    private String nom;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "chef_famille_id", nullable = false)
    private User chefFamille;

    @OneToMany(mappedBy = "famille", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<User> membres;

    @OneToMany(mappedBy = "famille", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Abonnement> abonnements;

    @OneToMany(mappedBy = "famille", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<FactureCollective> factures;


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

    public User getChefFamille() {
        return chefFamille;
    }

    public void setChefFamille(User chefFamille) {
        this.chefFamille = chefFamille;
    }

    public List<User> getMembres() {
        return membres;
    }

    public void setMembres(List<User> membres) {
        this.membres = membres;
    }

    public List<Abonnement> getAbonnements() {
        return abonnements;
    }

    public void setAbonnements(List<Abonnement> abonnements) {
        this.abonnements = abonnements;
    }

    public List<FactureCollective> getFactures() {
        return factures;
    }

    public void setFactures(List<FactureCollective> factures) {
        this.factures = factures;
    }
}
