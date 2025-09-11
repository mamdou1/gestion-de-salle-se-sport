package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ChangerMotDePassDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.StaffDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @PostMapping("/ajouter/staff")
    public ResponseEntity<String> ajouterStaff(@Valid @RequestBody StaffDTO staffDTO) throws AccessDeniedException, MessagingException {
        User admin = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.ajouterStaff(staffDTO, admin);

        return new  ResponseEntity<>(message, HttpStatus.CREATED);
    }

    //  2.  Ajouter un nouveau membre par le staff autoriser
    @PostMapping("/ajouter/membre")
    public ResponseEntity<String> ajouterMembre(@Valid @RequestBody MembreDTO membreDTO) throws MessagingException, AccessDeniedException {
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

    @PutMapping("/modifier-staff/{id}")
    public ResponseEntity<String> modifierStaff(@PathVariable Long id, @RequestBody StaffDTO dto) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.modifierStaff(id, dto, currentUser);

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
    @GetMapping("/membre")
    public ResponseEntity<List<User>> getAllMembre() throws AccessDeniedException {
        return new ResponseEntity<>(userService.getAllMembre(), HttpStatus.OK);
    }

    //  5.1 Afficher tout les membres paginer
    @GetMapping("/membre/paginer")
    public Page<User> getAllMembreAvecPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws AccessDeniedException {
        Pageable pageable = PageRequest.of(page, size); // PageRequest est une implémentation de Pageable
        return userService.getAllMembreAvecPagination(pageable);
    }

    //  6.  Afficher tout les utilisater
    @GetMapping("/staff")
    public ResponseEntity<List<User>> getAllStaff() throws AccessDeniedException {
        return new ResponseEntity<>(userService.getAllStaff(), HttpStatus.OK);
    }

    //  6.  Afficher tout les utilisater
    @GetMapping("/liste")
    public ResponseEntity<List<User>> getAllUser() throws AccessDeniedException {
        return new ResponseEntity<>(userService.getAllMembre(), HttpStatus.OK);
    }

    //  7.  Changer le mot de passe
    /*

    @PostMapping("/changer")
    public ResponseEntity<Map<String,Object>> changerMotDePasse(@RequestBody ChangerMotDePassDTO dto){
        userService.changerMotDePasse(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

     */

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> verifierMotDePasse(@RequestBody Map<String, String> body) {
        String motDePasseSaisi = body.get("motDePasse");
        String motDePasseEncode = body.get("motDePasseEncode");

        boolean estValide = userService.verifierMotDePasse(motDePasseSaisi, motDePasseEncode);

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("motDePasseValide", estValide);

        return ResponseEntity.ok(reponse);
    }
}

