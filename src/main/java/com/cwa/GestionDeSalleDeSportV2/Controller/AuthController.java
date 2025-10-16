package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.ConnexionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.VerificationDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtUtils;
import com.cwa.GestionDeSalleDeSportV2.Service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public AuthController(AuthService authService, JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/inscription", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> inscriptionAdmin(
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        logger.debug("Requête d'inscription reçue avec dto: {}", dtoString);
        InscriptionDTO dto = objectMapper.readValue(dtoString, InscriptionDTO.class);
        return authService.inscriptionAdmin(dto, image);
    }

    @PostMapping("/connexion")
    public ResponseEntity<Map<String, Object>> connexion(@Valid @RequestBody ConnexionDTO dto){
        logger.debug("Requête de connexion pour téléphone: {}", dto.getTelephone());
        return authService.connexion(dto);
    }

    @PostMapping("/verifier-compte")
    public ResponseEntity<Map<String, Object>> verifierCompte(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerificationDTO dto) {
        logger.debug("Requête de vérification reçue. Authorization: {}, VerificationDTO: {}",
                authHeader, dto.getVerificationCode());
        String token = authHeader.replace("Bearer ", "");
        String telephone = jwtUtils.extractUsername(token);
        logger.debug("Téléphone extrait du JWT: {}", telephone);
        return authService.verifierCompte(telephone, dto.getVerificationCode());
    }

    @PostMapping("/renvoyer-code")
    public ResponseEntity<Map<String, Object>> renvoyerCodeVerification(
            @RequestHeader("Authorization") String authHeader) {
        logger.debug("Requête de renvoi de code reçue. Authorization: {}", authHeader);
        String token = authHeader.replace("Bearer ", "");
        String telephone = jwtUtils.extractUsername(token);
        logger.debug("Téléphone extrait pour renvoi de code: {}", telephone);
        return authService.renvoyerCodeVerification(telephone);
    }

    @PostMapping("/mot-de-passe-oublier")
    public ResponseEntity<Map<String, Object>> motDePasseOublier(
            @RequestParam("email") @Email String email) throws MessagingException {
        logger.debug("Requête de réinitialisation de mot de passe pour email: {}", email);
        return authService.motDePasseOublier(email);
    }

    @PostMapping("/verifier/reinitialiser")
    public ResponseEntity<Map<String, Object>> verifierReinitialiser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerificationDTO dto){
        logger.debug("Requête de réinitialisation vérifiée. Authorization: {}, Code: {}",
                authHeader, dto.getVerificationCode());
        String token = authHeader.replace("Bearer ", "");
        String telephone = jwtUtils.extractUsername(token);
        return authService.verifyierReinitialiser(telephone, dto.getVerificationCode());
    }

    @PostMapping("/modifier")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerificationDTO dto){
        logger.debug("Requête de mise à jour de mot de passe. Authorization: {}, Nouveau mot de passe: [masqué]",
                authHeader);
        String token = authHeader.replace("Bearer ", "");
        String telephone = jwtUtils.extractUsername(token);
        return authService.updatePassword(telephone, dto.getPassword());
    }

    @PostMapping("/test")
    public User test (@RequestBody String email){
        logger.debug("Requête de test pour email: {}", email);
        return authService.test(email);
    }
}