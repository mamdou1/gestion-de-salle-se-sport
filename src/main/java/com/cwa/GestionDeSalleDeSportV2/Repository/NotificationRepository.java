package com.cwa.GestionDeSalleDeSportV2.Repository;

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

    // Récupérer les notifications non lues d'un utilisateur

    // Récupérer les notifications non lues d'un utilisateur triées par date
    List<Notification> findByDestinataireIdAndEstLuFalseOrderByDateEnvoiDesc(Long destinataireId);

    // Compter les notifications non lues d'un utilisateur
    long countByDestinataireIdAndEstLuFalse(Long destinataireId);

    // Récupérer les notifications par type pour un utilisateur
    List<Notification> findByDestinataireIdAndTypeNotification(Long destinataireId, String typeNotification);

    // Récupérer les notifications récentes (des 30 derniers jours)
    @Query("SELECT n FROM Notification n WHERE n.destinataire.id = :destinataireId AND n.dateEnvoi >= CURRENT_DATE - 30 ORDER BY n.dateEnvoi DESC")
    List<Notification> findRecentNotificationsByDestinataire(@Param("destinataireId") Long destinataireId);
}
