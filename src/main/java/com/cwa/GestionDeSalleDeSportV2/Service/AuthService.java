package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.DTO.ConnexionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
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
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, GymRepository gymRepository, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, NotificationRepository notificationRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    private static class PendingRegistration {
        private final Gym gym;
        private final User user;
        private final MultipartFile file;
        private final Notification notification;

        public PendingRegistration(Gym gym, User user, MultipartFile file, Notification notification) {
            this.gym = gym;
            this.user = user;
            this.file = file;
            this.notification = notification;
        }
    }

    public ResponseEntity<Map<String, Object>> inscriptionAdmin(InscriptionDTO dto, MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();

        try {
            Gym gym = new Gym();
            gym.setNom(dto.getNomGym());
            gym.setAdresse(dto.getAdresseGym());
            gym.setEmail(dto.getEmailGym());
            gym.setTelephone(dto.getTelephoneGym());
            gym.setDescription(dto.getDescription());
            if (file != null && !file.isEmpty()) {
                gym.setPhoto(file.getBytes());
            }

            User admin = new User();
            admin.setNom(dto.getNomAdmin());
            admin.setPrenom(dto.getPrenomAdmin());
            admin.setAdresse(dto.getAdresseAdmin());
            admin.setEmail(dto.getEmailAdmin());
            admin.setRole(Role.ADMIN);
            admin.setGenre(dto.getGenre());
            admin.setDate_de_naissance(dto.getDate_de_naissance());
            admin.setDate_creation(LocalDateTime.now());
            admin.setGym(gym);
            admin.getGyms().add(gym);

            admin.setTelephone(dto.getTelephoneAdmin());
            admin.setPassword(passwordEncoder.encode(dto.getPasswordAdmin()));

            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            admin.setVerificationCode(verificationCode);
            admin.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
            admin.setVerified(false);

            String titre = "Vérification de votre compte";
            String message = "Bonjour " + admin.getPrenom() + ",\n\n" +
                    "Merci pour votre inscription. Votre code de vérification est : " + verificationCode + "\n\n" +
                    "Ce code expirera dans 24 heures. Vérifiez vos spams si vous ne le trouvez pas.\n\n" +
                    "L'équipe GestionDeSalleDeSport";

            Notification notif = new Notification();
            notif.setTitre(titre);
            notif.setContenu(message);
            notif.setDateEnvoi(LocalDateTime.now());
            notif.setDestinataire(admin);
            notif.setContexte("Vérification de compte");
            notif.setTypeNotification(TypeNotification.VERIFICATION);

            logger.debug("Enregistrement en attente pour téléphone: {}, email: {}, code: {}",
                    dto.getTelephoneAdmin(), dto.getEmailAdmin(), verificationCode);
            pendingRegistrations.put(dto.getTelephoneAdmin(), new PendingRegistration(gym, admin, file, notif));

            emailService.envoyerEmail(admin.getEmail(), titre, message);
            logger.info("Email envoyé à {} avec code: {}", admin.getEmail(), verificationCode);

            String token = jwtUtils.generateToken(new UserDetails() {
                @Override
                public String getUsername() {
                    return admin.getTelephone();
                }
                @Override
                public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                    return java.util.Collections.emptyList();
                }
                @Override
                public String getPassword() {
                    return admin.getPassword();
                }
                @Override
                public boolean isAccountNonExpired() {
                    return true;
                }
                @Override
                public boolean isAccountNonLocked() {
                    return true;
                }
                @Override
                public boolean isCredentialsNonExpired() {
                    return true;
                }
                @Override
                public boolean isEnabled() {
                    return true;
                }
            });
            response.put("token", token);
            response.put("message", "Inscription réussie. Vérifiez votre email pour activer votre compte.");
            return ResponseEntity.ok(response);

        } catch (MessagingException e) {
            logger.error("Échec de l'envoi de l'email pour téléphone: {}: {}", dto.getTelephoneAdmin(), e.getMessage());
            response.put("error", "Échec de l'envoi de l'email de vérification : " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            logger.error("Erreur inscription pour téléphone: {}: {}", dto.getTelephoneAdmin(), e.getMessage());
            response.put("error", "Erreur lors de l'inscription : " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> verifierCompte(String telephone, String codeVerification) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.debug("Tentative de vérification pour téléphone: {}, code brut: {}", telephone, codeVerification);

            PendingRegistration pending = pendingRegistrations.get(telephone);
            if (pending == null) {
                logger.error("Aucune inscription en attente pour téléphone: {}", telephone);
                response.put("error", "Aucune inscription en attente pour ce numéro de téléphone. Veuillez recommencer l'inscription.");
                return ResponseEntity.status(400).body(response);
            }

            User user = pending.user;
            String cleanedCode = codeVerification != null ? codeVerification.trim().toUpperCase() : null;

            logger.debug("Code traité: {}, Code stocké: {}, Email associé: {}, Téléphone stocké: {}",
                    cleanedCode, user.getVerificationCode(), user.getEmail(), user.getTelephone());

            if (user.getVerificationCode() == null || !user.getVerificationCode().equals(cleanedCode)) {
                logger.error("Code de vérification invalide pour téléphone: {}. Reçu brut: {}, Traité: {}, Attendu: {}",
                        telephone, codeVerification, cleanedCode, user.getVerificationCode());
                response.put("error", "Code de vérification invalide. Vérifiez le code ou renvoyez un nouveau code.");
                return ResponseEntity.status(400).body(response);
            }

            if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
                logger.error("Code de vérification expiré pour téléphone: {}", telephone);
                response.put("error", "Code de vérification expiré. Veuillez demander un nouveau code.");
                pendingRegistrations.remove(telephone);
                return ResponseEntity.status(400).body(response);
            }

            Gym gym = pending.gym;
            gymRepository.save(gym);
            user.setGym(gym);
            user.getGyms().add(gym);
            user.setVerified(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);

            Notification notif = pending.notification;
            notif.setDestinataire(user);
            notificationRepository.save(notif);

            pendingRegistrations.remove(telephone);

            logger.info("Compte vérifié avec succès pour téléphone: {}", telephone);
            response.put("message", "Compte vérifié avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erreur de vérification pour téléphone {}: {}", telephone, e.getMessage());
            response.put("error", "Erreur lors de la vérification : " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> connexion(ConnexionDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
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

    public ResponseEntity<Map<String, Object>> renvoyerCodeVerification(String telephone) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.debug("Requête de renvoi de code pour téléphone: {}", telephone);
            PendingRegistration pending = pendingRegistrations.get(telephone);
            if (pending == null) {
                logger.error("Aucune inscription en attente pour téléphone: {}", telephone);
                response.put("error", "Aucune inscription en attente pour ce numéro de téléphone");
                return ResponseEntity.status(400).body(response);
            }

            User user = pending.user;

            if (user.isVerified()) {
                logger.info("Compte déjà vérifié pour téléphone: {}", telephone);
                response.put("message", "Compte déjà vérifié");
                return ResponseEntity.ok(response);
            }

            String newVerificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            user.setVerificationCode(newVerificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));

            String titre = "Nouveau code de vérification";
            String message = "Bonjour " + user.getPrenom() + ",\n\n" +
                    "Votre nouveau code de vérification est : " + newVerificationCode + "\n\n" +
                    "Ce code expirera dans 24 heures. Vérifiez vos spams si vous ne le trouvez pas.\n\n" +
                    "L'équipe GestionDeSalleDeSport";

            Notification notif = new Notification();
            notif.setTitre(titre);
            notif.setContenu(message);
            notif.setDateEnvoi(LocalDateTime.now());
            notif.setDestinataire(user);
            notif.setContexte("Vérification de compte");
            notif.setTypeNotification(TypeNotification.VERIFICATION);

            pendingRegistrations.put(telephone, new PendingRegistration(pending.gym, user, pending.file, notif));

            emailService.envoyerEmail(user.getEmail(), titre, message);
            logger.info("Nouveau code envoyé à {} avec code: {}", user.getEmail(), newVerificationCode);

            response.put("message", "Nouveau code envoyé par email");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du code pour téléphone {}: {}", telephone, e.getMessage());
            response.put("error", "Erreur lors de l'envoi du code : " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    public User test(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim());

        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        return user;
    }

    public ResponseEntity<Map<String, Object>> motDePasseOublier(String email) throws MessagingException {
        logger.debug("Requête de réinitialisation de mot de passe pour email: {}", email);
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null) {
            logger.error("Aucun utilisateur trouvé pour email: {}", email);
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
        logger.info("Email de réinitialisation envoyé à {} avec code: {}", user.getEmail(), verificationCode);

        String token = jwtUtils.generateToken(new UserDetails() {
            @Override
            public String getUsername() {
                return user.getTelephone();
            }
            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return java.util.Collections.emptyList();
            }
            @Override
            public String getPassword() {
                return user.getPassword();
            }
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            @Override
            public boolean isEnabled() {
                return true;
            }
        });

        response.put("token", token);
        response.put("message", "Un code de réinitialisation a été envoyé à votre email. Il expirera dans 24 heures.");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> verifyierReinitialiser(String telephone, String codeVerification) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(codeVerification)) {
            throw new IllegalArgumentException("Code de vérification invalide");
        }

        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Code de vérification expiré");
        }

        String defaultNewPassword = "Tempass123";
        user.setVerified(true);
        user.setPassword(passwordEncoder.encode(defaultNewPassword));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        response.put("message", "Compte vérifié et mot de passe réinitialisé avec succès");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> updatePassword(String telephone, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.isVerified()) {
            throw new IllegalStateException("Le compte n'est pas vérifié, impossible de mettre à jour le mot de passe");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        response.put("message", "Mot de passe mis à jour avec succès");
        return ResponseEntity.ok(response);
    }
}