package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Casier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutCasier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Salle;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.CasierRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.SalleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CasierService {

    private final CasierRepository casierRepository;
    private final UserRepository userRepository;
    private final SalleRepository salleRepository;
    private final GymRepository gymRepository;

    public CasierService(CasierRepository casierRepository, UserRepository userRepository, SalleRepository salleRepository, GymRepository gymRepository) {
        this.casierRepository = casierRepository;
        this.userRepository = userRepository;
        this.salleRepository = salleRepository;
        this.gymRepository = gymRepository;
    }

    private void veriicationAccesGym(User staff, Gym gym, String action){
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)){
            throw new RuntimeException("Accès refusé : l'utilsateur n'est pas autorisé à " + action + "cette gym");
        }
    }

    private void verificationAccesSalle(User staff, Salle salle, String action){
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(salle.getGym())){
            throw new RuntimeException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + "cette salle");
        }
    }

    //  1.  Ajouter un nouveau casier dans une salle (exemple de salle vestiere 1 = Homme, vestiere 2= Femme)
    public Casier AjouterCasier(User staff, Long salleId, String numeroDeCasier, BigDecimal prix ){
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(()-> new RuntimeException("Salle non Trouvé"));
        verificationAccesSalle(staff, salle, "ajouter un casier dans");

        Optional<Casier> existant = casierRepository.findByNumeroDeCasierAndSalle(numeroDeCasier, salle);
        if (existant.isPresent()){
            throw new RuntimeException("Ce numéro de casier existe déjà dans cette salle");
        }

        Casier casier = new Casier();
        casier.setGym(salle.getGym());
        casier.setSalle(salle);
        casier.setNumeroDeCasier(numeroDeCasier);
        casier.setPrix(prix);
        casier.setStatut(StatutCasier.DISPONIBLE);

        return casierRepository.save(casier);
    }

    //  2.  Assigne un casier à un client si disponible
    public Casier assignerCasier( User staff, Long salleId, Long membreId, BigDecimal prix){
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(()-> new RuntimeException("Salle non trouver."));
        User membre = userRepository.findById(membreId)
                .orElseThrow(()->new RuntimeException("Membre nom trouver."));
        verificationAccesSalle(staff, salle, "assigner un casier dans");

        if (!membre.getGyms().contains(salle.getGym())){
            throw new RuntimeException("Accès refusé : le membre n'est pas affilié à ce gym.");
        }

        Casier casierDispo = casierRepository.findBySalleAndStatut(salle, StatutCasier.DISPONIBLE)
                .stream().findFirst()
                .orElseThrow(()-> new RuntimeException("Il n'y a pas de casier disponible"));

        casierDispo.setMembre(membre);
        casierDispo.setPrix(prix);
        casierDispo.setDateDebut(LocalDate.now());
        casierDispo.setDateFin(LocalDate.now().plusMonths(1));
        casierDispo.setStatut(StatutCasier.OCCUPER);

        return casierRepository.save(casierDispo);
    }

    //  3.  Casiers disponibles dans une salle
    public List<Casier> getCasierDisponibleDansSalle(User staff,Long salleId){
        Salle salle = salleRepository.findById(salleId).orElseThrow();
        verificationAccesSalle(staff, salle, "consulter les casier disponible dans");
        return casierRepository.findBySalleAndStatut(salle, StatutCasier.DISPONIBLE);
    }

    //  4.  Tous les casiers d'une salle
    public List<Casier> getTousLesCasiersDisponibleDansSalle(User staff,Long salleId){
        Salle salle = salleRepository.findById(salleId).orElseThrow();
        verificationAccesSalle(staff, salle, "conslter tout les casiers dans");
        return casierRepository.findBySalle(salle);
    }

    //  5.  Tout les casier disponibles dans le gym
    public  List<Casier> getTousCasierDisponibleDansGym(User staff,Long gymId){
        Gym gym = gymRepository.findById(gymId).orElseThrow();
        veriicationAccesGym(staff, gym, "consulter les casier disponibles dans");
        return casierRepository.findByGymAndStatut(gym, StatutCasier.DISPONIBLE);
    }
}

