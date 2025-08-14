package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutLigne;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne
    @JsonBackReference
    private Panier panier;

    @ManyToOne
    @JsonBackReference
    private Vente vente;

    @ManyToOne
    @JsonManagedReference
    private Produit produit;

    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotal;

    @Enumerated(EnumType.STRING)
    private StatutLigne statut = StatutLigne.PANIER;

    @PrePersist
    @PreUpdate
    public void calculerPrixTotal(){
        if (produit != null && prixUnitaire != null && quantite != null){
            if (produit.getQuantiteEnStock() != null && produit.getQuantiteEnStock() < quantite){
                throw new RuntimeException("Quantité en stock insuffisante pour le produit : " + produit.getNom());
            }
            prixTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
            // ise à jour de la quantité en stock
            produit.setQuantiteEnStock(produit.getQuantiteEnStock() - quantite);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Panier getPanier() {
        return panier;
    }

    public void setPanier(Panier panier) {
        this.panier = panier;
    }

    public StatutLigne getStatut() {
        return statut;
    }

    public void setStatut(StatutLigne statut) {
        this.statut = statut;
    }

    public Vente getVente() {
        return vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
    }
}
