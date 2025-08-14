package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ValidationInscriptionDTO {

    private Long demandeId;
    private PeriodAbonnement periodAbonnement;
    private TypeAbonnements types;
    private ModeDePaiement modeDePaiement;
    private BigDecimal prixAbonnement;
    private BigInteger nombreDeMois;
    private BigDecimal fraisInscription;
    private Long gymId; // Ajout pour associer à un gym

    public PeriodAbonnement getPeriodAbonnement() {
        return periodAbonnement;
    }

    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) {
        this.periodAbonnement = periodAbonnement;
    }

    public Long getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Long demandeId) {
        this.demandeId = demandeId;
    }

    public TypeAbonnements getTypes() {
        return types;
    }

    public void setTypes(TypeAbonnements types) {
        this.types = types;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
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

    public BigDecimal getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(BigDecimal fraisInscription) {
        this.fraisInscription = fraisInscription;
    }

    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }
}
