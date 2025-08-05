package com.cwa.GestionDeSalleDeSportV2.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
public class FactureCollective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Famille famille;

    private BigDecimal montantTotal;
    private LocalDate dateEmission;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal fraisInscriptionTotal; // Total des frais d'inscription

    @ManyToMany
    @JoinTable(
            name = "facture_membres",
            joinColumns = @JoinColumn(name = "facture_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> beneficiaires;

    @OneToMany
    private List<Abonnement> abonnementsInclus;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Famille getFamille() {
        return famille;
    }

    public void setFamille(Famille famille) {
        this.famille = famille;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
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

    public List<User> getBeneficiaires() {
        return beneficiaires;
    }

    public void setBeneficiaires(List<User> beneficiaires) {
        this.beneficiaires = beneficiaires;
    }

    public List<Abonnement> getAbonnementsInclus() {
        return abonnementsInclus;
    }

    public void setAbonnementsInclus(List<Abonnement> abonnementsInclus) {
        this.abonnementsInclus = abonnementsInclus;
    }

    public BigDecimal getFraisInscriptionTotal() {
        return fraisInscriptionTotal;
    }

    public void setFraisInscriptionTotal(BigDecimal fraisInscriptionTotal) {
        this.fraisInscriptionTotal = fraisInscriptionTotal;
    }
}
