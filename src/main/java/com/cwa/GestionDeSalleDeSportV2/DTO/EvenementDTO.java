package com.cwa.GestionDeSalleDeSportV2.DTO;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvenementDTO {
    @NotNull(message = "L'ID de la salle de sport est requis")
    private Long gymId;

    @NotBlank(message = "Le titre est requis")
    private String title;

    private String description;

    @NotNull(message = "Le type d'événement est requis")
    private EventType eventType;

    @NotNull(message = "La date de début est requise")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull(message = "L'ID du créateur est requis")
    private Long createdById;


    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}