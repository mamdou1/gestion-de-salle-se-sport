package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
import java.time.Period;
import java.time.format.DateTimeFormatter;
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
    @JoinColumn(name = "gym_principal_id", nullable = true)
    @JsonManagedReference
    private Gym gym;

    @ManyToMany(cascade = CascadeType.ALL)
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

    private String password;

    private String date_de_naissance;

    @CreationTimestamp
    private LocalDateTime date_creation;

    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime lastLogin;
    private Boolean isOnline = false;
    private BigDecimal fraisInscription;
    private Boolean fraisInscriptionPayer = false;

    private Long staff;

    @Enumerated(EnumType.STRING)
    private ModeDePaiement modeDePaiement;

    @OneToMany(mappedBy = "membre", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Abonnement> abonnements = new ArrayList<>();

    @Column
    private String telephoneReference;

    @Column
    private LocalDate dateRetrait;

    @Enumerated(EnumType.STRING)
    private StatutMembre statut;

    @ManyToOne
    @JoinColumn(name = "famille_id")
    @JsonBackReference
    private Famille famille;

    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonBackReference
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<DemandeInscription> demandes;

    @OneToMany(mappedBy = "membre")
    @JsonBackReference
    private List<Vente> ventes;

    private boolean isVerified = false;

    private String verificationCode;

    private LocalDateTime verificationCodeExpiry;

    @ManyToOne
    @JoinColumn(name = "type_de_service_id", nullable = true)
    private TypeDeService typeDeService;

    @Column
    private String imageUrl; // Chemin de la photo (ex. "/uploads/users/123/photo.jpg")

    @Column(nullable = false)
    private boolean enabled = true;

    // === AJOUTÉ : Champ pour exposer familleId dans le JSON ===
    @Transient
    private Long familleId;

    public Long getFamilleId() {
        return famille != null ? famille.getId() : null;
    }
    // === FIN AJOUT ===

    @AssertTrue(message = "L'âge doit être compris entre 16 et 80 ans")
    public boolean isValidAge() {
        if (date_de_naissance == null || date_de_naissance.trim().isEmpty()) {
            return true;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(date_de_naissance, formatter);
            LocalDate currentDate = LocalDate.now();
            int age = Period.between(birthDate, currentDate).getYears();
            return age >= 16 && age <= 80;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
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

    public void addGym(Gym gymToAdd) {
        if (!gyms.contains(gymToAdd)) {
            gyms.add(gymToAdd);
        }
        if (this.gym == null) {
            this.gym = gymToAdd;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.telephone;
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

    public String getPassword() {
        return password;
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

    public Long getStaff() {
        return staff;
    }

    public void setStaff(Long staff) {
        this.staff = staff;
    }

    public ModeDePaiement getModeDePaiement() {
        return modeDePaiement;
    }

    public void setModeDePaiement(ModeDePaiement modeDePaiement) {
        this.modeDePaiement = modeDePaiement;
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

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    public TypeDeService getTypeDeService() {
        return typeDeService;
    }

    public void setTypeDeService(TypeDeService typeDeService) {
        this.typeDeService = typeDeService;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}