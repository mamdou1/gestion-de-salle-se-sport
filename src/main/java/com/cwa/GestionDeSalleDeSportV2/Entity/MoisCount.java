package com.cwa.GestionDeSalleDeSportV2.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class MoisCount {
    @Id
    private String mois; // Clé primaire, par exemple le nom du mois
    private Long count;  // Nombre d'occurrences

    // Constructeur sans arguments (requis pour JPA)
    public MoisCount() {
    }

    // Constructeur avec paramètres (facultatif)
    public MoisCount(String mois, Long count) {
        this.mois = mois;
        this.count = count;
    }
}