package com.cwa.GestionDeSalleDeSportV2.Entity;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gym_principal_id", nullable = false)
    @JsonManagedReference
    private Gym gym;

    @ManyToMany
    @JsonManagedReference
    @JoinTable(
            name = "user_gyms",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "gym_id")
    )

    private List<Gym> gyms = new ArrayList<>();
    private String nom;
    private String prenom;
    private String adresse;
    private String email;
    private String telephone;
    @Enumerated(EnumType.STRING)
    private Genre genre;

//    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
//    @Column(unique = true, nullable = false)
//    private String username;
    private String password;

    //@PastOrPresent(message = "La date de naissance ne peut pas être dans le futur")
    private String date_de_naissance;

    @CreationTimestamp // Veut dire que la date est (private LocalDateTime  date_creation = LocalDateTime.now(); )
    private LocalDateTime date_creation;

    @NotNull(message = "Le role est obligatoire")
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime lastLogin;
    private Boolean isOnline = false;
    private BigDecimal fraisInscription;
    private Boolean fraisInscriptionPayer = false;

    @OneToMany(mappedBy = "membre", cascade = CascadeType.ALL)
    private List<Abonnement> abonnements = new ArrayList<>();

    @Column
    private String telephoneReference; // Numéro de téléphone du chef de famille

    @Column
    private LocalDate dateRetrait; // Date à laquelle le retrait prend effet

    @Enumerated(EnumType.STRING)
    private StatutMembre statut; // Nouveau champ

    @ManyToOne
    @JoinColumn(name = "famille_id")
    @JsonBackReference
    private Famille famille;

    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonBackReference
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DemandeInscription> demandes;

    @OneToMany(mappedBy = "membre")
    @JsonBackReference
    private List<Vente> ventes; // Historique des ventes pour l'acheteur

    @Override
    public boolean isEnabled() {
        return true; // ou une logique basée sur un champ comme `isActive`
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Méthode pour ajouter un gym et synchroniser
    public void addGym(Gym gymToAdd) {
        if (!gyms.contains(gymToAdd)) {
            gyms.add(gymToAdd);
        }
        if (this.gym == null) {
            this.gym = gymToAdd; // Définir comme gym principal si aucun n'est défini
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Gym getGym() {
        return gym;
    }

    public void setGym(Gym gym) {
        this.gym = gym;
    }

    public List<Gym> getGyms() {
        return gyms;
    }

    public void setGyms(List<Gym> gyms) {
        this.gyms = gyms;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+this.role.name()));
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.telephone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDate_de_naissance() {
        return date_de_naissance;
    }

    public void setDate_de_naissance(String date_de_naissance) {
        this.date_de_naissance = date_de_naissance;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public LocalDateTime getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public BigDecimal getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(BigDecimal fraisInscription) {
        this.fraisInscription = fraisInscription;
    }

    public Boolean getFraisInscriptionPayer() {
        return fraisInscriptionPayer;
    }

    public void setFraisInscriptionPayer(Boolean fraisInscriptionPayer) {
        this.fraisInscriptionPayer = fraisInscriptionPayer;
    }

    public List<Abonnement> getAbonnements() {
        return abonnements;
    }

    public void setAbonnements(List<Abonnement> abonnements) {
        this.abonnements = abonnements;
    }

    public String getTelephoneReference() {
        return telephoneReference;
    }

    public void setTelephoneReference(String telephoneReference) {
        this.telephoneReference = telephoneReference;
    }

    public LocalDate getDateRetrait() {
        return dateRetrait;
    }

    public void setDateRetrait(LocalDate dateRetrait) {
        this.dateRetrait = dateRetrait;
    }

    public StatutMembre getStatut() {
        return statut;
    }

    public void setStatut(StatutMembre statut) {
        this.statut = statut;
    }

    public Famille getFamille() {
        return famille;
    }

    public void setFamille(Famille famille) {
        this.famille = famille;
    }

    public List<DemandeInscription> getDemandes() {
        return demandes;
    }

    public void setDemandes(List<DemandeInscription> demandes) {
        this.demandes = demandes;
    }

    public List<Vente> getVentes() {
        return ventes;
    }

    public void setVentes(List<Vente> ventes) {
        this.ventes = ventes;
    }
}

