package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.ConfirmationToastDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Service
public class FamilleAbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final FamilleRepository familleRepository;
    private final FactureCollectiveService factureCollectiveService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public FamilleAbonnementService(
            AbonnementRepository abonnementRepository,
            FamilleRepository familleRepository,
            FactureCollectiveService factureCollectiveService,
            EmailService emailService,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            UserService userService,
            UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.abonnementRepository = abonnementRepository;
        this.familleRepository = familleRepository;
        this.factureCollectiveService = factureCollectiveService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userService = userService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }


    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }

    //  1.  Crée un abonnement familial pour une famille
    public void creerAbonnementFamilial(FamilleAbonnementDTO dto) throws MessagingException {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();;
        if (!estMembreDuStaff(currentUser)){
            throw new RuntimeException("Seul les membres du staff peuvent créer un abonnement familial");
        }

        Famille famille = familleRepository.findById(dto.getFamilleId())
                .orElseThrow(()-> new RuntimeException("Famille non trouver"));

        List<User> membres = famille.getMembres();
        BigDecimal total = BigDecimal.ZERO;
        List<Abonnement> abonnements = new ArrayList<>();

        for (User membre : membres){
            BigDecimal base = membre.getGenre().name().equals("FEMME") ? dto.getTarifFemme() : dto.getTarifHomme();
            BigDecimal montantFinal = base.subtract(dto.getReductionParPersonne());

            Abonnement abonnement = new Abonnement();

            abonnement.setFamille(famille);
            abonnement.setMembre(membre);
            abonnement.setGym(famille.getGym());
            abonnement.setType(TypeAbonnement.FAMILIALE);
            abonnement.setPrixAbonnement(montantFinal);
            abonnement.setDateDebutAbonnement(LocalDate.now());
            abonnement.setDateFinAbonnement(LocalDate.now().plusMonths(dto.getNombreMois()));
            abonnement.setDateRappelFinAbonnement(abonnement.getDateFinAbonnement().minusDays(5));
            abonnement.setNombreDeMois(BigInteger.valueOf(dto.getNombreMois()));
            abonnement.setModeDePaiement(dto.getModeDePaiement());
            abonnement.setEnregistrerPar(utilisateurActuellementConnecter.getUtilisateurActuellementConnecter());
            abonnement.setStatut(StatutAbonnement.EN_COURS);

            abonnementRepository.save(abonnement);
            abonnements.add(abonnement);
            total = total.add(montantFinal.add(membre.getFraisInscription() != null ? membre.getFraisInscription() : BigDecimal.ZERO));
            membre.setFraisInscriptionPayer(true);
            userRepository.save(membre);
        }
        factureCollectiveService.creeFactureCollective(famille, abonnements, total);
    }

    //  2.  Ajouter un membre a famille deja existante par le chef de famille
    public ConfirmationToastDTO ajoutParChefDeFamille(@Valid MembreDTO dto) throws MessagingException{
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!currentUser.getId().equals(dto.getChefFamilleId())){
            throw new RuntimeException("Seul le chef de famille peut proposer un ajout");
        }

        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(()-> new RuntimeException("Chef de famille introuvable"));
        Famille famille = familleRepository.findByChefFamille(chefFamille);
        User membre = construireMembre(dto, famille, chefFamille);
        membre.setStatut(StatutMembre.EN_ATTENTE_VALIDATION);
        userRepository.save(membre);

        // Notification au chef de famille
        notificationService.notification(
                chefFamille,
                "Demande d'ajout de membre",
                "Demamde d'ajout de " + membre.getPrenom() + " à la famille. En attente de validation",
                "Ajout familial",
                TypeNotification.ABONNEMENT,
                true
        );

        // Notification au staff
        List<User> staff = userRepository.findByRoleIn(List.of(Role.ADMIN, Role.GERANT, Role.RECEPTIONNISTE));
        staff.forEach(s -> {
            try {
                notificationService.notification(s, "Nouvelle demande",
                        "Validation requise pour " + membre.getNom() + " " + membre.getPrenom(),
                        "Ajout famille", TypeNotification.ABONNEMENT, true);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });

        BigDecimal montantCalcule = calculerMontantRestant(null, membre.getGenre(), dto.getFraisInscriptionMembre());

        ConfirmationToastDTO toast = new ConfirmationToastDTO();

        toast.setNomComplet(membre.getNom() + " " + membre.getPrenom());
        toast.setFraisInscription(dto.getFraisInscriptionMembre());
        toast.setMontantCalcule(montantCalcule);
        toast.setMessage("Demande d'ajout envoyée. En attente de validation.");
        return toast;
    }

    //  3.  Ajouter un membre a famille deja existante par le Staff
    public ConfirmationToastDTO ajoutDirectParStaff(@Valid MembreDTO dto) throws MessagingException {
        User current = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!(current.getRole() == Role.ADMIN || current.getRole() == Role.GERANT || current.getRole() == Role.RECEPTIONNISTE)) {
            throw new RuntimeException("Seul un staff peut ajouter un membre directement");
        }

        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(() -> new RuntimeException("Chef de la famille introuvable"));
        Famille famille = familleRepository.findByChefFamille(chefFamille);

        User membre = (dto.getMembreId() != null) ? userRepository.findById(dto.getMembreId())
                .orElseThrow(() -> new RuntimeException("Membre introuvable"))
                : construireMembre(dto, famille, chefFamille);

        // Vérifier s'il existe un abonnement en cours pour ce membre
        List<Abonnement> abonnementsEnCours = abonnementRepository.findByMembreAndStatut(membre, StatutAbonnement.EN_COURS);
        if (!abonnementsEnCours.isEmpty()) {
            Abonnement abonnementEnCours = abonnementsEnCours.get(0); // Prendre le premier abonnement en cours
            ConfirmationToastDTO toast = new ConfirmationToastDTO();
            toast.setNomComplet(membre.getNom() + " " + membre.getPrenom());
            toast.setMessage("Ce membre a déjà un abonnement en cours jusqu'au " + abonnementEnCours.getDateFinAbonnement()
                    + ". Veuillez attendre la fin de cet abonnement.");
            return toast;
        }

        String mdp = userService.genererMotDePasse(membre);
        membre.setPassword(passwordEncoder.encode(mdp));
        userRepository.save(membre);

        Abonnement abonnementFamilial = abonnementRepository.findByFamilleAndType(famille, TypeAbonnement.FAMILIALE)
                .stream()
                .filter(a -> a.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abonnement familial actif introuvable"));

        BigDecimal montantCalcule = calculerMontantRestant(abonnementFamilial, membre.getGenre(), dto.getFraisInscriptionMembre());

        // Créer un nouvel abonnement pour le membre
        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(membre);
        abonnement.setGym(famille.getGym());
        abonnement.setType(TypeAbonnement.FAMILIALE); // Type familial pour l'abonnement individuel
        abonnement.setPrixAbonnement(montantCalcule);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setDateFinAbonnement(abonnementFamilial.getDateFinAbonnement());
        abonnement.setDateRappelFinAbonnement(abonnementFamilial.getDateRappelFinAbonnement());
        abonnement.setNombreDeMois(BigInteger.valueOf(ChronoUnit.MONTHS.between(LocalDate.now(), abonnementFamilial.getDateFinAbonnement())));
        abonnement.setModeDePaiement(abonnementFamilial.getModeDePaiement());
        abonnement.setEnregistrerPar(current);
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnementRepository.save(abonnement);

        membre.setFraisInscriptionPayer(true);
        userRepository.save(membre);

        factureCollectiveService.creeFactureCollective(famille, List.of(abonnement), montantCalcule.add(dto.getFraisInscriptionMembre()));
        emailService.envoyerEmailBienvenu(membre, mdp);
        emailService.envoyerEmail(chefFamille.getEmail(), "Nouveau membre ajouté", membre.getNom() + " " + membre.getPrenom() + " a été ajouté à votre famille.");

        ConfirmationToastDTO toast = new ConfirmationToastDTO();
        toast.setNomComplet(membre.getNom() + " " + membre.getPrenom());
        toast.setFraisInscription(dto.getFraisInscriptionMembre());
        toast.setMontantCalcule(montantCalcule);
        toast.setMessage("Ajout effectué avec succès.");
        return toast;
    }
    private User construireMembre(MembreDTO dto, Famille famille, User chefFamille) {
        User membre = new User();
        membre.setNom(dto.getNomMembre());
        membre.setPrenom(dto.getPrenomMembre());
        membre.setEmail(dto.getEmailMembre());
        membre.setTelephone(dto.getNumeroTelephoneMembre());
        membre.setGenre(dto.getGenreMembre());
        membre.setAdresse(dto.getAdresseMembre());
        membre.setDate_de_naissance(dto.getGetDate_de_naissanceMembre());
        membre.setFraisInscription(dto.getFraisInscriptionMembre());
        membre.setFraisInscriptionPayer(false);
        membre.setRole(Role.MEMBRE);
        membre.setGym(famille.getGym());
        membre.setTelephoneReference(chefFamille.getTelephone());
        membre.setFamille(famille);
        membre.setDate_creation(LocalDateTime.now());
        return membre;
    }

    //  4.  Demande le retrait d'un membre
    public ConfirmationToastDTO demenderRetraitMembre(Long membreId) throws MessagingException, AccessDeniedException {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));
        User chefFamille = userRepository.findByTelephone(membre.getTelephoneReference())
                .orElseThrow(() -> new RuntimeException("Chef de famille introuvable"));
        boolean isChefFamille = currentUser.getId().equals(chefFamille.getId());
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;

        if (!isChefFamille && !isStaff) {
            throw new AccessDeniedException("Seul le chef de famille ou un staff autorisé peut demander un retrait.");
        }

        Abonnement abonnementFamilial = abonnementRepository.findByFamilleAndType(membre.getFamille(), TypeAbonnement.FAMILIALE)
                .stream()
                .filter(a -> a.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abonnement familial en cours introuvable."));

        ConfirmationToastDTO toast = new ConfirmationToastDTO();
        toast.setNomComplet(membre.getNom() + " " + membre.getPrenom());
        toast.setMessage("Voulez-vous retirer " + toast.getNomComplet() + " de la famille ? Le retrait sera effectif à la fin de l'abonnement.");

        // Définir la date de retrait
        membre.setDateRetrait(abonnementFamilial.getDateFinAbonnement());
        userRepository.save(membre);

        // Envoyer une notification pour validation par le staff
        notificationService.notification(
                chefFamille,
                "Demande de retrait de membre",
                "Demande de retrait de " + toast.getNomComplet() + " de la famille. En attente de validation par le staff.",
                "Retrait famille",
                TypeNotification.ABONNEMENT,
                true
        );

        return toast;
    }

    //  5.  Confirme le retrait d’un membre
    public void confirmerRetraitMembre(Long membreId) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;

        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut valider le retrait.");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));
        User chefFamille = userRepository.findByTelephone(membre.getTelephoneReference())
                .orElseThrow(() -> new RuntimeException("Chef de famille introuvable"));

        emailService.envoyerEmail(
                membre.getEmail(),
                "Retrait de la famille",
                "Vous serez retiré de la famille à la fin de l'abonnement."
        );
        emailService.envoyerEmail(
                chefFamille.getEmail(),
                "Retrait de membre",
                membre.getNom() + " " + membre.getPrenom() + " sera retiré de votre famille à la fin de l'abonnement."
        );
    }

    //  6.  Annule le retrait d’un membre
    public void annulerRetraitMembre(Long membreId) throws MessagingException{

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() ==Role.GERANT || currentUser.getRole() == Role.RECEPTIONNISTE;
        if (!isStaff){
            throw new RuntimeException("Seul le staff autorisé peut annuler un retrait. ");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(()-> new RuntimeException("Membre introuvable."));
        User chefFamille = userRepository.findByTelephone(membre.getTelephoneReference())
                .orElseThrow(()-> new RuntimeException("Chef de famille introuvable."));
        if (membre.getDateRetrait() == null) {
            throw new IllegalStateException("Aucun retrait n'est planifié pour ce membre.");
        }

        membre.setDateRetrait(null);
        userRepository.save(membre);

        emailService.envoyerEmail(
                membre.getEmail(),
                "Annulation du retrait",
                "Votre retrait de la famille a été annulé. Vous restez membre de la famille."
        );
        emailService.envoyerEmail(
                chefFamille.getEmail(),
                "Annulation du retrait de membre",
                membre.getNom() + " " + membre.getPrenom() + " reste membre de votre famille."
        );
    }

    //  7.  Confirme l'ajout d’un membre
    public void confirmationAjoutMembre(Long membreId) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;

        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut valider l'ajout.");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));
        Famille famille = membre.getFamille();
        Abonnement abonnementFamilial = abonnementRepository.findByFamilleAndType(famille, TypeAbonnement.FAMILIALE)
                .stream()
                .filter(a -> a.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abonnement familial en cours introuvable."));

        BigDecimal montantCalcule = calculerMontantRestant(abonnementFamilial, membre.getGenre(), membre.getFraisInscription());

        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(membre);
        abonnement.setGym(famille.getGym());
        abonnement.setType(TypeAbonnement.FAMILIALE);
        abonnement.setPrixAbonnement(montantCalcule);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setDateFinAbonnement(abonnementFamilial.getDateFinAbonnement());
        abonnement.setDateRappelFinAbonnement(abonnementFamilial.getDateRappelFinAbonnement());
        abonnement.setNombreDeMois(BigInteger.valueOf(ChronoUnit.MONTHS.between(LocalDate.now(), abonnementFamilial.getDateFinAbonnement())));
        abonnement.setModeDePaiement(abonnementFamilial.getModeDePaiement());
        abonnement.setEnregistrerPar(currentUser);
        abonnement.setStatut(StatutAbonnement.EN_COURS);
        abonnementRepository.save(abonnement);

        membre.setFraisInscriptionPayer(true);
        userRepository.save(membre);
        factureCollectiveService.creeFactureCollective(famille, List.of(abonnement), montantCalcule.add(membre.getFraisInscription()));
        emailService.envoyerEmailBienvenu(membre, userService.genererMotDePasse(membre));
    }

    public void annulationAjoutMembre(Long membreId) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;

        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut annuler un ajout.");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        // Vérifier que le membre est en attente de validation
        if (membre.getStatut() != StatutMembre.EN_ATTENTE_VALIDATION) {
            throw new IllegalStateException("Seul un membre en attente de validation peut être annulé.");
        }

        // Récupérer le chef de famille via le téléphone de référence
        User chefFamille = userRepository.findByTelephone(membre.getTelephoneReference())
                .orElseThrow(() -> new RuntimeException("Chef de famille introuvable"));

        // Supprimer le membre (ou marquer comme annulé selon votre préférence)
        membre.setStatut(StatutMembre.REJETE); // Option 2 : Changer le statut (nécessite un nouveau statut)
        userRepository.save(membre);

        // Envoyer une notification au chef de famille
        notificationService.notification(
                chefFamille,
                "Annulation de la demande d'ajout",
                "La demande d'ajout de " + membre.getNom() + " " + membre.getPrenom() + " a été annulée.",
                "Annulation ajout", TypeNotification.ABONNEMENT, true
        );

        // Envoyer une notification au staff (optionnel)
        List<User> staff = userRepository.findByRoleIn(List.of(Role.ADMIN, Role.GERANT, Role.RECEPTIONNISTE));
        staff.forEach(s -> {
            try {
                notificationService.notification(
                        s,
                        "Annulation de demande",
                        "La demande d'ajout de " + membre.getNom() + " " + membre.getPrenom() + " a été annulée par " + currentUser.getNom(),
                        "Annulation ajout", TypeNotification.ABONNEMENT, true
                );
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });

        // Envoyer un email au chef de famille
        emailService.envoyerEmail(
                chefFamille.getEmail(),
                "Annulation de la demande d'ajout",
                "La demande d'ajout de " + membre.getNom() + " " + membre.getPrenom() + " à votre famille a été annulée."
        );
    }

    //  8.  Montant restant
    private BigDecimal calculerMontantRestant(Abonnement abonnement, Genre genre, BigDecimal fraisInscription) {
        LocalDate aujourdHui = LocalDate.now();
        long joursRestants = ChronoUnit.DAYS.between(aujourdHui, abonnement.getDateFinAbonnement());
        long dureeTotale = ChronoUnit.DAYS.between(abonnement.getDateDebutAbonnement(), abonnement.getDateFinAbonnement());
        BigDecimal tarifJournalier = genre.name().equals("FEMME") ? new BigDecimal("25000") : new BigDecimal("30000");
        tarifJournalier = tarifJournalier.divide(BigDecimal.valueOf(dureeTotale), 2, RoundingMode.HALF_UP);
        BigDecimal reduction = BigDecimal.valueOf(0.1); // 10% pour familial
        BigDecimal montant = BigDecimal.valueOf(joursRestants).multiply(tarifJournalier).multiply(BigDecimal.ONE.subtract(reduction));
        return montant.add(fraisInscription != null ? fraisInscription : BigDecimal.ZERO);
    }
}
