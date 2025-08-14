package com.cwa.GestionDeSalleDeSportV2.Entity;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    private String titre;
    private String contenu;

    @Enumerated(EnumType.STRING)
    private TypeNotification typeNotification; // EMAIL, PUSH, etc.

    private boolean estLu;

    private LocalDateTime dateEnvoi;

    @ManyToOne
    @JsonManagedReference
    private User destinataire;

    private String contexte; // ABONNEMENT, VENTE, STOCK


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public TypeNotification getTypeNotification() {
        return typeNotification;
    }

    public void setTypeNotification(TypeNotification typeNotification) {
        this.typeNotification = typeNotification;
    }

    public boolean isEstLu() {
        return estLu;
    }

    public void setEstLu(boolean estLu) {
        this.estLu = estLu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public User getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(User destinataire) {
        this.destinataire = destinataire;
    }

    public String getContexte() {
        return contexte;
    }

    public void setContexte(String contexte) {
        this.contexte = contexte;
    }
}
