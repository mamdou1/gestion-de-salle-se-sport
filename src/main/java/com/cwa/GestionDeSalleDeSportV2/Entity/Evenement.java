package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutEvent;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "evenement")
@Data
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEvent statutEvent;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;


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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StatutEvent getStatutEvent() {
        return statutEvent;
    }

    public void setStatutEvent(StatutEvent statutEvent) {
        this.statutEvent = statutEvent;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}