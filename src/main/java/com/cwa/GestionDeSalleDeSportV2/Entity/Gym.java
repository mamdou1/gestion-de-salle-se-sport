package com.cwa.GestionDeSalleDeSportV2.Entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gym {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @Email(message = "Un email valide est requis")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private  String telephone;

    //  orphanRemoval = true : supprime les users orphelins si on les retire de la liste
    @OneToMany (mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<User> propritaires;

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<DemandeInscription> demandes;

    //  Liste des salles du gym
    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Salle> salles;

    //  Liste des casiers du gym
    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Casier> casiers;

    //  Liste des événements du gym
//    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Evenement> evenements;

    //  Liste des programmes (coaching) du gym
//    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Programme> programmes;




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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<User> getPropritaires() {
        return propritaires;
    }

    public void setPropritaires(List<User> propritaires) {
        this.propritaires = propritaires;
    }

    public List<DemandeInscription> getDemandes() {
        return demandes;
    }

    public void setDemandes(List<DemandeInscription> demandes) {
        this.demandes = demandes;
    }

    public List<Salle> getSalles() {
        return salles;
    }

    public void setSalles(List<Salle> salles) {
        this.salles = salles;
    }

    public List<Casier> getCasiers() {
        return casiers;
    }

    public void setCasiers(List<Casier> casiers) {
        this.casiers = casiers;
    }
}
