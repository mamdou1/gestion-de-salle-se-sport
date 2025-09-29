package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ChangerMotDePasseDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.StaffDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<String> ajouterStaff(@Valid @ModelAttribute StaffDTO staffDTO, @RequestParam(required = false)MultipartFile file) throws IOException, MessagingException { // , MultipartFile image
        User admin = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.ajouterStaff(staffDTO, admin, file); // , image

        return new  ResponseEntity<>(message, HttpStatus.CREATED);
    }

//    @GetMapping("/photo/{id}")
//    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id){
//        byte[] image = userService.getPhotoProduitMembre(id);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") //  ou "image/png"
//                .body(image);
//    }

    //  2.  Ajouter un nouveau membre par le staff autoriser
    @PostMapping("/ajouter/membre")
    public ResponseEntity<Optional<User>> ajouterMembre(@Valid @ModelAttribute MembreDTO membreDTO, @RequestParam(required = false)MultipartFile file) throws MessagingException, IOException {
        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Optional<User> user = userService.ajouterMembre(membreDTO, staff, file);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    //  3.  Modifier un membre par le staff autoriser
    @PutMapping(value = "/modifier-membre/{id}",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> modifierMembre(@ModelAttribute MembreDTO membreDTO, @RequestPart(required = false)MultipartFile file, @PathVariable Long id) throws IOException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.modifierMembre(id, membreDTO, currentUser, file);

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @PutMapping("/modifier-staff/{id}")
    public ResponseEntity<String> modifierStaff(@PathVariable Long id, @ModelAttribute StaffDTO dto, @RequestParam(required = false)MultipartFile file) throws IOException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.modifierStaff(id, dto, currentUser, file);

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/photo/staff/{id}")
    public ResponseEntity<byte[]> getPhotoProduitStaff(@PathVariable Long id){
        byte[] image = userService.getPhotoProduitStaff(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") //  ou "image/png"
                .body(image);
    }
    @GetMapping("/photo/membre/{id}")
    public ResponseEntity<byte[]> getPhotoProduitMembre(@PathVariable Long id){
        byte[] image = userService.getPhotoProduitMembre(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") //  ou "image/png"
                .body(image);
    }

    //  4.  Consultation d’un profil
    @GetMapping("/profil/{id}")
    public ResponseEntity<User> getProfil(@PathVariable Long id) throws AccessDeniedException {
        User currentUtils = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User membre = userService.consulterProfil(id, currentUtils);

        return ResponseEntity.ok(membre);
    }
    @GetMapping("/profil-app/{id}")
    public ResponseEntity<User> getProfilApp(@PathVariable Long id) throws AccessDeniedException {
        User currentUtils = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        id = currentUtils.getId();
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


    @PostMapping("/changer")
    public ResponseEntity<String> changerMotDePasse(@RequestBody ChangerMotDePasseDTO dto){
        userService.changerMotDePasse(dto);
        return new ResponseEntity<>("Le changement de mot de passe à été éffectuer avec succès", HttpStatus.CREATED);
    }

    //  retirer staff
    @PutMapping("/retirer/staff/{staffId}")
    public ResponseEntity<String> retirerStaff(@PathVariable Long staffId){
        userService.retirerStaff(staffId);
        return new ResponseEntity<>("Staff retirer avec succes.", HttpStatus.OK);
    }


    //  verifier mot de Passe
    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> verifierMotDePasse(@RequestBody Map<String, String> body) {
        String motDePasseSaisi = body.get("motDePasse");
        String motDePasseEncode = body.get("motDePasseEncode");

        boolean estValide = userService.verifierMotDePasse(motDePasseSaisi, motDePasseEncode);

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("motDePasseValide", estValide);

        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/gyms")
    public ResponseEntity<List<Gym>> getGymsOfMember() throws AccessDeniedException {
        List<Gym> gyms = userService.getGymsOfMembre();
        return new ResponseEntity<>(gyms, HttpStatus.OK);
    }
}

