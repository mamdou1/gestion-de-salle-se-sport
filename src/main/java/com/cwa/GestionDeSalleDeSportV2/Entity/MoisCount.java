package com.cwa.GestionDeSalleDeSportV2.Entity;

public class MoisCount {
    private String mois;
    private long count;

    // Constructeur vide
    public MoisCount() {}

    // Constructeur avec paramètres
    public MoisCount(String mois, long count) {
        this.mois = mois;
        this.count = count;
    }

    // Getters et setters
    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}