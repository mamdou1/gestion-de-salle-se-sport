package com.cwa.GestionDeSalleDeSportV2.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "facture_collective")
public class FactureCollective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "famille_id")
    private Famille famille;

    // 🔥 CORRECTION : Ajout du bénéficiaire principal
    @ManyToOne
    @JoinColumn(name = "beneficiaire_principal_id")
    private User beneficiairePrincipal;

    private BigDecimal montantTotal;
    private LocalDate dateEmission;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal fraisInscriptionTotal;

    // 🔥 CORRECTION : Supprimer le ManyToMany problématique
    // @ManyToMany
    // @JoinTable(
    //         name = "facture_membres",
    //         joinColumns = @JoinColumn(name = "facture_id"),
    //         inverseJoinColumns = @JoinColumn(name = "user_id")
    // )
    // private List<User> beneficiaires;

    @OneToMany
    @JoinTable(
            name = "facture_abonnements",
            joinColumns = @JoinColumn(name = "facture_id"),
            inverseJoinColumns = @JoinColumn(name = "abonnement_id")
    )
    private List<Abonnement> abonnementsInclus;

    // Constructeurs
    public FactureCollective() {}

    public FactureCollective(Famille famille, User beneficiairePrincipal, BigDecimal montantTotal) {
        this.famille = famille;
        this.beneficiairePrincipal = beneficiairePrincipal;
        this.montantTotal = montantTotal;
        this.dateEmission = LocalDate.now();
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Famille getFamille() { return famille; }
    public void setFamille(Famille famille) { this.famille = famille; }

    public User getBeneficiairePrincipal() { return beneficiairePrincipal; }
    public void setBeneficiairePrincipal(User beneficiairePrincipal) {
        this.beneficiairePrincipal = beneficiairePrincipal;
    }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    // 🔥 CORRECTION : Supprimer ou commenter le getter/setter des bénéficiaires
    // public List<User> getBeneficiaires() { return beneficiaires; }
    // public void setBeneficiaires(List<User> beneficiaires) { this.beneficiaires = beneficiaires; }

    public List<Abonnement> getAbonnementsInclus() { return abonnementsInclus; }
    public void setAbonnementsInclus(List<Abonnement> abonnementsInclus) { this.abonnementsInclus = abonnementsInclus; }

    public BigDecimal getFraisInscriptionTotal() { return fraisInscriptionTotal; }
    public void setFraisInscriptionTotal(BigDecimal fraisInscriptionTotal) { this.fraisInscriptionTotal = fraisInscriptionTotal; }
}