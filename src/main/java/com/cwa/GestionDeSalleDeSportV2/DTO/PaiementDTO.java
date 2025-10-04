package com.cwa.GestionDeSalleDeSportV2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementDTO {
    private Long id;
    private String typePaiement; // "ABONNEMENT", "FRAIS_INSCRIPTION", "CASIER", "VENTE"
    private LocalDateTime datePaiement;
    private BigDecimal montant;
    private String modePaiement;
    //private String statut;

    // Informations de l'acheteur (peuvent être null pour les ventes)
    private Long acheteurId;
    private String acheteurNom;
    private String acheteurPrenom;
    private String acheteurTelephone;
    private String acheteurEmail;

    // Informations supplémentaires selon le type
    private String details; // Description du service/produit
    private Long referenceId; // ID de l'abonnement, vente, etc.
    private String gymNom; // Nom du gym

    // Staff qui a enregistré le paiement
    private String staffNom;
    private String staffPrenom;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public Long getAcheteurId() {
        return acheteurId;
    }

    public void setAcheteurId(Long acheteurId) {
        this.acheteurId = acheteurId;
    }

    public String getAcheteurNom() {
        return acheteurNom;
    }

    public void setAcheteurNom(String acheteurNom) {
        this.acheteurNom = acheteurNom;
    }

    public String getAcheteurPrenom() {
        return acheteurPrenom;
    }

    public void setAcheteurPrenom(String acheteurPrenom) {
        this.acheteurPrenom = acheteurPrenom;
    }

    public String getAcheteurTelephone() {
        return acheteurTelephone;
    }

    public void setAcheteurTelephone(String acheteurTelephone) {
        this.acheteurTelephone = acheteurTelephone;
    }

    public String getAcheteurEmail() {
        return acheteurEmail;
    }

    public void setAcheteurEmail(String acheteurEmail) {
        this.acheteurEmail = acheteurEmail;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getGymNom() {
        return gymNom;
    }

    public void setGymNom(String gymNom) {
        this.gymNom = gymNom;
    }

    public String getStaffNom() {
        return staffNom;
    }

    public void setStaffNom(String staffNom) {
        this.staffNom = staffNom;
    }

    public String getStaffPrenom() {
        return staffPrenom;
    }

    public void setStaffPrenom(String staffPrenom) {
        this.staffPrenom = staffPrenom;
    }
}