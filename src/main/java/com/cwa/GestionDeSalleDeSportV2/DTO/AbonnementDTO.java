package com.cwa.GestionDeSalleDeSportV2.DTO;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class AbonnementDTO {

    @NotNull(message = "Period d'abonnement requis")
    private PeriodAbonnement periodAbonnement;

//    @NotNull(message = "Type d'abonnement requis")
//    private TypeAbonnement type;

    @NotNull(message = "Type d'abonnement requis")
    private TypeAbonnements types;

    @NotNull(message = "Nombre de mois requis")
    private BigInteger nombreDeMois;

    @NotNull(message = "Prix requis")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private BigDecimal prixAbonnement;

    @NotNull(message = "Mode de paiement requis")
    private ModeDePaiement modeDePaiement;

    @NotNull(message = "ID du membre requis")
    private Long membreId;

    @NotNull(message = "ID de la salle requis")
    private Long gymId;

    public PeriodAbonnement getPeriodAbonnement() {
        return periodAbonnement;
    }

    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) {
        this.periodAbonnement = periodAbonnement;
    }

//    public TypeAbonnement getType() {
//        return type;
//    }
//
//    public void setType(TypeAbonnement type) {
//        this.type = type;
//    }

    public TypeAbonnements getTypes() {
        return types;
    }

    public void setTypes(TypeAbonnements types) {
        this.types = types;
    }

    public BigInteger getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(BigInteger nombreDeMois) {
        this.nombreDeMois = nombreDeMois;
    }

    public BigDecimal getPrixAbonnement() {
        return prixAbonnement;
    }

    public void setPrixAbonnement(BigDecimal prixAbonnement) {
        this.prixAbonnement = prixAbonnement;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
    }

    public Long getMembreId() {
        return membreId;
    }

    public void setMembreId(Long membreId) {
        this.membreId = membreId;
    }

    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }
}
