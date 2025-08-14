package com.cwa.GestionDeSalleDeSportV2.Entity;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class DemandeInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String prenom;

    private String adresse;

    private String email;

    private String telephone;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    private String password;

    //@PastOrPresent(message = "La date de naissance ne peut pas être dans le futur")
    private String date_de_naissance;

    @CreationTimestamp
    private LocalDateTime dateSoumission;

    @ManyToOne
    @JoinColumn(name = "gym_id")
    @JsonBackReference
    private Gym gym;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user; // Lien avec utilisateur existant (si applicable)

    private String raisonRejet; // Raison si rejté

    @Enumerated(EnumType.STRING)
    private StatutMembre statut = StatutMembre.EN_ATTENTE_VALIDATION; //Statut

    private boolean estValidee = false;

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDate_de_naissance() {
        return date_de_naissance;
    }

    public void setDate_de_naissance(String date_de_naissance) {
        this.date_de_naissance = date_de_naissance;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public boolean isEstValidee() {
        return estValidee;
    }

    public void setEstValidee(boolean estValidee) {
        this.estValidee = estValidee;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRaisonRejet() {
        return raisonRejet;
    }

    public void setRaisonRejet(String raisonRejet) {
        this.raisonRejet = raisonRejet;
    }

    public StatutMembre getStatut() {
        return statut;
    }

    public void setStatut(StatutMembre statut) {
        this.statut = statut;
    }
}
