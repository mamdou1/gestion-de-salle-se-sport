package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.ValidationInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Service.ValidationInscriptionService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation")
public class ValiderInscriptionController {

    private final ValidationInscriptionService validationInscriptionService;

    public ValiderInscriptionController(ValidationInscriptionService validationInscriptionService) {
        this.validationInscriptionService = validationInscriptionService;
    }

    @PostMapping("/confirmer/{demandeId}")
    public ResponseEntity<String> validerDemande(@Valid @RequestBody ValidationInscriptionDTO dto, @PathVariable Long demandeId) throws MessagingException {
        try {
            validationInscriptionService.validerInscription(demandeId,dto);
            return new ResponseEntity<>("Utilisateur et abonnement créés avec succès !", HttpStatus.CREATED);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Erreur lors de l'envoi de l'email : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la validation : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/rejeter/{demandeId}")
    public ResponseEntity<String> rejterDemande(@PathVariable Long demandeId, String raison) throws MessagingException {
        validationInscriptionService.rejeterInscription(demandeId, raison);
        return new ResponseEntity<>("Demande d'abonnement rejeté avec succès !", HttpStatus.CREATED);
    }
}
