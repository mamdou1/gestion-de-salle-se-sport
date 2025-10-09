package com.cwa.GestionDeSalleDeSportV2.Entity;

import java.util.List;

public class Statistiques {
    private long totalMembres;
    private long totalEvenements;
    private long totalVentes;
    private long abonnementsActifs;
    private double revenusMensuels;
    private List<MoisCount> membresParMois;

    // Constructeur vide
    public Statistiques() {}

    // Constructeur avec paramètres
    public Statistiques(long totalMembres, long totalEvenements, long totalVentes, long abonnementsActifs,
                        double revenusMensuels, List<MoisCount> membresParMois) {
        this.totalMembres = totalMembres;
        this.totalEvenements = totalEvenements;
        this.totalVentes = totalVentes;
        this.abonnementsActifs = abonnementsActifs;
        this.revenusMensuels = revenusMensuels;
        this.membresParMois = membresParMois;
    }

    // Getters et setters
    public long getTotalMembres() {
        return totalMembres;
    }

    public void setTotalMembres(long totalMembres) {
        this.totalMembres = totalMembres;
    }

    public long getTotalEvenements() {
        return totalEvenements;
    }

    public void setTotalEvenements(long totalEvenements) {
        this.totalEvenements = totalEvenements;
    }

    public long getTotalVentes() {
        return totalVentes;
    }

    public void setTotalVentes(long totalVentes) {
        this.totalVentes = totalVentes;
    }

    public long getAbonnementsActifs() {
        return abonnementsActifs;
    }

    public void setAbonnementsActifs(long abonnementsActifs) {
        this.abonnementsActifs = abonnementsActifs;
    }

    public double getRevenusMensuels() {
        return revenusMensuels;
    }

    public void setRevenusMensuels(double revenusMensuels) {
        this.revenusMensuels = revenusMensuels;
    }

    public List<MoisCount> getMembresParMois() {
        return membresParMois;
    }

    public void setMembresParMois(List<MoisCount> membresParMois) {
        this.membresParMois = membresParMois;
    }
}