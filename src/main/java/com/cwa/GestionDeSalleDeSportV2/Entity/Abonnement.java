package com.cwa.GestionDeSalleDeSportV2.Entity;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    @JsonBackReference
    private Gym gym;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User membre;

    @ManyToOne
    @JsonBackReference
    private User enregistrerPar; // Le staff qui a effectué l'ajout

    @Enumerated(EnumType.STRING)
    private StatutAbonnement statut;

//    @Enumerated(EnumType.STRING)
//    private TypeAbonnement type;

    private LocalDate dateDebutAbonnement;

    private LocalDate dateFinAbonnement;

    private LocalDate datePauseAbonnement;

    private LocalDate dateRappelFinAbonnement;

    private BigDecimal prixAbonnement;

    private BigInteger nombreDeMois;

    @Enumerated(EnumType.STRING)
    private ModeDePaiement modeDePaiement;

    @ManyToOne
    @JoinColumn(name = "famille_id")
    @JsonBackReference
    private Famille famille;

    @Enumerated(EnumType.STRING)
    private PeriodAbonnement periodAbonnement;

    @Enumerated(EnumType.STRING)
    private TypeAbonnements types;


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

    public User getMembre() {
        return membre;
    }

    public void setMembre(User membre) {
        this.membre = membre;
    }

    public User getEnregistrerPar() {
        return enregistrerPar;
    }

    public void setEnregistrerPar(User enregistrerPar) {
        this.enregistrerPar = enregistrerPar;
    }

    public StatutAbonnement getStatut() {
        return statut;
    }

    public void setStatut(StatutAbonnement statut) {
        this.statut = statut;
    }


    public LocalDate getDateDebutAbonnement() {
        return dateDebutAbonnement;
    }

    public void setDateDebutAbonnement(LocalDate dateDebutAbonnement) {
        this.dateDebutAbonnement = dateDebutAbonnement;
    }

    public LocalDate getDateFinAbonnement() {
        return dateFinAbonnement;
    }

    public void setDateFinAbonnement(LocalDate dateFinAbonnement) {
        this.dateFinAbonnement = dateFinAbonnement;
    }

    public LocalDate getDatePauseAbonnement() {
        return datePauseAbonnement;
    }

    public void setDatePauseAbonnement(LocalDate datePauseAbonnement) {
        this.datePauseAbonnement = datePauseAbonnement;
    }

    public LocalDate getDateRappelFinAbonnement() {
        return dateRappelFinAbonnement;
    }

    public void setDateRappelFinAbonnement(LocalDate dateRappelFinAbonnement) {
        this.dateRappelFinAbonnement = dateRappelFinAbonnement;
    }

    public BigDecimal getPrixAbonnement() {
        return prixAbonnement;
    }

    public void setPrixAbonnement(BigDecimal prixAbonnement) {
        this.prixAbonnement = prixAbonnement;
    }

    public BigInteger getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(BigInteger nombreDeMois) {
        this.nombreDeMois = nombreDeMois;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }

    public Famille getFamille() {
        return famille;
    }

    public void setFamille(Famille famille) {
        this.famille = famille;
    }

    public PeriodAbonnement getPeriodAbonnement() {
        return periodAbonnement;
    }

    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) {
        this.periodAbonnement = periodAbonnement;
    }

    public TypeAbonnements getTypes() {
        return types;
    }

    public void setTypes(TypeAbonnements types) {
        this.types = types;
    }
}


