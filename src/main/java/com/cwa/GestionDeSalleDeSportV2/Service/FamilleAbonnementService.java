package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleAbonnementDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final GymRepository gymRepository;

    public FamilleAbonnementService(
            AbonnementRepository abonnementRepository,
            FamilleRepository familleRepository,
            FactureCollectiveService factureCollectiveService,
            EmailService emailService,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            UserService userService,
            UtilisateurActuellementConnecter utilisateurActuellementConnecter, GymRepository gymRepository) {
        this.abonnementRepository = abonnementRepository;
        this.familleRepository = familleRepository;
        this.factureCollectiveService = factureCollectiveService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userService = userService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.gymRepository = gymRepository;
    }

    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }

    //  1.  Crée un abonnement familial pour une famille
    public void creerAbonnementFamilial(FamilleAbonnementDTO dto) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new RuntimeException("Seul les membres du staff peuvent créer un abonnement familial");
        }

        Famille famille = familleRepository.findById(dto.getFamilleId())
                .orElseThrow(() -> new RuntimeException("Famille non trouvée"));

        // Vérification que le staff est autorisé à gérer ce gym
        if (!currentUser.getGyms().contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        List<User> membres = famille.getMembres();
        BigDecimal total = BigDecimal.ZERO;
        List<Abonnement> abonnements = new ArrayList<>();

        for (User membre : membres) {
            BigDecimal base = membre.getGenre().name().equals("FEMME") ? dto.getTarifFemme() : dto.getTarifHomme();
            BigDecimal montantFinal = base.subtract(dto.getReductionParPersonne());

            Abonnement abonnement = new Abonnement();
            abonnement.setFamille(famille);
            abonnement.setMembre(membre);
            abonnement.setGym(famille.getGym());
            abonnement.setTypes(TypeAbonnements.FAMILIALE);
            abonnement.setPrixAbonnement(montantFinal);
            abonnement.setDateDebutAbonnement(LocalDate.now());
            abonnement.setPeriodAbonnement(dto.getPeriodAbonnement());
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

    //  2.  Ajouter un membre à une famille déjà existante par le chef de famille
    public void ajoutParChefDeFamille(@Valid MembreDTO dto) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        // Vérifie si l'utilisateur est chef d'une famille
        Famille famille = familleRepository.findByChefFamille(currentUser);
        if (famille == null) {
            throw new RuntimeException("Seul le chef de famille peut proposer un ajout");
        }

        // Vérifie l'affiliation au gym
        if (!currentUser.getGyms().contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas affilié à ce gym.");
        }

        // Création du membre
        User membre = construireMembre(dto, famille, currentUser);
        membre.setStatut(StatutMembre.EN_ATTENTE_VALIDATION);
        userRepository.save(membre);

        // Notification au chef de famille (lui-même)
        notificationService.notification(
                currentUser,
                "Demande d'ajout de membre",
                "Demande d'ajout de " + membre.getPrenom() + " à la famille. En attente de validation",
                "Ajout familial",
                TypeNotification.ABONNEMENT,
                true
        );

        // Notification aux staff
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
    }

    //  2.1.  Liste des demandes d'ajout de chef de famille
    public List<User> listeDemandesAjoutParChefDeFamille() throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        // Vérifie que l'utilisateur est un staff
        if (!(currentUser.getRole() == Role.ADMIN ||
                currentUser.getRole() == Role.GERANT ||
                currentUser.getRole() == Role.RECEPTIONNISTE)) {
            throw new AccessDeniedException("Accès refusé : seuls les membres du staff peuvent consulter cette liste.");
        }

        // Récupère tous les membres avec le statut EN_ATTENTE_VALIDATION
        return userRepository.findByStatutAndRole(StatutMembre.EN_ATTENTE_VALIDATION, Role.MEMBRE);
    }




    //  3.  Ajouter un membre à une famille déjà existante par le staff
    public void ajoutDirectParStaff(@Valid MembreDTO dto) throws MessagingException, AccessDeniedException {
        User current = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!(current.getRole() == Role.ADMIN || current.getRole() == Role.GERANT || current.getRole() == Role.RECEPTIONNISTE)) {
            throw new RuntimeException("Seul un staff peut ajouter un membre directement");
        }

        User chefFamille = userRepository.findById(dto.getChefFamilleId())
                .orElseThrow(() -> new RuntimeException("Chef de la famille introuvable"));
        Famille famille = familleRepository.findByChefFamille(chefFamille);

        if (!current.getGyms().contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        User membre = (dto.getMembreId() != null) ? userRepository.findById(dto.getMembreId())
                .orElseThrow(() -> new RuntimeException("Membre introuvable"))
                : construireMembre(dto, famille, chefFamille);

        // Initialiser les champs telephoneReference et familleId
        if (dto.getMembreId() != null) {
            membre.setFamille(famille);
            membre.setTelephoneReference(chefFamille.getTelephone());
            userRepository.save(membre); // Sauvegarde explicite pour les membres existants
        }

        if (membre.getFamille() != null && !membre.getFamille().getId().equals(famille.getId())) {
            throw new RuntimeException("Ce membre est déjà associé à une autre famille.");
        }

        // Vérifier s'il existe un abonnement en cours pour ce membre
        List<Abonnement> abonnementsEnCours = abonnementRepository.findByMembreAndStatut(membre, StatutAbonnement.EN_COURS);
        if (!abonnementsEnCours.isEmpty()) {
            throw new RuntimeException("Ce membre a déjà un abonnement en cours. Veuillez attendre la fin de cet abonnement.");
        }

        String mdp = userService.genererMotDePasse(membre);
        membre.setPassword(passwordEncoder.encode(mdp));
        userRepository.save(membre);

        Abonnement abonnementFamilial = abonnementRepository.findByFamilleAndTypes(famille, TypeAbonnements.FAMILIALE)
                .stream()
                .filter(a -> a.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abonnement familial actif introuvable"));

        BigDecimal frais = dto.getFraisInscriptionMembre() != null ? dto.getFraisInscriptionMembre() : BigDecimal.ZERO;
        BigDecimal montantCalcule = calculerMontantRestant(abonnementFamilial, membre.getGenre(), frais);

        // Créer un nouvel abonnement pour le membre
        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(membre);
        abonnement.setGym(famille.getGym());
        abonnement.setTypes(TypeAbonnements.FAMILIALE); // Type familial pour l'abonnement individuel
        abonnement.setPrixAbonnement(montantCalcule);
        abonnement.setDateDebutAbonnement(LocalDate.now());
        abonnement.setPeriodAbonnement(abonnementFamilial.getPeriodAbonnement());
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
        if (dto.getGymId() != null) {
            membre.setGym(userService.findGymById(dto.getGymId()));
        }
        if (dto.getGymsIds() != null) {
            membre.setGyms(userService.findGymsByIds(dto.getGymsIds()));
        }
        membre.setTelephoneReference(chefFamille.getTelephone());
        membre.setFamille(famille);
        famille.getMembres().add(membre);
        membre.setDate_creation(LocalDateTime.now());

        // 🔐 Ajout du mot de passe
        if (dto.getPasswordMembre() == null || dto.getPasswordMembre().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est requis pour créer un membre");
        }

        // Si tu utilises un encodeur
        membre.setPassword(passwordEncoder.encode(dto.getPasswordMembre()));

        return membre;
    }

    //  4.  Retrait d'un membre
    public void retraitMembre(Long membreId) throws MessagingException, AccessDeniedException {
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
        if (!currentUser.getGyms().contains(membre.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        Abonnement abonnementFamilial = abonnementRepository.findByFamilleAndTypes(membre.getFamille(), TypeAbonnements.FAMILIALE)
                .stream()
                .filter(a -> a.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abonnement familial en cours introuvable."));

        // Définir la date de retrait
        membre.setDateRetrait(abonnementFamilial.getDateFinAbonnement());
        userRepository.save(membre);

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

    //  4.1  Liste de demande de retrait
    public List<Famille> listeDesRetrait() throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;

        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut valider le retrait.");
        }
        if (currentUser.getGyms() == null || currentUser.getGyms().isEmpty()) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer un gym.");
        }

        return familleRepository.findAll().stream()
                .filter(famille -> currentUser.getGyms().contains(famille.getGym()))
                .filter(famille -> famille.getMembres().stream().anyMatch(m->m.getDateRetrait() != null))
                .toList();
    }


    //  6.  Annule le retrait d’un membre
    public void annulerRetraitMembre(Long membreId) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.GERANT || currentUser.getRole() == Role.RECEPTIONNISTE;
        if (!isStaff) {
            throw new RuntimeException("Seul le staff autorisé peut annuler un retrait.");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable."));
        User chefFamille = userRepository.findByTelephone(membre.getTelephoneReference())
                .orElseThrow(() -> new RuntimeException("Chef de famille introuvable."));
        if (!currentUser.getGyms().contains(membre.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

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
        if (!currentUser.getGyms().contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }
        Abonnement abonnementFamilial = abonnementRepository.findByFamilleAndTypes(famille, TypeAbonnements.FAMILIALE)
                .stream()
                .filter(a -> a.getStatut() == StatutAbonnement.EN_COURS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abonnement familial en cours introuvable."));

        BigDecimal montantCalcule = calculerMontantRestant(abonnementFamilial, membre.getGenre(), membre.getFraisInscription());

        Abonnement abonnement = new Abonnement();
        abonnement.setFamille(famille);
        abonnement.setMembre(membre);
        abonnement.setGym(famille.getGym());
        abonnement.setTypes(TypeAbonnements.FAMILIALE);
        abonnement.setPrixAbonnement(montantCalcule);
        abonnement.setPeriodAbonnement(abonnementFamilial.getPeriodAbonnement());
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

    //  8.  Annuler demande d'ajout
    public void annulationAjoutMembre(Long membreId) throws MessagingException, AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONNISTE || currentUser.getRole() == Role.GERANT;

        if (!isStaff) {
            throw new AccessDeniedException("Seul un staff autorisé peut annuler un ajout.");
        }

        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        if (!currentUser.getGyms().contains(membre.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

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

    //  9.  Montant restant
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