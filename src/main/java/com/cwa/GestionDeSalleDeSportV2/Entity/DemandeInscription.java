package com.cwa.GestionDeSalleDeSportV2.Entity;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Data
public class DemandeInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PeriodAbonnement periodAbonnement;

    @Enumerated(EnumType.STRING)
    private TypeAbonnements types = TypeAbonnements.INDIVIDUEL;

    @ManyToOne
    @JoinColumn(name = "type_de_service_id", nullable = true)
    @JsonManagedReference
    private TypeDeService typeDeService;

    private BigDecimal nombreDeMois;

    @CreationTimestamp
    private LocalDateTime dateSoumission;

    @ManyToOne
    @JoinColumn(name = "gym_id")
    @JsonManagedReference
    private Gym gym;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    private User user; // Lien avec utilisateur existant (si applicable)

    @Enumerated(EnumType.STRING)
    private StatutMembre statut = StatutMembre.EN_ATTENTE_VALIDATION; //Statut

    private boolean estValidee = false;

    private String raisonRejet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public TypeDeService getTypeDeService() {
        return typeDeService;
    }

    public void setTypeDeService(TypeDeService typeDeService) {
        this.typeDeService = typeDeService;
    }

    public BigDecimal getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(BigDecimal nombreDeMois) {
        this.nombreDeMois = nombreDeMois;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public boolean isEstValidee() {
        return estValidee;
    }

    public void setEstValidee(boolean estValidee) {
        this.estValidee = estValidee;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRaisonRejet() {
        return raisonRejet;
    }

    public void setRaisonRejet(String raisonRejet) {
        this.raisonRejet = raisonRejet;
    }

    public StatutMembre getStatut() {
        return statut;
    }

    public void setStatut(StatutMembre statut) {
        this.statut = statut;
    }
}
