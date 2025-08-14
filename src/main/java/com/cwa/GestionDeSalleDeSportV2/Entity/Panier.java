package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutPanier;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne
    @JsonManagedReference
    private User membre;

    @Enumerated(EnumType.STRING)
    private StatutPanier statut = StatutPanier.EN_COURS;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneVente> lignes;


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

    public User getMembre() {
        return membre;
    }

    public void setMembre(User membre) {
        this.membre = membre;
    }

    public StatutPanier getStatut() {
        return statut;
    }

    public void setStatut(StatutPanier statut) {
        this.statut = statut;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneVente> lignes) {
        this.lignes = lignes;
    }
}
