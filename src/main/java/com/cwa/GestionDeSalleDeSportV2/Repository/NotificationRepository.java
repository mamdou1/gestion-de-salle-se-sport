package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Notification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByGymDestinataireId(Long gymId);
    List<Notification> findByDestinataireId(Long userId);

    List<Notification> findByDestinataireIdAndEstLuFalse(Long userId);

    List<Notification> findByDestinataireIdAndEstLuFalseOrderByDateEnvoiDesc(Long destinataireId);

    long countByDestinataireIdAndEstLuFalse(Long destinataireId);

    List<Notification> findByDestinataireIdAndTypeNotification(Long destinataireId, TypeNotification typeNotification);

    @Query("SELECT n FROM Notification n WHERE n.destinataire.id = :destinataireId AND n.dateEnvoi >= CURRENT_DATE - 30 ORDER BY n.dateEnvoi DESC")
    List<Notification> findRecentNotificationsByDestinataire(@Param("destinataireId") Long destinataireId);

    long countByGymDestinataireIdAndEstLuFalse(Long gymId);

    // AJOUTÉ – Requête native qui marche à 100% (résout le bug LazyInitialization)
    @Query(value = "SELECT * FROM notification WHERE gym_destinataire_id = :gymId ORDER BY date_envoi DESC", nativeQuery = true)
    List<Notification> findAllByGymId(@Param("gymId") Long gymId);
}