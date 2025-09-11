package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutCasier;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Casier {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JsonManagedReference
    private Gym gym;

    @ManyToOne
    @JsonManagedReference
    private Salle salle; // ➕ Salle où se trouve le casier

    @ManyToOne
    private User membre; // uniquement membre

    @ManyToOne
    private User staff;

    private String numeroDeCasier; // unique par salle

    private BigDecimal prix;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutCasier statut; // OCCUPER / DISPONIBLE


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public User getMembre() {
        return membre;
    }

    public void setMembre(User membre) {
        this.membre = membre;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public String getNumeroDeCasier() {
        return numeroDeCasier;
    }

    public void setNumeroDeCasier(String numeroDeCasier) {
        this.numeroDeCasier = numeroDeCasier;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutCasier getStatut() {
        return statut;
    }

    public void setStatut(StatutCasier statut) {
        this.statut = statut;
    }
}
