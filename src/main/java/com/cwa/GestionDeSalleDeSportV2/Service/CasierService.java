package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.DTO.AssignerCasierDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CasierDTO;
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
import java.nio.file.AccessDeniedException;
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
    private final ProduitService produitService;

    public CasierService(CasierRepository casierRepository, UserRepository userRepository, SalleRepository salleRepository, GymRepository gymRepository, ProduitService produitService) {
        this.casierRepository = casierRepository;
        this.userRepository = userRepository;
        this.salleRepository = salleRepository;
        this.gymRepository = gymRepository;
        this.produitService = produitService;
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
    // Dans CasierService
    public Casier AjouterCasier(CasierDTO dto) throws AccessDeniedException {
        User staff = produitService.initializeAccess(true);
        Salle salle = salleRepository.findById(dto.getSalleId())
                .orElseThrow(()-> new RuntimeException("Salle non Trouvé"));

        // Plus besoin de récupérer le staff depuis le repository
        verificationAccesSalle(staff, salle, "ajouter un casier dans");

        Optional<Casier> existant = casierRepository.findByNumeroDeCasierAndSalle(dto.getNumeroDeCasier(), salle);
        if (existant.isPresent()){
            throw new RuntimeException("Ce numéro de casier existe déjà dans cette salle");
        }

        Casier casier = new Casier();
        casier.setGym(salle.getGym());
        casier.setSalle(salle);
        casier.setNumeroDeCasier(dto.getNumeroDeCasier());
        casier.setPrix(dto.getPrix());
        casier.setStatut(StatutCasier.DISPONIBLE);
        //casier.setGym(staff.getGym());

        return casierRepository.save(casier);
    }

    //  2.  Assigne un casier à un client si disponible
    public Casier assignerCasier( AssignerCasierDTO dto) throws AccessDeniedException {
        User staff = produitService.initializeAccess(true);
        Casier casier = casierRepository.findById(dto.getId())
                .orElseThrow(()-> new RuntimeException("Casier non trouver."));

        if (casier.getStatut() == StatutCasier.OCCUPER){
            throw new RuntimeException("Ce casier a déjà été assigner.");
        }

        User membre = userRepository.findById(dto.getMembreId())
                .orElseThrow(()->new RuntimeException("Membre nom trouver."));

        if (!membre.getGyms().contains(casier.getGym())){
            throw new RuntimeException("Accès refusé : le membre n'est pas affilié à ce gym.");
        }

        casier.setMembre(membre);
        casier.setDateDebut(dto.getDateDebut());
        casier.setDateFin(LocalDate.now().plusMonths(1));
        casier.setStatut(StatutCasier.OCCUPER);
        casier.setStaff(staff);

        return casierRepository.save(casier);
    }

    //  3.  Casiers disponibles dans une salle
    public List<Casier> getCasierDisponibleDansSalle(Long salleId) throws AccessDeniedException {
        User staff = produitService.initializeAccess(true);
        Salle salle = salleRepository.findById(salleId).orElseThrow();
        verificationAccesSalle(staff, salle, "consulter les casier disponible dans");
        return casierRepository.findBySalleAndStatut(salle, StatutCasier.DISPONIBLE);
    }

    //  4.  Tous les casiers d'une salle
    public List<Casier> getTousLesCasiersDisponibleDansSalle(Long salleId) throws AccessDeniedException {
        User staff = produitService.initializeAccess(true);
        Salle salle = salleRepository.findById(salleId).orElseThrow();
        verificationAccesSalle(staff, salle, "conslter tout les casiers dans");
        return casierRepository.findBySalle(salle);
    }

    //  5.  Tout les casier disponibles dans le gym
    public  List<Casier> getTousCasierDisponibleDansGym(Long gymId) throws AccessDeniedException {
        User staff = produitService.initializeAccess(true);
        Gym gym = gymRepository.findById(gymId).orElseThrow();
        veriicationAccesGym(staff, gym, "consulter les casier disponibles dans");
        return casierRepository.findByGymAndStatut(gym, StatutCasier.DISPONIBLE);
    }

    //  6.  getCasier By Id
    public Casier getCasierById(Long casierId) throws AccessDeniedException {
        produitService.initializeAccess(true);

        Casier casier = casierRepository.findById(casierId)
                .orElseThrow(()->new RuntimeException("Casier non trouver."));
        return casier;
    }

    //  7.  Liste casier
    public List<Casier> listeCasier() throws AccessDeniedException {
        produitService.initializeAccess(true);
        List<Casier> casiers = casierRepository.findAll();
        return casiers;
    }
}

