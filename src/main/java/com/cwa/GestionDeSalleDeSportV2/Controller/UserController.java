package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.StaffDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public UserController(UserService userService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.userService = userService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    //  1.  Ajouter un nouveau staff par l'admin
    @PostMapping("/staff")
    public ResponseEntity<String> ajouterStaff(@Valid @RequestBody StaffDTO staffDTO) throws AccessDeniedException, MessagingException {
        User admin = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.ajouterStaff(staffDTO, admin);

        return new  ResponseEntity<>(message, HttpStatus.CREATED);
    }

    //  2.  Ajouter un nouveau membre par le staff autoriser
    @PostMapping("/membre")
    public ResponseEntity<String> ajouterMembre(@Valid @RequestBody MembreDTO membreDTO) throws MessagingException {
        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.ajouterMembre(membreDTO, staff);

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    //  3.  Modifier un membre par le staff autoriser
    @PutMapping("/modifier-membre/{id}")
    public ResponseEntity<String> modifierMembre(@PathVariable Long id, @RequestBody MembreDTO membreDTO) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.modifierMembre(id, membreDTO, currentUser);

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    //  4.  Consultation d’un profil
    @GetMapping("/profil/{id}")
    public ResponseEntity<User> getProfil(@PathVariable Long id) throws AccessDeniedException {
        User currentUtils = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User membre = userService.consulterProfil(id, currentUtils);

        return ResponseEntity.ok(membre);
    }

    //  5.  Afficher tout les utilisater
    @GetMapping
    public ResponseEntity<List<User>> getAllUser(){
        return new ResponseEntity<>(userService.getAllUser(), HttpStatus.OK);
    }
}

