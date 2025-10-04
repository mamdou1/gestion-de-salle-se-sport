package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListePaiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypePaiement typePaiement; // ABONNEMENT, FRAIS_INSCRIPTION, CASIER, VENTE

    private LocalDateTime datePaiement;
    private BigDecimal montant;

//    @Enumerated(EnumType.STRING)
    //private StatutPaiement statut; // PAYE, EN_ATTENTE, ANNULE, REFUSE

    @Enumerated(EnumType.STRING)
    private ModeDePaiement modeDePaiement;


    // Référence à l'élément payé
    private Long referenceId; // ID de l'abonnement, vente, etc.

    // Informations de l'acheteur
    @ManyToOne
    private User acheteur;

    @ManyToOne
    private User staffEnregistreur;

    @ManyToOne
    private Gym gym;

    private String details;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypePaiement getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(TypePaiement typePaiement) {
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

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public User getAcheteur() {
        return acheteur;
    }

    public void setAcheteur(User acheteur) {
        this.acheteur = acheteur;
    }

    public User getStaffEnregistreur() {
        return staffEnregistreur;
    }

    public void setStaffEnregistreur(User staffEnregistreur) {
        this.staffEnregistreur = staffEnregistreur;
    }

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}