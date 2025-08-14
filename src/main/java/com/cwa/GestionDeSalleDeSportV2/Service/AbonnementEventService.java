package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AbonnementEventService {

//    private final JavaMailSender javaMailSender;
    private final DocumentService documentService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String fromEmail; //   Expediteur

    public AbonnementEventService(DocumentService documentService, NotificationService notificationService, EmailService emailService) {
        this.notificationService = notificationService;
        this.documentService = documentService;
        this.emailService = emailService;
    }

    //Envoie par email la facture PDF au membre
    public void envoyerFactureParEmail(User utilisateur ,Abonnement abonnement , String contexte) throws MessagingException {

        byte [] pdf = documentService.genererFactureAbonnement(abonnement);

        // Email email avec pièce jointe
        emailService.envoyerEmailAvecPieceJointe(utilisateur.getEmail(),
                "Facture - " + contexte + "abonnement",
                String.format("""
            Bonjour %s %s,

            Votre facture est générée suite à la %s de votre abonnement à la salle %s.
            Veuillez trouver la pièce jointe ci-dessous.

            Sportivement 🏋️,
            L’équipe de gestion
            """,
                        utilisateur.getPrenom(),
                        utilisateur.getNom(),
                        contexte.toLowerCase(),
                        utilisateur.getGym().getNom()),
                pdf,
                "facture_abonnement.pdf"
                );
        notificationService.notification(utilisateur,
                "Facture générée",
                "Votre facture est disponible pour l'abonnement de " + abonnement.getNombreDeMois() +"mois.",
                contexte,
                TypeNotification.ABONNEMENT,
                false);
    }

    public void notifierRappelFin(Abonnement abonnement) throws MessagingException {

        User user = abonnement.getMembre();
        String titre = "Votre abonnement expiret bientôt";
        String message = "Bonjour" + user.getPrenom() + ", votre abonnement expire bientôt. Prensez à la renouveler.";

        notificationService.notification(user, titre, message, "Rappel de fin", TypeNotification.ABONNEMENT, true);

        System.out.println("⏰ Tâche de rappel exécutée à : " + LocalDateTime.now());
    }

    public void notifierExpiration(Abonnement abonnement) throws MessagingException {
        User user = abonnement.getMembre();
        if (!user.getGyms().contains(abonnement.getGym())) {
            throw new SecurityException("Accès refusé : l'utilisateur n'est pas affilié à ce gym.");
        }
        String titre = "Votre abonnement a expiré";
        String message = "Bonjour " + user.getPrenom() + ", votre abonnement est maintenant expiré.";
        notificationService.notification(user, titre, message, "Expiration", TypeNotification.ABONNEMENT, true);
    }
}

