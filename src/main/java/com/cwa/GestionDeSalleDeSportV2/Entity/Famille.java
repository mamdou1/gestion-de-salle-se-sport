package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Famille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la famille est obligatoire")
    private String nom;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    @JsonBackReference
    private Gym gym;

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "chef_famille_id", nullable = false)
    private User chefFamille;

    @OneToMany(mappedBy = "famille", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<User> membres;

    @OneToMany(mappedBy = "famille", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Abonnement> abonnements;

    @OneToMany(mappedBy = "famille", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<FactureCollective> factures;

    /**
     * Méthode utilitaire : renvoie le nom complet du chef de famille
     */
    public String getChefFamilleNomComplet() {
        if (chefFamille == null) return null;
        return chefFamille.getNom() + " " + chefFamille.getPrenom();
    }
}
