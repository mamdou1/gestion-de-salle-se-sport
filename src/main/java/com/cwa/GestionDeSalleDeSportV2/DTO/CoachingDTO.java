package com.cwa.GestionDeSalleDeSportV2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoachingDTO {
    @NotNull(message = "L'ID de la salle de sport est requis")
    private Long gymId;

    @NotNull(message = "L'ID du client est requis")
    private Long clientId;

    @NotNull(message = "L'ID du coach est requis")
    private Long coachId;

    @NotNull(message = "La date de début est requise")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est requise")
    private LocalDateTime endDate;

    private BigDecimal cost;

    @NotBlank(message = "Le nom du cours est requis")
    private String courseName;


    public Long getGymId() {
        return gymId;
    }

    public void setGymId(Long gymId) {
        this.gymId = gymId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}