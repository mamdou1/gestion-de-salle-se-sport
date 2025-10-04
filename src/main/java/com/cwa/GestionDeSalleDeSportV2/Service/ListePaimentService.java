package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Repository.*;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListePaimentService {

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
    }

    /**
     * Récupère tous les paiements du gym de l'utilisateur courant
     * @return Liste de tous les paiements (abonnements, frais d'inscription, casiers, ventes)
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public List<ListePaiment> getAllPaiements() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        Gym userGym = currentUser.getGym();
        verificationAccesGym(currentUser, userGym, "consulter les paiements dans");

        return ListePaimentRepository.findByGymId(userGym.getId())
                .stream()
                .sorted((p1, p2) -> p2.getDatePaiement().compareTo(p1.getDatePaiement()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les paiements filtrés par type
     * @param type Type de paiement ("ABONNEMENT", "FRAIS_INSCRIPTION", "CASIER", "VENTE")
     * @return Liste filtrée des paiements
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public List<ListePaiment> getPaiementsByType(String type) throws AccessDeniedException {
        List<ListePaiment> tousLesPaiements = getAllPaiements();

        return tousLesPaiements.stream()
                .filter(paiement -> paiement.getTypePaiement().equals(type))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les paiements d'une période spécifique
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste filtrée des paiements
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public List<ListePaiment> getPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) throws AccessDeniedException {
        List<ListePaiment> tousLesPaiements = getAllPaiements();

        return tousLesPaiements.stream()
                .filter(paiement -> {
                    LocalDate datePaiement = paiement.getDatePaiement().toLocalDate();
                    return !datePaiement.isBefore(dateDebut) && !datePaiement.isAfter(dateFin);
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcule le total des paiements pour une période
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Montant total
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public BigDecimal getTotalPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) throws AccessDeniedException {
        List<ListePaiment> paiements = getPaiementsByPeriode(dateDebut, dateFin);

        return paiements.stream()
                .map(ListePaiment::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère les statistiques de paiements par type
     *
     * @return Map avec le total par type de paiement
     * @throws AccessDeniedException Si l'utilisateur n'est pas autorisé
     */
    public Map<TypePaiement, BigDecimal> getStatistiquesPaiements() throws AccessDeniedException {
        List<ListePaiment> tousLesPaiements = getAllPaiements();

        return tousLesPaiements.stream()
                .collect(Collectors.groupingBy(
                        ListePaiment::getTypePaiement,
                        Collectors.reducing(BigDecimal.ZERO, ListePaiment::getMontant, BigDecimal::add)
                ));
    }



    // Méthodes d'accès et vérification
    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) {
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