package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final GymRepository gymRepository;

    public EmailService(JavaMailSender javaMailSender, GymRepository gymRepository) {
        this.javaMailSender = javaMailSender;
        this.gymRepository = gymRepository;
    }

    @Value("${spring.mail.username}")
    private String fromEmail; // Expéditeur par défaut

    public void envoyerEmail(String destinataire, String sujet, String contenu) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(destinataire);
        helper.setSubject(sujet);
        helper.setText(contenu, false);
        javaMailSender.send(message);
        logger.info("Email envoyé à {}", destinataire);
    }


    public void envoyerEmailGymAMembre(String expediteur, String destinataire, String sujet, String contenu) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(expediteur); // Attention : certains SMTP exigent que le from soit l'adresse authentifiée
        helper.setTo(destinataire);
        helper.setSubject(sujet);
        helper.setText(contenu, false);

        javaMailSender.send(message);
        logger.info("Email envoyé à {} (expéditeur: {})", destinataire, expediteur);
    }

    public void envoyerEmailAvecPieceJointe(String destinataire, String sujet, String contenu, byte[] fichier, String nomFichier) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(destinataire);
        helper.setSubject(sujet);
        helper.setText(contenu, false);

        ByteArrayResource attachment = new ByteArrayResource(fichier);
        helper.addAttachment(nomFichier, attachment);

        javaMailSender.send(message);
        logger.info("Email avec pièce jointe envoyé à {}", destinataire);
    }

    public void envoyerEmailAvecPieceJointeGymAMembre(String expediteur, String destinataire, String sujet, String contenu, byte[] fichier, String nomFichier) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setCc(expediteur);
        helper.setReplyTo(expediteur);
        helper.setTo(destinataire);
        helper.setSubject(sujet);
        helper.setText(contenu, false);

        ByteArrayResource attachment = new ByteArrayResource(fichier);
        helper.addAttachment(nomFichier, attachment);

        javaMailSender.send(message);
        logger.info("Email avec pièce jointe envoyé à {} (reply-to: {})", destinataire, expediteur);
    }

    public void envoyerEmailBienvenu(User user, String motDePasse) throws MessagingException {
        String message = String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre compte a été créé sur notre plateforme de gestion de salle.\n\n" +
                        "Identifiant : %s\n" +
                        "Mot de passe : %s\n\n" +
                        "Vous pouvez modifier ce mot de passe à tout moment.\n\n" +
                        "Bienvenue dans l'équipe !",
                user.getPrenom(), user.getNom(), user.getTelephone(), motDePasse);

        envoyerEmail(user.getEmail(), "Bienvenue dans l'équipe", message);
    }

    public void envoyerEmailBienvenuGymAMembre(Gym gym, User user, String motDePasse) throws MessagingException {
        String message = String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre compte a été créé sur notre plateforme de gestion de salle.\n\n" +
                        "Identifiant : %s\n" +
                        "Mot de passe : %s\n\n" +
                        "Vous pouvez modifier ce mot de passe à tout moment.\n\n" +
                        "Bienvenue dans l'équipe !",
                user.getPrenom(), user.getNom(), user.getTelephone(), motDePasse);

        envoyerEmailGymAMembre(gym.getEmail(), user.getEmail(), "Bienvenue dans l'équipe", message);
    }

    public void envoyerNouveauMotDePasse(User user, String nouveauMotDePasse) throws MessagingException {
        String sujet = "Réinitialisation de votre mot de passe";
        String contenu = String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre mot de passe a été réinitialisé. Voici votre nouveau mot de passe : %s\n\n" +
                        "Nous vous recommandons de changer ce mot de passe après votre première connexion.\n\n" +
                        "Cordialement,\nL'équipe de votre salle de sport",
                user.getPrenom(), user.getNom(), nouveauMotDePasse);

        envoyerEmail(user.getEmail(), sujet, contenu);
    }
}