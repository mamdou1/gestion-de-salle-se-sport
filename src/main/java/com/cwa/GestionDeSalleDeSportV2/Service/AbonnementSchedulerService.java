package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutEvent;
import com.cwa.GestionDeSalleDeSportV2.Entity.Evenement;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.EvenementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AbonnementSchedulerService {

    private final AbonnementRepository abonnementRepository;
    private final AbonnementService abonnementService;
    private final AbonnementEventService abonnementEventService;
    private final UserRepository userRepository;

    public AbonnementSchedulerService(AbonnementRepository abonnementRepository, AbonnementService abonnementService, AbonnementEventService abonnementEventService, UserRepository userRepository) {
        this.abonnementRepository = abonnementRepository;
        this.abonnementService = abonnementService;
        this.abonnementEventService = abonnementEventService;
        this.userRepository = userRepository;
    }

    // Chaque jour à 6h — mise à jour des statuts
    @Scheduled(cron = "0 0 6 * * *")
    public void verifierEtMettreAJourToutLesStatuts() throws MessagingException {

        List<Abonnement> abonnements = abonnementRepository.findAll();
        for (Abonnement abonnement : abonnements){

            StatutAbonnement statut = abonnementService.calculStatutAbonnemnt(abonnement);
            abonnement.setStatut(statut);
            abonnementRepository.save(abonnement);

            // Si l'abonnement est expiré : envenyer notif
            if (statut == StatutAbonnement.EXPIRE){
                abonnementEventService.notifierExpiration(abonnement);
            }
        }
    }

    // Tous les jours à 8h
 //   @Scheduled(cron = "*/15 * * * * *") //<--->  tout le 15 seconde
    @Scheduled(cron = "0 0 8 * * *")
    public void envoyerNotificationRappel() throws MessagingException {

        LocalDate aujourd_hui = LocalDate.now();
        List<Abonnement> abonnements = abonnementRepository.findAll();

        for (Abonnement abonnement : abonnements){
            if (abonnement.getDateRappelFinAbonnement() != null &&
                abonnement.getDateRappelFinAbonnement().equals(aujourd_hui) &&
                abonnement.getStatut() == StatutAbonnement.BIENTOT_EXPIRE){

                boolean doitNotifier = abonnement.getDateRappelFinAbonnement() != null &&
                        abonnement.getDateRappelFinAbonnement().isEqual(aujourd_hui) &&
                        abonnement.getStatut() == StatutAbonnement.BIENTOT_EXPIRE;

                if (doitNotifier) {
                    abonnementEventService.notifierRappelFin(abonnement);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void verifierRetraitFamille(){

        LocalDate aujourd_hui = LocalDate.now();
        List<User> membreARetirer = userRepository.findByDateRetrait(aujourd_hui);

        for (User membre : membreARetirer){
            membre.setTelephoneReference(null);
            membre.setFamille(null);
            membre.setDateRetrait(null);

            userRepository.save(membre);
        }
    }
}
