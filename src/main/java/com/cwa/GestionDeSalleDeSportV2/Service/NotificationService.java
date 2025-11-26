package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.NotificationRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.userRepository = userRepository;
    }

    // Notification simple pour un utilisateur
    public void notification(User user, String titre, String message, String contexte, TypeNotification typeNotification, boolean envoyerEmail) throws MessagingException {

        Notification notif = new Notification();
        notif.setTitre(titre);
        notif.setContenu(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setDestinataire(user);
        notif.setContexte(contexte);
        notif.setTypeNotification(typeNotification);
        // Juste un log propre si tu veux
        System.out.println("Envoi notification à " + user.getNom() + " → Type: " + typeNotification);

        notificationRepository.save(notif);

        if (envoyerEmail) {
            emailService.envoyerEmail(user.getEmail(), titre, message);
            System.out.println("📨 Envoi du mail à " + user.getEmail() + " avec sujet : " + titre);
        }
    }

    // Notification pour un gym (staff)
    public void notificationAdminAGym(Gym gym, String titre, String message, String contexte, TypeNotification typeNotification, boolean envoyerEmail) throws MessagingException {

        System.out.println("🔔 Création notification pour le gym: " + gym.getNom());
        System.out.println("   📝 Titre: " + titre);
        System.out.println("   📄 Contenu: " + message);

        Notification notif = new Notification();
        notif.setTitre(titre);
        notif.setContenu(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setGymDestinataire(gym);
        notif.setContexte(contexte);
        notif.setTypeNotification(typeNotification);

        Notification savedNotification = notificationRepository.save(notif);
        System.out.println("✅ Notification créée avec ID: " + savedNotification.getId());

        if (envoyerEmail) {
            emailService.envoyerEmail(gym.getEmail(), titre, message);
            System.out.println("📨 Envoi du mail à " + gym.getEmail() + " avec sujet : " + titre);
        }
    }

    // Notification gym + membre
    public void notifyGymAndMember(Gym gym, User member, String title, String content, String context, TypeNotification type, boolean sendEmail) throws MessagingException {

        // 🔔 Notification membre
        Notification memberNotif = new Notification();
        memberNotif.setTitre(title);
        memberNotif.setContenu(content);
        memberNotif.setDateEnvoi(LocalDateTime.now());
        memberNotif.setDestinataire(member);
        memberNotif.setContexte(context);
        memberNotif.setTypeNotification(type);
        notificationRepository.save(memberNotif);

        if (sendEmail) {
            emailService.envoyerEmailAvecPieceJointeGymAMembre(
                    gym.getEmail(),
                    member.getEmail(),
                    title,
                    content,
                    null,
                    null
            );
        }

        // 🔔 Notification staff (gym)
        Notification staffNotif = new Notification();
        staffNotif.setTitre("Nouvelle demande de validation de panier");
        staffNotif.setContenu("Le membre " + member.getPrenom() + " " + member.getNom() +
                " a envoyé un panier pour validation. " + content);
        staffNotif.setDateEnvoi(LocalDateTime.now());
        staffNotif.setGymDestinataire(gym);
        staffNotif.setContexte("VALIDATION_PANIER");
        staffNotif.setTypeNotification(TypeNotification.VALIDATION_PANIER);

        notificationRepository.save(staffNotif);
        System.out.println("✅ Notification staff créée pour le gym: " + gym.getNom());
    }

    // Inscription en ligne - membre
    public void notifyInscriptionEnLigne(User member, String title, String content, String context, TypeNotification type) {
        Notification memberNotif = new Notification();
        memberNotif.setTitre(title);
        memberNotif.setContenu(content);
        memberNotif.setDateEnvoi(LocalDateTime.now());
        memberNotif.setDestinataire(member);
        memberNotif.setContexte(context);
        memberNotif.setTypeNotification(type);

        notificationRepository.save(memberNotif);
    }

    // Inscription en ligne - gym
    public void notifyInscriptionEnLigneGym(Gym gym, String title, String content, String context, TypeNotification type) {
        Notification memberNotif = new Notification();
        memberNotif.setTitre(title);
        memberNotif.setContenu(content);
        memberNotif.setDateEnvoi(LocalDateTime.now());
        memberNotif.setGymDestinataire(gym);
        memberNotif.setContexte(context);
        memberNotif.setTypeNotification(type);

        notificationRepository.save(memberNotif);
    }

    // 🗑️ Supprimer une notification
    public void supprimerNotification(Long id) throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        verificationAccesGym(currentUser, currentUser.getGym(), "supprimer une notification dans");

        notificationRepository.delete(notification);
    }

    // 🔍 Consulter le détail d'une notification
    public Notification consulterDetailNotif(Long notificationId) throws AccessDeniedException {
        User currentUser = initializeAccess(false);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        // 🔐 Sécurité : empêcher lecture d'une notification qui ne nous appartient pas
        if (notification.getDestinataire() != null &&
                !notification.getDestinataire().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à consulter cette notification");
        }

        if (notification.getGymDestinataire() != null) {
            if (currentUser.getGym() == null ||
                    !notification.getGymDestinataire().getId().equals(currentUser.getGym().getId())) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à consulter cette notification");
            }
        }

        return notification;
    }

    // 🔔 Notifications d'un gym (staff)
    public List<Notification> getNotificationByGym() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Long gymId = currentUser.getGym().getId();

        System.out.println("🔔 Récupération notifications pour le gym ID: " + gymId);
        List<Notification> notifications = notificationRepository.findByGymDestinataireId(gymId);
        System.out.println("📊 Notifications trouvées: " + notifications.size());

        return notifications;
    }

    // 🔔 Notifications d'un utilisateur
    public List<Notification> getNotificationByUser() throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        return notificationRepository.findByDestinataireId(currentUser.getId());
    }

    // ✔️ Marquer une notification User comme lue
    public void marquerCommeLu(Long notificationId) throws AccessDeniedException {
        User currentUser = initializeAccess(false);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable avec l'ID: " + notificationId));

        if (notification.getDestinataire() == null ||
                !notification.getDestinataire().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette notification");
        }

        if (!notification.isEstLu()) {
            notification.setEstLu(true);
            notificationRepository.save(notification);
            System.out.println("✅ Notification " + notificationId + " marquée comme lue");
        }
    }

    // ✔️ Marquer toutes les notifications user comme lues
    public void marquerToutesCommeLues(Long userId) throws AccessDeniedException {
        User currentUser = initializeAccess(false);

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Vous ne pouvez marquer que vos propres notifications comme lues");
        }

        List<Notification> notificationsNonLues = notificationRepository.findByDestinataireIdAndEstLuFalse(userId);

        if (!notificationsNonLues.isEmpty()) {
            for (Notification notification : notificationsNonLues) {
                notification.setEstLu(true);
            }
            notificationRepository.saveAll(notificationsNonLues);
            System.out.println("✅ " + notificationsNonLues.size() + " notifications marquées comme lues");
        }
    }

    // ✔️ Notifications non lues (user)
    public List<Notification> getNotificationsNonLuesByUser() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        return notificationRepository.findByDestinataireIdAndEstLuFalseOrderByDateEnvoiDesc(currentUser.getId());
    }

    // ✔️ Compter les notifications non lues (user)
    public long countNotificationsNonLuesByUser() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        return notificationRepository.countByDestinataireIdAndEstLuFalse(currentUser.getId());
    }

    // ✔️ Marquer notification gym comme lue (staff)
    public void marquerNotificationGymCommeLue(Long notificationId) throws AccessDeniedException {
        User currentUser = initializeAccess(true);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable avec l'ID: " + notificationId));

        if (notification.getGymDestinataire() == null ||
                !notification.getGymDestinataire().getId().equals(currentUser.getGym().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette notification");
        }

        if (!notification.isEstLu()) {
            notification.setEstLu(true);
            notificationRepository.save(notification);
            System.out.println("✅ Notification gym " + notificationId + " marquée comme lue");
        }
    }

    // ✔️ Compter notifications non lues d'un gym
    public long countNotificationsNonLuesByGym() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        return notificationRepository.countByGymDestinataireIdAndEstLuFalse(currentUser.getGym().getId());
    }

    // Vérification accès staff ou utilisateur simple
    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        if (requireStaff &&
                currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.RECEPTIONNISTE &&
                currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }

        if (requireStaff && currentUser.getGym() == null) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        return currentUser;
    }

    // Vérification que le staff appartient au gym
    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        if (staff.getGym() == null || !staff.getGym().getId().equals(gym.getId())) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }
}

