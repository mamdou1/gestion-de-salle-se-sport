package com.cwa.GestionDeSalleDeSportV2.DTO;

import java.util.List;

public class FamilleAvecMembresDTO {
    private Long id;
    private String nom;
    private Long chefFamilleId;
    private String chefFamilleNomPrenom;
    private List<Long> membresId;
    private List<MembreSimpleDTO> membres;
    private Long gymId;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Long getChefFamilleId() { return chefFamilleId; }
    public void setChefFamilleId(Long chefFamilleId) { this.chefFamilleId = chefFamilleId; }

    public String getChefFamilleNomPrenom() { return chefFamilleNomPrenom; }
    public void setChefFamilleNomPrenom(String chefFamilleNomPrenom) { this.chefFamilleNomPrenom = chefFamilleNomPrenom; }

    public List<Long> getMembresId() { return membresId; }
    public void setMembresId(List<Long> membresId) { this.membresId = membresId; }

    public List<MembreSimpleDTO> getMembres() { return membres; }
    public void setMembres(List<MembreSimpleDTO> membres) { this.membres = membres; }

    public Long getGymId() { return gymId; }
    public void setGymId(Long gymId) { this.gymId = gymId; }

    // Classe interne pour les membres simples
    public static class MembreSimpleDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String telephone;
        private String email;
        private String genre; // Maintenant en String

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
    }
}