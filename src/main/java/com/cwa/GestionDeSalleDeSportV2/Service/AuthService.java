package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.DTO.ConnexionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.VerificationDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtUtils;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.NotificationRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, GymRepository gymRepository, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, NotificationRepository notificationRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public ResponseEntity<Map<String, Object>> inscriptionAdmin(InscriptionDTO dto, MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();

        try {


        Gym gym = new Gym();
        gym.setNom(dto.getNomGym());
        gym.setAdresse(dto.getAdresseGym());
        gym.setEmail(dto.getEmailGym());
        gym.setTelephone(dto.getTelephoneGym());
//        gym.setDescription(dto.getDescription());
//
//        if (file != null && file.isEmpty()){
//            gym.setPhoto(file.getBytes()); //  Conversion du MultipartFile en byte[]
//        }

            gymRepository.save(gym);

        // 2. Créer l’utilisateur admin lié à ce gym
        User admin = new User();
        admin.setNom(dto.getNomAdmin());
        admin.setPrenom(dto.getPrenomAdmin());
        admin.setAdresse(dto.getAdresseAdmin());
        admin.setEmail(dto.getEmailAdmin());
        admin.setRole(Role.ADMIN);
        admin.setGenre(dto.getGenre());
        admin.setDate_de_naissance(dto.getDate_de_naissance());
        admin.setDate_creation(LocalDateTime.now());
      //  admin.setOnline(false);
        admin.setGym(gym);
        admin.getGyms().add(gym);

//            if (file != null && file.isEmpty()){
//                admin.setPhoto(file.getBytes()); //  Conversion du MultipartFile en byte[]
//            }

        // . Telephone et mot de passe
        admin.setTelephone(dto.getTelephoneAdmin());
        admin.setPassword(passwordEncoder.encode(dto.getPasswordAdmin()));


            // Générer le code de vérification
            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            admin.setVerificationCode(verificationCode);
            admin.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
            admin.setVerified(false); // Compte non vérifié par défaut

            userRepository.save(admin);

        // 4. Création du message
        String titre = "Vérification de votre compte";
        String message = "Bonjour " + admin.getPrenom() + ",\n\n" +
                "Merci pour votre inscription. Votre code de vérification est : " + verificationCode + "\n\n" +
                "Ce code expirera dans 24 heures.\n\n" +
                "L'équipe GestionDeSalleDeSport";

        // 5. Enregistrement de la notification et envoi de l’email
        Notification notif = new Notification();
        notif.setTitre(titre);
        notif.setContenu(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setDestinataire(admin);
        notif.setContexte("Vérification de compte");
        notif.setTypeNotification(TypeNotification.VERIFICATION);
        notificationRepository.save(notif);

        emailService.envoyerEmail(admin.getEmail(), titre, message);

            // 6. Génération du token
            String token = jwtUtils.generateToken(admin);
            response.put("token", token);
            response.put("message", "Inscription réussie. Vérifiez votre email pour activer votre compte.");
            return ResponseEntity.ok(response);

        } catch (MessagingException e) {
            response.put("error", "Échec de l'envoi de l'email de vérification : " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("error", "Erreur lors de l'inscription : " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

//    public byte[] getPhotoProduitGym (Long id){
//        Gym gym = gymRepository.findById(id)
//                .orElseThrow(()->new RuntimeException("Produit non trouvé."));
//        return gym.getPhoto();
//    }
//
//    public byte[] getPhotoProduitAdmin (Long id){
//        User user = userRepository.findById(id)
//                .orElseThrow(()->new RuntimeException("Produit non trouvé."));
//        return user.getPhoto();
//    }

    public ResponseEntity<Map<String, Object>> connexion(ConnexionDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Vérifier d'abord si le compte est vérifié
//            User user = userRepository.findByTelephone(dto.getTelephone())
//                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//
//            if (!user.isVerified()) {
//                response.put("error", "Votre compte n'a pas encore été vérifié. Veuillez vérifier votre email.");
//                return ResponseEntity.status(401).body(response);
//            }

            // Si le compte est vérifié, procéder à l'authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getTelephone(), dto.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);
            response.put("token", token);
            response.put("message", "Connexion réussie");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Identifiants invalides ou compte non vérifié : " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    // Nouvelle méthode pour vérifier le compte
    public ResponseEntity<Map<String, Object>> verifierCompte(String telephone, String codeVerification) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findByTelephone(telephone)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.isVerified()) {
                response.put("message", "Compte déjà vérifié");
                return ResponseEntity.ok(response);
            }

            if (user.getVerificationCode() == null || !user.getVerificationCode().equals(codeVerification)) {
                response.put("error", "Code de vérification invalide");
                return ResponseEntity.status(400).body(response);
            }

            if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
                response.put("error", "Code de vérification expiré");
                return ResponseEntity.status(400).body(response);
            }

            // Vérification réussie
            user.setVerified(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);

            response.put("message", "Compte vérifié avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Erreur lors de la vérification : " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }

    }

    // Méthode pour renvoyer le code de vérification
    public ResponseEntity<Map<String, Object>> renvoyerCodeVerification(String telephone) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findByTelephone(telephone)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.isVerified()) {
                response.put("message", "Compte déjà vérifié");
                return ResponseEntity.ok(response);
            }

            // Générer un nouveau code
            String newVerificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            user.setVerificationCode(newVerificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

            // Envoyer le nouvel email
            String titre = "Nouveau code de vérification";
            String message = "Bonjour " + user.getPrenom() + ",\n\n" +
                    "Votre nouveau code de vérification est : " + newVerificationCode + "\n\n" +
                    "Ce code expirera dans 24 heures.\n\n" +
                    "L'équipe GestionDeSalleDeSport";

            emailService.envoyerEmail(user.getEmail(), titre, message);

            response.put("message", "Nouveau code envoyé par email");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Erreur lors de l'envoi du code : " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    public User test(String email){
        User user = userRepository.findByEmailIgnoreCase(email.trim());

        if (user == null) {
            throw new RuntimeException("Utilisateur non trouver");
        }

        return user;
    }


    //  Mot de passe oublié
    public ResponseEntity<Map<String, Object>> motDePasseOublier(String email) throws MessagingException {
        System.out.println("L'email reçu par le backend : " + email);
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null) {
            response.put("error", "Aucun utilisateur trouvé avec cet email.");
            return ResponseEntity.status(404).body(response);
        }



        String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        user.setVerified(false);

        userRepository.save(user);

        String titre = "Réinitialisation de votre mot de passe";
        String message = "Bonjour " + user.getPrenom() + ",\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe. Votre code de réinitialisation est : " + verificationCode + "\n\n" +
                "Ce code expirera dans 24 heures. Veuillez utiliser ce code pour réinitialiser votre mot de passe.\n\n" +
                "L'équipe GestionDeSalleDeSport";

        Notification notif = new Notification();
        notif.setTitre(titre);
        notif.setContenu(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setDestinataire(user);
        notif.setContexte("Réinitialisation de mot de passe");
        notif.setTypeNotification(TypeNotification.VERIFICATION);
        notificationRepository.save(notif);

        emailService.envoyerEmail(user.getEmail(), titre, message);

        String token = jwtUtils.generateToken(user);

        response.put("token:",  token );
        response.put("message:", " Un code de réinitialisation a été envoyé à votre email. Il expirera dans 24 heures.");
        return ResponseEntity.ok(response);

    }

    //  Mettre a jour le mot de passe
    public ResponseEntity<Map<String, Object>> verifyierReinitialiser(String telephone, String codeVerification) {
        Map<String, Object> respone = new HashMap<>();

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(codeVerification)) {
            throw new IllegalArgumentException("Code de vérification invalide");
        }

        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Code de vérification expiré");
        }

        // Vérification et réinitialisation réussie
        String defaultNewPassword = "Tempass123";
        user.setVerified(true); // Marquer comme vérifié
        user.setPassword(passwordEncoder.encode(defaultNewPassword)); // Réinitialiser le mot de passe
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        respone.put("message: ","Compte vérifié et mot de passe réinitialisé avec succès");
        return ResponseEntity.ok(respone);
    }

    //  Modifier le mot de passe
    public ResponseEntity<Map<String, Object>> updatePassword(String telephone, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.isVerified()) {
            throw new IllegalStateException("Le compte n'est pas vérifié, impossible de mettre à jour le mot de passe");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        response.put("message: ", "Mot de passe mis à jour avec succès");
        return ResponseEntity.ok(response);
    }
}

