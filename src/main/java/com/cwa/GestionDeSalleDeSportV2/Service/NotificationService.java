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
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
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

    public void notificationAdminAGym(Gym gym, String titre, String message, String contexte, TypeNotification typeNotification, boolean envoyerEmail) throws MessagingException {

        // User user1 = new User();

        Notification notif = new Notification();

        notif.setTitre(titre);
        notif.setContenu(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setGymDestinataire(gym);
        notif.setContexte(contexte);
        notif.setTypeNotification(typeNotification);

        notificationRepository.save(notif);

        if (envoyerEmail){

            emailService.envoyerEmail(gym.getEmail(), titre, message);

            System.out.println("📨 Envoi du mail à " + gym.getEmail() + " avec sujet : " + titre);

        }

    }



    public void notifyGymAndMember(Gym gym, User member, String title, String content, String context, TypeNotification type, boolean sendEmail)  {
        // Notification pour le membre
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
                    null, // Pas de pièce jointe ici, ajustez si nécessaire
                    null
            );
        }

        // Notification pour le staff/admin de la gym
        //List<User> gymStaff = userRepository.findByGymsContainingAndRoleIn(gym, List.of(Role.ADMIN, Role.GERANT, Role.RECEPTIONNISTE));
//        //for (User staff : gymStaff) {
//            Notification staffNotif = new Notification();
//            staffNotif.setTitre("Action dans votre gym : " + title);
//            staffNotif.setContenu("Une action concernant " + member.getPrenom() + " " + member.getNom() + " : " + content);
//            staffNotif.setDateEnvoi(LocalDateTime.now());
//            //staffNotif.setDestinataire(staff);
//            staffNotif.setGymDestinataire(gym);
//            staffNotif.setContexte(context);
//            staffNotif.setTypeNotification(type);
//            notificationRepository.save(staffNotif);

//            if (sendEmail) {
//                emailService.envoyerEmailAvecPieceJointe(
//                        gym.getEmail(),
//                        staff.getEmail(),
//                        "Action dans votre gym : " + title,
//                        "Détails : " + content,
//                        null,
//                        null
//                );
//            }
        //}
    }

    public void notifyInscriptionEnLigne(User member, String title, String content, String context, TypeNotification type) {
        // Notification pour le membre
        Notification memberNotif = new Notification();
        memberNotif.setTitre(title);
        memberNotif.setContenu(content);
        memberNotif.setDateEnvoi(LocalDateTime.now());
        memberNotif.setDestinataire(member);
        memberNotif.setContexte(context);
        memberNotif.setTypeNotification(type);
        notificationRepository.save(memberNotif);

    }

    public void notifyInscriptionEnLigneGym(Gym gym, String title, String content, String context, TypeNotification type) {
        // Notification pour le membre
        Notification memberNotif = new Notification();
        memberNotif.setTitre(title);
        memberNotif.setContenu(content);
        memberNotif.setDateEnvoi(LocalDateTime.now());
        memberNotif.setGymDestinataire(gym);
        memberNotif.setContexte(context);
        memberNotif.setTypeNotification(type);
        notificationRepository.save(memberNotif);

    }


    //  Supprimer une notifilaction
    public void supprimerNotification(Long id) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        verificationAccesGym(currentUser, currentUser.getGym(), "supprimer un produit dans");
        notificationRepository.delete(notification);
    }

//    public List<Produit> listerProduitNotifications() throws AccessDeniedException {
//        User currentUser = initializeAccess(false);
//        verificationAccesGym(currentUser, currentUser.getGym(), "consulter la liste les produits dans");
//        return notificationRepository.findByGym(currentUser.getGym());
//    }

    //  Afficher les detailles d'une notifilaction
    public Notification consulterDetailNotif(Long notificationId) throws AccessDeniedException {
        initializeAccess(false);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()->new RuntimeException("Notification introuvable"));
        return notification;
    }

    //  Afficher les notifilaction d'un Gym
    public List<Notification> getNotificationByGym() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Long gymId = currentUser.getGym().getId();
        return notificationRepository.findByGymDestinataireId(gymId);
    }

    //  Afficher les notifilaction d'un user
    public List<Notification> getNotificationByUser() throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        return notificationRepository.findByDestinataireId(currentUser.getId());
    }


    /**
     * Marque une notification spécifique comme lue
     * @param notificationId ID de la notification à marquer comme lue
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé à modifier cette notification
     */
    public void marquerCommeLu(Long notificationId) throws AccessDeniedException {
        User currentUser = initializeAccess(false);

        // Récupérer la notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable avec l'ID: " + notificationId));

        // Vérifier que l'utilisateur est bien le destinataire de la notification
        if (notification.getDestinataire() == null ||
                !notification.getDestinataire().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette notification");
        }

        // Marquer comme lue seulement si ce n'est pas déjà le cas
        if (!notification.isEstLu()) {
            notification.setEstLu(true);
            notificationRepository.save(notification);

            System.out.println("✅ Notification " + notificationId + " marquée comme lue pour l'utilisateur " + currentUser.getId());
        }
    }

    /**
     * Marque toutes les notifications de l'utilisateur comme lues
     * @param userId ID de l'utilisateur
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public void marquerToutesCommeLues(Long userId) throws AccessDeniedException {
        User currentUser = initializeAccess(false);

        // Vérifier que l'utilisateur ne peut marquer que ses propres notifications comme lues
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Vous ne pouvez marquer que vos propres notifications comme lues");
        }

        // Récupérer toutes les notifications non lues de l'utilisateur
        List<Notification> notificationsNonLues = notificationRepository.findByDestinataireIdAndEstLuFalse(userId);

        if (!notificationsNonLues.isEmpty()) {
            // Marquer toutes comme lues
            for (Notification notification : notificationsNonLues) {
                notification.setEstLu(true);
            }

            // Sauvegarder en une seule opération
            notificationRepository.saveAll(notificationsNonLues);

            System.out.println("✅ " + notificationsNonLues.size() + " notifications marquées comme lues pour l'utilisateur " + userId);
        }
    }

    /**
     * Récupère les notifications non lues de l'utilisateur connecté
     * @return Liste des notifications non lues
     * @throws AccessDeniedException Si l'utilisateur n'est pas connecté
     */
    public List<Notification> getNotificationsNonLuesByUser() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        return notificationRepository.findByDestinataireIdAndEstLuFalseOrderByDateEnvoiDesc(currentUser.getId());
    }

    /**
     * Compte le nombre de notifications non lues de l'utilisateur connecté
     * @return Nombre de notifications non lues
     * @throws AccessDeniedException Si l'utilisateur n'est pas connecté
     */
    public long countNotificationsNonLuesByUser() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        return notificationRepository.countByDestinataireIdAndEstLuFalse(currentUser.getId());
    }


    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) { // Vérification du gym uniquement pour staff
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }

    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }
}
