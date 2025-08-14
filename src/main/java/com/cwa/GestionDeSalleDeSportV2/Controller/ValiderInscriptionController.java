package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.ValidationInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.ValidationInscriptionService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validation")
public class ValiderInscriptionController {

    private final ValidationInscriptionService validationInscriptionService;

    public ValiderInscriptionController(ValidationInscriptionService validationInscriptionService) {
        this.validationInscriptionService = validationInscriptionService;
    }

    @PostMapping
    public ResponseEntity<String> validerDemande(@Valid @RequestBody ValidationInscriptionDTO dto) throws MessagingException {
        try {
            validationInscriptionService.validerInscription(dto);
            return new ResponseEntity<>("Utilisateur et abonnement créés avec succès !", HttpStatus.CREATED);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Erreur lors de l'envoi de l'email : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la validation : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
