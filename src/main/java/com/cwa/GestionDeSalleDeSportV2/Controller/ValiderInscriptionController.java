package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.ValidationInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.ValidationInscriptionService;
import jakarta.mail.MessagingException;
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
    public ResponseEntity<String> validerDemande(@RequestBody ValidationInscriptionDTO dto) throws MessagingException {
        validationInscriptionService.validerInscription(dto);
        return new ResponseEntity<>("Utilisateur et abonnement crée avec succès !", HttpStatus.CREATED);
    }
}
