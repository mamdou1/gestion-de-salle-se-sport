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

    private TypeAbonnements types;

    @NotNull(message = "Nombre de mois requis")
    private BigDecimal nombreDeMois;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private BigDecimal prixAbonnement;

    @NotNull(message = "Mode de paiement requis")
    private ModeDePaiement modeDePaiement;

    @NotNull(message = "ID du membre requis")
    private Long membreId;

    private Long gymId;

    @NotNull(message = "ID du type de service requis")
    private Long typeDeServiceId;

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

    public BigDecimal getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(BigDecimal nombreDeMois) {
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

    public Long getTypeDeServiceId() {
        return typeDeServiceId;
    }

    public void setTypeDeServiceId(Long typeDeServiceId) {
        this.typeDeServiceId = typeDeServiceId;
    }
}
