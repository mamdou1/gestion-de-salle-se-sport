package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.NotificationRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public void notification(User user, String titre, String message, String contexte, TypeNotification typeNotification, boolean envoyerEmail) throws MessagingException {

       // User user1 = new User();

        Notification notif = new Notification();

        notif.setTitre(titre);
        notif.setContenu(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setDestinataire(user);
        notif.setContexte(contexte);
        notif.setTypeNotification(typeNotification);

        notificationRepository.save(notif);

        if (envoyerEmail){

            emailService.envoyerEmail(user.getEmail(), titre, message);

            System.out.println("📨 Envoi du mail à " + user.getEmail() + " avec sujet : " + titre);

        }
    }
}
