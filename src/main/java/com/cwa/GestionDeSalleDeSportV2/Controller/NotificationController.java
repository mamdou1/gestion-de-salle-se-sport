package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
import com.cwa.GestionDeSalleDeSportV2.Service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //  Afficher les notifilaction d'un Gym
    @GetMapping("/gym_notification")
    public ResponseEntity<List<Notification>> getNotificationByGym() throws AccessDeniedException {
        List<Notification> notif = notificationService.getNotificationByGym();
        return new ResponseEntity<>(notif, HttpStatus.OK);
    }

    //  Afficher les notifilaction d'un user
    @GetMapping("/user_notification")
    public ResponseEntity<List<Notification>> getNotificationByUser() throws AccessDeniedException {
        List<Notification> notif = notificationService.getNotificationByUser();
        return new ResponseEntity<>(notif, HttpStatus.OK);
    }

    //  Afficher les detailles d'une notifilaction
    @GetMapping("/consulter_detail_notif/{notificationId}")
    public ResponseEntity<Notification> consulterDetailNotif(@PathVariable Long notificationId) throws AccessDeniedException {
        Notification notif = notificationService.consulterDetailNotif(notificationId);
        return new ResponseEntity<>(notif, HttpStatus.OK);
    }

    //  Supprimer une notifilaction
    @DeleteMapping("/supprimer_notification/{notificationId}")
    public ResponseEntity<String> supprimerNotification(@PathVariable Long notificationId) throws AccessDeniedException {
        notificationService.supprimerNotification(notificationId);
        return new ResponseEntity<>("Notification suoorimer avec succès", HttpStatus.OK);
    }
}
