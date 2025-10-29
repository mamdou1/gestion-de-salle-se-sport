package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListePaimentService {

    private static final Logger logger = LoggerFactory.getLogger(ListePaimentService.class);

    private final AbonnementRepository abonnementRepository;
    private final UserRepository userRepository;
    private final CasierRepository casierRepository;
    private final VenteRepository venteRepository;
    private final ListePaimentRepository ListePaimentRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public ListePaimentService(AbonnementRepository abonnementRepository,
                               UserRepository userRepository,
                               CasierRepository casierRepository,
                               VenteRepository venteRepository, ListePaimentRepository ListePaimentRepository,
                               UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.abonnementRepository = abonnementRepository;
        this.userRepository = userRepository;
        this.casierRepository = casierRepository;
        this.venteRepository = venteRepository;
        this.ListePaimentRepository = ListePaimentRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        logger.info("ListePaimentService initialized");
    }

    public List<ListePaiment> getAllPaiements() throws AccessDeniedException {
        logger.info("Entering getAllPaiements");
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les paiements dans");

        // UTILISEZ LA MÉTHODE AVEC JOIN FETCH POUR CHARGER L'ACHETEUR
        List<ListePaiment> paiements = ListePaimentRepository.findByGymIdWithAcheteur(userGym.getId())
                .stream()
                .sorted((p1, p2) -> {
                    LocalDateTime d1 = p1.getDatePaiement();
                    LocalDateTime d2 = p2.getDatePaiement();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d2.compareTo(d1);
                })
                .collect(Collectors.toList());

        // DEBUG: Vérifiez que l'acheteur est bien chargé
        paiements.forEach(paiement -> {
            if (paiement.getAcheteur() != null) {
                logger.info("✅ Paiement ID: {}, Acheteur: {} {}",
                        paiement.getId(),
                        paiement.getAcheteur().getPrenom(),
                        paiement.getAcheteur().getNom());
            } else {
                logger.warn("❌ Paiement ID: {}, Acheteur: NULL", paiement.getId());
            }
        });

        logger.info("Retrieved {} payments for gymId={}", paiements.size(), userGym.getId());
        return paiements;
    }


    public List<ListePaiment> getPaiementsByType(String type) throws AccessDeniedException {
        logger.info("Entering getPaiementsByType with type={}", type);
        List<ListePaiment> tousLesPaiements = getAllPaiements();
        logger.debug("Filtering {} payments by type={}", tousLesPaiements.size(), type);

        List<ListePaiment> filteredPaiements = tousLesPaiements.stream()
                .filter(paiement -> paiement.getTypePaiement().equals(type))
                .collect(Collectors.toList());
        logger.info("Found {} payments for type={}", filteredPaiements.size(), type);
        return filteredPaiements;
    }

    public List<ListePaiment> getPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) throws AccessDeniedException {
        logger.info("Entering getPaiementsByPeriode with dateDebut={}, dateFin={}", dateDebut, dateFin);
        List<ListePaiment> tousLesPaiements = getAllPaiements();
        logger.debug("Filtering {} payments by period {} to {}", tousLesPaiements.size(), dateDebut, dateFin);

        List<ListePaiment> filteredPaiements = tousLesPaiements.stream()
                .filter(paiement -> {
                    if (paiement.getDatePaiement() == null) {
                        logger.warn("Payment with id={} has null datePaiement", paiement.getId());
                        return false;
                    }
                    LocalDate datePaiement = paiement.getDatePaiement().toLocalDate();
                    return !datePaiement.isBefore(dateDebut) && !datePaiement.isAfter(dateFin);
                })
                .collect(Collectors.toList());
        logger.info("Found {} payments for period {} to {}", filteredPaiements.size(), dateDebut, dateFin);
        return filteredPaiements;
    }

    public BigDecimal getTotalPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) throws AccessDeniedException {
        logger.info("Entering getTotalPaiementsByPeriode with dateDebut={}, dateFin={}", dateDebut, dateFin);
        List<ListePaiment> paiements = getPaiementsByPeriode(dateDebut, dateFin);
        logger.debug("Calculating total for {} payments", paiements.size());

        BigDecimal total = paiements.stream()
                .map(ListePaiment::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.info("Total amount calculated: {}", total);
        return total;
    }

    public Map<TypePaiement, BigDecimal> getStatistiquesPaiements() throws AccessDeniedException {
        logger.info("Entering getStatistiquesPaiements");
        List<ListePaiment> tousLesPaiements = getAllPaiements();
        logger.debug("Generating statistics for {} payments", tousLesPaiements.size());

        Map<TypePaiement, BigDecimal> stats = tousLesPaiements.stream()
                .collect(Collectors.groupingBy(
                        ListePaiment::getTypePaiement,
                        Collectors.reducing(BigDecimal.ZERO, ListePaiment::getMontant, BigDecimal::add)
                ));
        logger.info("Generated statistics: {}", stats);
        return stats;
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        logger.info("Entering initializeAccess with requireStaff={}", requireStaff);
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        logger.debug("Current user: id={}, role={}", currentUser.getId(), currentUser.getRole());
        if (requireStaff && currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            logger.error("Access denied: User role {} is not authorized", currentUser.getRole());
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) {
            logger.error("Access denied: No gym associated with user id={}", currentUser.getId());
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        logger.info("Access initialized for user id={}", currentUser.getId());
        return currentUser;
    }

    private void verificationAccesGym(User staff, Gym gym, String action) throws AccessDeniedException {
        logger.info("Entering verificationAccesGym for userId={}, gymId={}, action={}",
                staff.getId(), gym != null ? gym.getId() : "null", action);
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            logger.error("Access denied: User id={} not authorized for gymId={} for action={}",
                    staff.getId(), gym != null ? gym.getId() : "null", action);
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
        logger.info("Access verified for userId={} in gymId={}", staff.getId(), gym.getId());
    }
}