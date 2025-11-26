package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
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
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public NotificationController(NotificationService notificationService, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.notificationService = notificationService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    // ***************************************
    // 🔔 NOTIFICATIONS GYM (React Staff)
    // ***************************************

    @GetMapping("/gym_notification")
    public ResponseEntity<List<Notification>> getNotificationByGym() throws AccessDeniedException {
        return ResponseEntity.ok(notificationService.getNotificationByGym());
    }

    @GetMapping("/gym_notification/unread-count")
    public ResponseEntity<Long> getUnreadGymNotificationsCount() throws AccessDeniedException {
        return ResponseEntity.ok(notificationService.countNotificationsNonLuesByGym());
    }

    @PutMapping("/gym_notification/{id}/lu")
    public ResponseEntity<Void> marquerNotificationGymCommeLue(@PathVariable Long id) throws AccessDeniedException {
        notificationService.marquerNotificationGymCommeLue(id);
        return ResponseEntity.ok().build();
    }


    // ******************************************
    // 🔔 NOTIFICATIONS USER (Flutter Membres)
    // ******************************************

    @GetMapping("/user_notification")
    public ResponseEntity<List<Notification>> getNotificationByUser() throws AccessDeniedException {
        return ResponseEntity.ok(notificationService.getNotificationByUser());
    }

    @PutMapping("/user_notification/{id}/lu")
    public ResponseEntity<Void> marquerNotificationCommeLue(@PathVariable Long id) throws AccessDeniedException {
        notificationService.marquerCommeLu(id);
        return ResponseEntity.ok().build();
    }


    // ***************************************
    // 🔎 CONSULTER + SUPPRIMER NOTIFICATION
    // ***************************************

    @GetMapping("/consulter_detail_notif/{notificationId}")
    public ResponseEntity<Notification> consulterDetailNotif(@PathVariable Long notificationId) throws AccessDeniedException {
        return ResponseEntity.ok(notificationService.consulterDetailNotif(notificationId));
    }

    @DeleteMapping("/supprimer_notification/{notificationId}")
    public ResponseEntity<String> supprimerNotification(@PathVariable Long notificationId) throws AccessDeniedException {
        notificationService.supprimerNotification(notificationId);
        return ResponseEntity.ok("Notification supprimée avec succès");
    }


    // ***************************************
    // 🧪 TEST NOTIFICATION GYM
    // ***************************************

    @PostMapping("/test-gym-notification")
    public ResponseEntity<String> createTestGymNotification() {

        try {
            User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

            if (currentUser.getGym() == null) {
                return ResponseEntity.badRequest().body("❌ Aucun gym associé à l'utilisateur");
            }

            notificationService.notificationAdminAGym(
                    currentUser.getGym(),
                    "📦 Test Notification - Commande #" + System.currentTimeMillis(),
                    "Ceci est une notification de test pour vérifier que le système fonctionne correctement.",
                    "TEST",
                    TypeNotification.VALIDATION_PANIER,
                    false
            );

            return ResponseEntity.ok("✅ Notification de test créée avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur: " + e.getMessage());
        }
    }
}
