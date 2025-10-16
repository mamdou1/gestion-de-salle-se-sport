package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.AssignerCasierDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.CasierDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutCasier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Repository.CasierRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.SalleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.ListePaimentRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CasierService {

    private static final Logger logger = LoggerFactory.getLogger(CasierService.class);

    private final CasierRepository casierRepository;
    private final UserRepository userRepository;
    private final SalleRepository salleRepository;
    private final GymRepository gymRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final AbonnementEventService abonnementEventService;
    private final ListePaimentRepository listePaimentRepository;

    public CasierService(CasierRepository casierRepository, UserRepository userRepository, SalleRepository salleRepository,
                         GymRepository gymRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter,
                         AbonnementEventService abonnementEventService, ListePaimentRepository listePaimentRepository) {
        this.casierRepository = casierRepository;
        this.userRepository = userRepository;
        this.salleRepository = salleRepository;
        this.gymRepository = gymRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.abonnementEventService = abonnementEventService;
        this.listePaimentRepository = listePaimentRepository;
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (currentUser == null) {
            throw new AccessDeniedException("Utilisateur non authentifié.");
        }
        if (requireStaff && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        logger.debug("Utilisateur authentifié : {}", currentUser.getUsername());
        return currentUser;
    }

    private void verificationAccesGym(User staff, Gym gym, String action) {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new RuntimeException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }

    private void verificationAccesSalle(User staff, Salle salle, String action) {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(salle.getGym())) {
            throw new RuntimeException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette salle");
        }
    }

    // 1. Ajouter un nouveau casier dans une salle
    public Casier AjouterCasier(CasierDTO dto) throws AccessDeniedException {
        User staff = initializeAccess(true);
        Salle salle = salleRepository.findById(dto.getSalleId())
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        verificationAccesSalle(staff, salle, "ajouter un casier dans");

        Optional<Casier> existant = casierRepository.findByNumeroDeCasierAndSalle(dto.getNumeroDeCasier(), salle);
        if (existant.isPresent()) {
            throw new RuntimeException("Ce numéro de casier existe déjà dans cette salle");
        }

        Casier casier = new Casier();
        casier.setGym(salle.getGym());
        casier.setSalle(salle);
        casier.setNumeroDeCasier(dto.getNumeroDeCasier());
        casier.setPrix(dto.getPrix());
        casier.setStatut(StatutCasier.DISPONIBLE);

        Casier savedCasier = casierRepository.save(casier);
        logger.info("Casier {} ajouté avec succès pour la salle ID: {}", dto.getNumeroDeCasier(), salle.getId());
        return savedCasier;
    }

    // 2. Assigne un casier à un client si disponible
    public Casier assignerCasier(Long id, AssignerCasierDTO dto) throws AccessDeniedException, MessagingException {
        User staff = initializeAccess(true);
        Casier casier = casierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Casier non trouvé."));

        if (casier.getStatut() == StatutCasier.OCCUPER) {
            throw new RuntimeException("Ce casier a déjà été assigné.");
        }

        User membre = userRepository.findById(dto.getMembreId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé."));

        if (!membre.getGyms().contains(casier.getGym())) {
            throw new RuntimeException("Accès refusé : le membre n'est pas affilié à ce gym.");
        }

        BigDecimal prix = casier.getPrix();

        casier.setMembre(membre);
        casier.setDateDebut(LocalDate.now());
        casier.setDateFin(LocalDate.now().plusMonths(1));
        casier.setStatut(StatutCasier.OCCUPER);
        casier.setStaff(staff);
        casier.setModeDePaiement(dto.getModeDePaiement());
        casier.setDateRappelFinAbonnement(LocalDate.now()
                .plusMonths(dto.getNombreDeMois())
                .minusDays(5));
        casier.setPrix(prix.multiply(BigDecimal.valueOf(dto.getNombreDeMois())));

        // Sauvegarde du casier
        Casier savedCasier = casierRepository.save(casier);

        // Création de l'entrée dans liste_paiment
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.CASIER);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(casier.getPrix());
        paiement.setModeDePaiement(dto.getModeDePaiement());
        paiement.setReferenceId(savedCasier.getId());
        paiement.setAcheteur(membre);
        paiement.setStaffEnregistreur(staff);
        paiement.setGym(casier.getGym());
        paiement.setDetails("Assignation casier " + casier.getNumeroDeCasier() + " pour " + membre.getNom() + " " + membre.getPrenom());
        listePaimentRepository.save(paiement);
        logger.info("Created ListePaiment for assignation casier: {}", paiement);

        abonnementEventService.envoyerFactureCasierParEmail(membre, casier, "Validation");

        return savedCasier;
    }

    // 3. Casiers disponibles dans une salle
    public List<Casier> getCasierDisponibleDansSalle(Long salleId) throws AccessDeniedException {
        User staff = initializeAccess(true);
        Salle salle = salleRepository.findById(salleId).orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        verificationAccesSalle(staff, salle, "consulter les casiers disponibles dans");
        return casierRepository.findBySalleAndStatut(salle, StatutCasier.DISPONIBLE);
    }

    // 4. Tous les casiers d'une salle
    public List<Casier> getTousLesCasiersDisponibleDansSalle(Long salleId) throws AccessDeniedException {
        User staff = initializeAccess(true);
        Salle salle = salleRepository.findById(salleId).orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        verificationAccesSalle(staff, salle, "consulter tous les casiers dans");
        return casierRepository.findBySalle(salle);
    }

    // 5. Tous les casiers disponibles dans le gym
    public List<Casier> getTousCasierDisponibleDansGym(Long gymId) throws AccessDeniedException {
        User staff = initializeAccess(true);
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new RuntimeException("Gym non trouvé"));
        verificationAccesGym(staff, gym, "consulter les casiers disponibles dans");
        return casierRepository.findByGymAndStatut(gym, StatutCasier.DISPONIBLE);
    }

    // 6. getCasier By Id
    public Casier getCasierById(Long casierId) throws AccessDeniedException {
        User staff = initializeAccess(true);
        Casier casier = casierRepository.findById(casierId)
                .orElseThrow(() -> new RuntimeException("Casier non trouvé."));
        return casier;
    }

    // 7. Liste casier
    public List<Casier> listeCasier() throws AccessDeniedException {
        initializeAccess(true);
        List<Casier> casiers = casierRepository.findAll();
        return casiers;
    }

    // 8. Libérer un casier
    public Casier libererCasier(Long casierId) throws AccessDeniedException {
        User staff = initializeAccess(true);
        Casier casier = casierRepository.findById(casierId)
                .orElseThrow(() -> new RuntimeException("Casier non trouvé."));
        casier.setStatut(StatutCasier.DISPONIBLE);
        casier.setDateDebut(null);
        casier.setDateFin(null);
        casier.setMembre(null);
        casier.setStaff(null);
        casier.setModeDePaiement(null);
        Casier savedCasier = casierRepository.save(casier);
        logger.info("Casier {} libéré avec succès", casierId);
        return savedCasier;
    }

    // 9. Renouvellement d'un casier
    public Casier renouvellement(Long casierId, AssignerCasierDTO dto) throws AccessDeniedException, MessagingException {
        Casier casier = casierRepository.findById(casierId)
                .orElseThrow(() -> new RuntimeException("Casier non trouvé."));
        User currentUser = initializeAccess(true);

        if (!currentUser.getGyms().contains(casier.getGym())) {
            throw new AccessDeniedException("Accès refusé à cet abonnement.");
        }

        BigDecimal prix = casier.getPrix();

        if (casier.getStatut() == StatutCasier.OCCUPER) {
            LocalDate nouvelleDateFin = casier.getDateFin().plusMonths(dto.getNombreDeMois());
            casier.setDateFin(nouvelleDateFin);
            casier.setDateRappelFinAbonnement(nouvelleDateFin.minusDays(5));
            casier.setModeDePaiement(dto.getModeDePaiement());
            casier.setNombreDeMois(BigDecimal.valueOf(dto.getNombreDeMois()));
            casier.setPrix(prix.multiply(BigDecimal.valueOf(dto.getNombreDeMois())));
        }

        // Sauvegarde du casier
        Casier savedCasier = casierRepository.save(casier);

        // Création de l'entrée dans liste_paiment
        ListePaiment paiement = new ListePaiment();
        paiement.setTypePaiement(TypePaiement.CASIER);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(casier.getPrix());
        paiement.setModeDePaiement(dto.getModeDePaiement());
        paiement.setReferenceId(savedCasier.getId());
        paiement.setAcheteur(casier.getMembre());
        paiement.setStaffEnregistreur(currentUser);
        paiement.setGym(casier.getGym());
        paiement.setDetails("Renouvellement casier " + casier.getNumeroDeCasier() + " pour " + casier.getMembre().getNom() + " " + casier.getMembre().getPrenom());
        listePaimentRepository.save(paiement);
        logger.info("Created ListePaiment for renouvellement casier: {}", paiement);

        User membre = casier.getMembre();
        abonnementEventService.envoyerFactureCasierParEmail(membre, casier, "Renouvellement");

        return savedCasier;
    }
}