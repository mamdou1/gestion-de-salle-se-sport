package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VenteManuelDTO {
    private Long acheteurId; // ID d'un membre existant, ou null pour un non-membre
    private String nomAcheteur; // Nom pour un non-membre
    private String prenomAcheteur; // Prénom pour un non-membre
    private String telephoneAcheteur; // Contact pour un non-membre
    private ModeDePaiement modeDePaiement;
    private List<Long> produitIds; // IDs des produits vendus
    private List<Integer> quantites; // Quantités correspondantes
    private Genre genre;


    public List<Integer> getQuantites() {
        return quantites;
    }

    public void setQuantites(List<Integer> quantites) {
        this.quantites = quantites;
    }

    public List<Long> getProduitIds() {
        return produitIds;
    }

    public void setProduitIds(List<Long> produitIds) {
        this.produitIds = produitIds;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }

    public String getTelephoneAcheteur() {
        return telephoneAcheteur;
    }

    public void setTelephoneAcheteur(String telephoneAcheteur) {
        this.telephoneAcheteur = telephoneAcheteur;
    }

    public String getPrenomAcheteur() {
        return prenomAcheteur;
    }

    public void setPrenomAcheteur(String prenomAcheteur) {
        this.prenomAcheteur = prenomAcheteur;
    }

    public String getNomAcheteur() {
        return nomAcheteur;
    }

    public void setNomAcheteur(String nomAcheteur) {
        this.nomAcheteur = nomAcheteur;
    }

    public Long getAcheteurId() {
        return acheteurId;
    }

    public void setAcheteurId(Long acheteurId) {
        this.acheteurId = acheteurId;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
}