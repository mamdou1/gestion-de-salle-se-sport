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
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public UserController(UserService userService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.userService = userService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    // 1. Ajouter un nouveau staff par l'admin
    @PostMapping("/ajouter/staff")
    public ResponseEntity<String> ajouterStaff(@Valid @ModelAttribute StaffDTO staffDTO, @RequestParam(required = false) MultipartFile file) throws IOException, MessagingException {
        User admin = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.ajouterStaff(staffDTO, admin, file);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    // 2. Ajouter un nouveau membre par le staff autorisé
    @PostMapping("/ajouter/membre")
    public ResponseEntity<Optional<User>> ajouterMembre(@Valid @ModelAttribute MembreDTO membreDTO, @RequestParam(required = false) MultipartFile file) throws MessagingException, IOException {
        User staff = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        Optional<User> user = userService.ajouterMembre(membreDTO, staff, file);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // 3. Modifier un membre par le staff autorisé
    @PutMapping(value = "/modifier-membre/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> modifierMembre(@ModelAttribute MembreDTO membreDTO, @RequestPart(required = false) MultipartFile file, @PathVariable Long id) throws IOException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.modifierMembre(id, membreDTO, currentUser, file);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @PutMapping("/modifier-staff/{id}")
    public ResponseEntity<String> modifierStaff(@PathVariable Long id, @ModelAttribute StaffDTO dto, @RequestParam(required = false) MultipartFile file) throws IOException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        String message = userService.modifierStaff(id, dto, currentUser, file);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/photo/staff/{id}")
    public ResponseEntity<byte[]> getPhotoProduitStaff(@PathVariable Long id) {
        byte[] image = userService.getPhotoProduitStaff(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(image);
    }

    @GetMapping("/photo/membre/{id}")
    public ResponseEntity<byte[]> getPhotoProduitMembre(@PathVariable Long id) {
        byte[] image = userService.getPhotoProduitMembre(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(image);
    }

    // 4. Consultation d’un profil
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

    // 5. Afficher tous les membres
    @GetMapping("/membre")
    public ResponseEntity<List<User>> getAllMembre() throws AccessDeniedException {
        return new ResponseEntity<>(userService.getAllMembre(), HttpStatus.OK);
    }

    // 5.1 Afficher tous les membres avec pagination
    @GetMapping("/membre/paginer")
    public ResponseEntity<Page<User>> getAllMembreAvecPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws AccessDeniedException {
        Pageable pageable = PageRequest.of(page, size);
        return new ResponseEntity<>(userService.getAllMembreAvecPagination(pageable), HttpStatus.OK);
    }

    // 6. Afficher tous les staff
    @GetMapping("/staff")
    public ResponseEntity<List<User>> getAllStaff() throws AccessDeniedException {
        return new ResponseEntity<>(userService.getAllStaff(), HttpStatus.OK);
    }

    // 7. Afficher tous les utilisateurs
    @GetMapping("/liste")
    public ResponseEntity<List<User>> getAllUser() throws AccessDeniedException {
        return new ResponseEntity<>(userService.getAllMembre(), HttpStatus.OK);
    }

    // 8. Changer le mot de passe
    @PostMapping("/changer")
    public ResponseEntity<String> changerMotDePasse(@RequestBody ChangerMotDePasseDTO dto) {
        userService.changerMotDePasse(dto);
        return new ResponseEntity<>("Le changement de mot de passe a été effectué avec succès", HttpStatus.CREATED);
    }

    // 9. Retirer un staff
    @PutMapping("/retirer/staff/{staffId}")
    public ResponseEntity<String> retirerStaff(@PathVariable Long staffId) {
        userService.retirerStaff(staffId);
        return new ResponseEntity<>("Staff retiré avec succès.", HttpStatus.OK);
    }

    // 10. Vérifier le mot de passe
    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> verifierMotDePasse(@RequestBody Map<String, String> body) {
        String motDePasseSaisi = body.get("motDePasse");
        String motDePasseEncode = body.get("motDePasseEncode");
        boolean estValide = userService.verifierMotDePasse(motDePasseSaisi, motDePasseEncode);
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("motDePasseValide", estValide);
        return ResponseEntity.ok(reponse);
    }

    // 11. Récupérer les gyms d'un membre
    @GetMapping("/gyms")
    public ResponseEntity<List<Gym>> getGymsOfMember() throws AccessDeniedException {
        List<Gym> gyms = userService.getGymsOfMembre();
        return new ResponseEntity<>(gyms, HttpStatus.OK);
    }

    // 12. Supprimer un membre
    @DeleteMapping("/supprimer-membre/{id}")
    public ResponseEntity<String> supprimerMembre(@PathVariable @Positive Long id) {
        logger.info("Requête DELETE pour supprimer le membre ID: {}", id);
        try {
            User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
            userService.supprimerMembre(id, currentUser);
            logger.info("Membre ID {} supprimé avec succès", id);
            return new ResponseEntity<>("Membre supprimé avec succès", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Membre non trouvé pour l'ID: {}", id, e);
            return new ResponseEntity<>("Membre non trouvé avec l'ID: " + id, HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Accès refusé pour la suppression du membre ID: {}", id, e);
            return new ResponseEntity<>("Accès non autorisé: " + e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la suppression du membre ID: {}", id, e);
            return new ResponseEntity<>("Erreur lors de la suppression: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}