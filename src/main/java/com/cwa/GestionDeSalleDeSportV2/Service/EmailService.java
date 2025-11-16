package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final GymRepository gymRepository;

    public EmailService(JavaMailSender javaMailSender, GymRepository gymRepository) {
        this.javaMailSender = javaMailSender;
        this.gymRepository = gymRepository;
    }
   @Value("${spring.mail.username}")
    private String fromEmail; //   Expediteur

    public void envoyerEmail(String destinateur, String suject, String contenu) throws MessagingException {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinateur);
            helper.setSubject(suject);
            helper.setText(contenu, false);

            javaMailSender.send(message);
            System.out.println(" Email envoyé à " +destinateur);

        }catch (MessagingException e){
            System.out.println("Échec de l'envoi d'email : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email");
        }
    }

    public void envoyerEmailGymAMembre(String expediteur ,String destinateur, String suject, String contenu) throws MessagingException {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur);
            helper.setTo(destinateur);
            helper.setSubject(suject);
            helper.setText(contenu, false);

            javaMailSender.send(message);
            System.out.println(" Email envoyé à " +destinateur);

        }catch (MessagingException e){
            System.out.println("Échec de l'envoi d'email : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email");
        }
    }

    public void envoyerEmailAvecPieceJointe(String destinateur, String sujet, String contenu, byte[] fichier, String nomFichier) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinateur);
            helper.setSubject(sujet);
            helper.setText(contenu, false);

            ByteArrayResource attachment = new ByteArrayResource(fichier);
            helper.addAttachment(nomFichier, attachment);

            javaMailSender.send(message);
            System.out.println("📎 Email envoyé avec fichier : " + nomFichier + " à " + destinateur);
        } catch (MessagingException e) {
            System.out.println(" Échec envoi pièce jointe : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec pièce jointe.");
        }
    }

    public void envoyerEmailAvecPieceJointeGymAMembre(String expediteur ,String destinateur, String sujet, String contenu, byte[] fichier, String nomFichier) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setCc(expediteur);
            helper.setReplyTo(expediteur);
            helper.setTo(destinateur);
            helper.setSubject(sujet);
            helper.setText(contenu, false);

            ByteArrayResource attachment = new ByteArrayResource(fichier);
            helper.addAttachment(nomFichier, attachment);

            javaMailSender.send(message);
            System.out.println("📎 Email envoyé avec fichier : " + nomFichier + " à " + destinateur);
        } catch (MessagingException e) {
            System.out.println(" Échec envoi pièce jointe : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec pièce jointe.");
        }
    }

    public void envoyerEmailBienvenu(User user, String motDePasse) throws MessagingException {

        String message = String.format(
                " Bonjour %s %s,\n\n"+
                        "Votre compte a été créé sur notre plateforme de gestion de salle.\n\n"+
                        "Identifiant : %s\n"+
                        "Mot de passe : %s\n\n"+
                        "vous pouvez modifier ce mot de passe à tout momement.\n\n"+
                        "Bienvenu dans l'équipe !"
                , user.getPrenom(), user.getNom(), user.getTelephone(), motDePasse);

        envoyerEmail(user.getEmail(), "Bienvenu dans l'équipe", message);
    }

    public void envoyerEmailBienvenuGymAMembre(Gym gym, User user, String motDePasse) throws MessagingException {

        String message = String.format(
                " Bonjour %s %s,\n\n"+
                        "Votre compte a été créé sur notre plateforme de gestion de salle.\n\n"+
                        "Identifiant : %s\n"+
                        "Mot de passe : %s\n\n"+
                        "vous pouvez modifier ce mot de passe à tout momement.\n\n"+
                        "Bienvenu dans l'équipe !"
                , user.getPrenom(), user.getNom(), user.getTelephone(), motDePasse);

        envoyerEmailGymAMembre( gym.getEmail(),user.getEmail(), "Bienvenu dans l'équipe", message);
    }

    // Dans votre EmailService.java, ajoutez cette méthode :
    public void envoyerNouveauMotDePasse(User user, String nouveauMotDePasse) throws MessagingException {
        // Implémentez l'envoi d'email avec le nouveau mot de passe
        String sujet = "Réinitialisation de votre mot de passe";
        String contenu = "Bonjour " + user.getPrenom() + " " + user.getNom() + ",\n\n"
                + "Votre mot de passe a été réinitialisé. Voici votre nouveau mot de passe : " + nouveauMotDePasse + "\n\n"
                + "Nous vous recommandons de changer ce mot de passe après votre première connexion.\n\n"
                + "Cordialement,\nL'équipe de votre salle de sport";

        envoyerEmail(user.getEmail(), sujet, contenu);
    }

}

