package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.PeriodAbonnement; // IMPORT AJOUTÉ
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeAbonnements;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AbonnementDTO {
    private Long id;
    private PeriodAbonnement periodAbonnement; // CORRIGÉ : Changé de String à PeriodAbonnement
    private TypeAbonnements types;
    private BigDecimal nombreDeMois;
    private BigDecimal prixAbonnement;
    private ModeDePaiement modeDePaiement;
    private StatutAbonnement statut;
    private LocalDate dateDebutAbonnement;
    private LocalDate dateFinAbonnement;
    private LocalDate dateRappelFinAbonnement;
    private LocalDate dateMiseEnPause;
    private LocalDate dateResiliation;

    // Informations du membre
    private Long membreId;
    private String nomMembre;

    // Informations de la famille
    private Long familleId;
    private String nomFamille;

    // Informations du gym
    private Long gymId;
    private String nomGym;

    // Informations du type de service
    private Long typeDeServiceId;
    private String nomTypeDeService;

    // Constructeur par défaut
    public AbonnementDTO() {}

    // 🔥 CONSTRUCTEUR CORRIGÉ POUR LES REQUÊTES JPQL - 10 paramètres
    public AbonnementDTO(Long id, PeriodAbonnement periodAbonnement, TypeAbonnements types, // CORRIGÉ : PeriodAbonnement au lieu de String
                         BigDecimal nombreDeMois, BigDecimal prixAbonnement,
                         ModeDePaiement modeDePaiement, StatutAbonnement statut,
                         LocalDate dateDebutAbonnement, LocalDate dateFinAbonnement,
                         Long typeDeServiceId) {
        this.id = id;
        this.periodAbonnement = periodAbonnement; // CORRIGÉ : Plus besoin de conversion
        this.types = types;
        this.nombreDeMois = nombreDeMois;
        this.prixAbonnement = prixAbonnement;
        this.modeDePaiement = modeDePaiement;
        this.statut = statut;
        this.dateDebutAbonnement = dateDebutAbonnement;
        this.dateFinAbonnement = dateFinAbonnement;
        this.typeDeServiceId = typeDeServiceId;

        // Initialiser les autres champs avec des valeurs par défaut
        this.dateRappelFinAbonnement = null;
        this.dateMiseEnPause = null;
        this.dateResiliation = null;
        this.membreId = null;
        this.nomMembre = null;
        this.familleId = null;
        this.nomFamille = null;
        this.gymId = null;
        this.nomGym = null;
        this.nomTypeDeService = null;
    }

    // Getters et Setters complets
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // CORRIGÉ : Getters et Setters pour PeriodAbonnement
    public PeriodAbonnement getPeriodAbonnement() { return periodAbonnement; }
    public void setPeriodAbonnement(PeriodAbonnement periodAbonnement) { this.periodAbonnement = periodAbonnement; }

    public TypeAbonnements getTypes() { return types; }
    public void setTypes(TypeAbonnements types) { this.types = types; }

    public BigDecimal getNombreDeMois() { return nombreDeMois; }
    public void setNombreDeMois(BigDecimal nombreDeMois) { this.nombreDeMois = nombreDeMois; }

    public BigDecimal getPrixAbonnement() { return prixAbonnement; }
    public void setPrixAbonnement(BigDecimal prixAbonnement) { this.prixAbonnement = prixAbonnement; }

    public ModeDePaiement getModeDePaiement() { return modeDePaiement; }
    public void setModeDePaiement(ModeDePaiement modeDePaiement) { this.modeDePaiement = modeDePaiement; }

    public StatutAbonnement getStatut() { return statut; }
    public void setStatut(StatutAbonnement statut) { this.statut = statut; }

    public LocalDate getDateDebutAbonnement() { return dateDebutAbonnement; }
    public void setDateDebutAbonnement(LocalDate dateDebutAbonnement) { this.dateDebutAbonnement = dateDebutAbonnement; }

    public LocalDate getDateFinAbonnement() { return dateFinAbonnement; }
    public void setDateFinAbonnement(LocalDate dateFinAbonnement) { this.dateFinAbonnement = dateFinAbonnement; }

    public LocalDate getDateRappelFinAbonnement() { return dateRappelFinAbonnement; }
    public void setDateRappelFinAbonnement(LocalDate dateRappelFinAbonnement) { this.dateRappelFinAbonnement = dateRappelFinAbonnement; }

    public LocalDate getDateMiseEnPause() { return dateMiseEnPause; }
    public void setDateMiseEnPause(LocalDate dateMiseEnPause) { this.dateMiseEnPause = dateMiseEnPause; }

    public LocalDate getDateResiliation() { return dateResiliation; }
    public void setDateResiliation(LocalDate dateResiliation) { this.dateResiliation = dateResiliation; }

    public Long getMembreId() { return membreId; }
    public void setMembreId(Long membreId) { this.membreId = membreId; }

    public String getNomMembre() { return nomMembre; }
    public void setNomMembre(String nomMembre) { this.nomMembre = nomMembre; }

    public Long getFamilleId() { return familleId; }
    public void setFamilleId(Long familleId) { this.familleId = familleId; }

    public String getNomFamille() { return nomFamille; }
    public void setNomFamille(String nomFamille) { this.nomFamille = nomFamille; }

    public Long getGymId() { return gymId; }
    public void setGymId(Long gymId) { this.gymId = gymId; }

    public String getNomGym() { return nomGym; }
    public void setNomGym(String nomGym) { this.nomGym = nomGym; }

    public Long getTypeDeServiceId() { return typeDeServiceId; }
    public void setTypeDeServiceId(Long typeDeServiceId) { this.typeDeServiceId = typeDeServiceId; }

    public String getNomTypeDeService() { return nomTypeDeService; }
    public void setNomTypeDeService(String nomTypeDeService) { this.nomTypeDeService = nomTypeDeService; }

    @Override
    public String toString() {
        return "AbonnementDTO{" +
                "id=" + id +
                ", periodAbonnement=" + periodAbonnement + // CORRIGÉ : Plus de guillemets
                ", types=" + types +
                ", nombreDeMois=" + nombreDeMois +
                ", prixAbonnement=" + prixAbonnement +
                ", modeDePaiement=" + modeDePaiement +
                ", statut=" + statut +
                ", dateDebutAbonnement=" + dateDebutAbonnement +
                ", dateFinAbonnement=" + dateFinAbonnement +
                ", typeDeServiceId=" + typeDeServiceId +
                '}';
    }
}