package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.DTO.ConnexionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.VerificationDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtUtils;
import com.cwa.GestionDeSalleDeSportV2.Service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/inscription")
    public ResponseEntity<Map<String, Object>> inscriptionAdmin(@RequestBody InscriptionDTO dto){
        return authService.inscriptionAdmin(dto);
    }

    @PostMapping("/connexion")
    public ResponseEntity<Map<String, Object>> connexion(@Valid @RequestBody ConnexionDTO dto){
        return authService.connexion(dto);
    }

    @PostMapping("/verifier-compte")
    public ResponseEntity<Map<String, Object>> verifierCompte(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerificationDTO dto) {

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String telephone = jwtUtils.extractUsername(token);

        return authService.verifierCompte(telephone, dto.getVerificationCode());
    }

    @PostMapping("/renvoyer-code")
    public ResponseEntity<Map<String, Object>> renvoyerCodeVerification(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String telephone = jwtUtils.extractUsername(token);

        return authService.renvoyerCodeVerification(telephone);
    }

    //  Mot de passe oublier
    @PostMapping("/mot-de-passe-oublier")
    public ResponseEntity<Map<String, Object>> motDePasseOublier(
            @RequestParam("email") @Email String email) throws MessagingException {
        return authService.motDePasseOublier(email);
    }

    //  Verifier et réinitialiser le mot de passe
    @PostMapping("/verifier/reitialiser")
    public ResponseEntity<Map<String, Object>> verifyierReinitialiser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerificationDTO dto){
        String token = authHeader.substring(7);
        String telephone = jwtUtils.extractUsername(token);

        return authService.verifyierReinitialiser(telephone, dto.getVerificationCode());
    }

    //  Mettre à jour le mot de passe
    @PostMapping("/modifier")
    public  ResponseEntity<Map<String, Object>> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerificationDTO dto){
        String token = authHeader.substring(7);
        String telephone = jwtUtils.extractUsername(token);

        return authService.updatePassword(telephone, dto.getPassword());
    }

    @PostMapping("/test")
    public User test (@RequestBody String email){
        return authService.test(email);
    }
}

