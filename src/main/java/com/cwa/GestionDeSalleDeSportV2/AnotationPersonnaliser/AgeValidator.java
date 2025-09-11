package com.cwa.GestionDeSalleDeSportV2.AnotationPersonnaliser;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class AgeValidator implements ConstraintValidator<AgeConstraint, String> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(AgeConstraint constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String dateNaissance, ConstraintValidatorContext context) {
        if (dateNaissance == null || dateNaissance.trim().isEmpty()){
            return false;
        }

        try{
            //  Parse la date ou definition de la date en format "yyyy-mm-dd"
            DateTimeFormatter formatDeLaDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dateAnniversaire = LocalDate.parse(dateNaissance, formatDeLaDate);
            LocalDate currentDate = LocalDate.now();

            int age = Period.between(dateAnniversaire, currentDate).getYears();

            return age >= minAge && age <= maxAge;
        } catch (Exception e) {
            return false;
        }
    }
}
