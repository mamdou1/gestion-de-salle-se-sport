package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class AbonnementGym {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @JsonManagedReference
    @ManyToOne
    private User enregistrerPar; // Le staff qui a effectué l'ajout

    @Enumerated(EnumType.STRING)
    private StatutAbonnement statut;

    private LocalDate dateDebutAbonnement;

    private LocalDate dateFinAbonnement;

    private LocalDate datePauseAbonnement;

    private LocalDate dateRappelFinAbonnement;

    private BigDecimal prixAbonnement;

    private BigInteger nombreDeMois;

    @Enumerated(EnumType.STRING)
    private ModeDePaiement modeDePaiement;

    @Enumerated(EnumType.STRING)
    private PeriodAbonnement periodAbonnement;

    private Integer joursAbsence;

    private LocalDate dateResiliation;

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


    public PeriodAbonnement getPeriodAbonnement() {
        return periodAbonnement;
    }

    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) {
        this.periodAbonnement = periodAbonnement;
    }

    public Integer getJoursAbsence() {
        return joursAbsence;
    }

    public void setJoursAbsence(Integer joursAbsence) {
        this.joursAbsence = joursAbsence;
    }

    public LocalDate getDateResiliation() {
        return dateResiliation;
    }

    public void setDateResiliation(LocalDate dateResiliation) {
        this.dateResiliation = dateResiliation;
    }
}
