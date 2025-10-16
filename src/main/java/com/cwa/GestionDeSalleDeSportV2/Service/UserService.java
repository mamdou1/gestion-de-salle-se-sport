package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.TypeDeServiceRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.ListePaimentRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final FamilleRepository familleRepository;
    private final GymRepository gymRepository;
    private final TypeDeServiceRepository typeDeServiceRepository;
    private final NotificationService notificationService;
    private final ListePaimentRepository listePaimentRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService,
                       UtilisateurActuellementConnecter utilisateurActuellementConnecter, FamilleRepository familleRepository,
                       GymRepository gymRepository, TypeDeServiceRepository typeDeServiceRepository,
                       NotificationService notificationService, ListePaimentRepository listePaimentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.familleRepository = familleRepository;
        this.gymRepository = gymRepository;
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.notificationService = notificationService;
        this.listePaimentRepository = listePaimentRepository;
    }

    // 1. Vérifie si l'utilisateur peut gérer des membres
    public boolean peutGererMembre(User user) {
        return user.getRole() == Role.ADMIN ||
                user.getRole() == Role.RECEPTIONNISTE ||
                user.getRole() == Role.GERANT;
    }

    // 2. Génération du mot de passe
    public String genererMotDePasse(User user) {
        String nom = user.getNom().length() >= 2 ? user.getNom().substring(0, 2) : user.getNom();
        String prenom = user.getPrenom().length() >= 2 ? user.getPrenom().substring(0, 2) : user.getPrenom();
        String tel = user.getTelephone().replaceAll("\\D", "");
        tel = tel.length() >= 4 ? tel.substring(0, 4) : tel;
        return (nom + prenom + tel).toLowerCase();
    }

    // 3. Ajouter nouveau staff
    public String ajouterStaff(StaffDTO dto, User admin, MultipartFile file) throws IOException, MessagingException {
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Seul un admin peut ajouter des membres du staff.");
        }

        // Validation de l'âge
        if (dto.getDate_de_naissanceStaff() != null && !dto.getDate_de_naissanceStaff().trim().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(dto.getDate_de_naissanceStaff(), formatter);
            LocalDate currentDate = LocalDate.now();
            int age = Period.between(birthDate, currentDate).getYears();
            if (age < 16 || age > 80) {
                throw new RuntimeException("L'âge doit être compris entre 16 et 80 ans.");
            }
        } else {
            throw new RuntimeException("La date de naissance est requise.");
        }

        User staff = new User();
        staff.setNom(dto.getNomStaff());
        staff.setPrenom(dto.getPrenomStaff());
        staff.setAdresse(dto.getAdresseStaff());
        staff.setGenre(dto.getGenreStaff());
        staff.setRole(dto.getRoleStaff());
        staff.setEmail(dto.getEmailStaff());
        staff.setDate_creation(LocalDateTime.now());
        staff.setDate_de_naissance(dto.getDate_de_naissanceStaff());
        staff.setTelephone(dto.getNumeroTelephoneStaff());
        staff.setGym(admin.getGym());
        staff.addGym(admin.getGym());

        if (file != null && !file.isEmpty()) {
            staff.setProfil(file.getBytes());
        }

        String mdp = genererMotDePasse(staff);
        staff.setPassword(passwordEncoder.encode(mdp));

        userRepository.save(staff);
        emailService.envoyerEmailBienvenu(staff, mdp);
        return "Staff ajouté avec succès";
    }

    public byte[] getPhotoProduitStaff(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image non trouvée."));
        return user.getProfil();
    }

    // 4. Ajout d'un membre par le staff autorisé
    public Optional<User> ajouterMembre(MembreDTO dto, User staff, MultipartFile file) throws MessagingException, IOException {
        if (!peutGererMembre(staff)) {
            throw new AccessDeniedException("Seul le staff autorisé peut ajouter un membre.");
        }

        Optional<User> existingUser = userRepository.findByTelephoneOrEmail(dto.getNumeroTelephoneMembre(), dto.getEmailMembre());
        if (existingUser.isPresent()) {
            User membreExistant = existingUser.get();
            if (!membreExistant.getGyms().contains(staff.getGym())) {
                membreExistant.getGyms().add(staff.getGym());
            }
            if (dto.getGymsIds() != null) {
                dto.getGymsIds().forEach(gymId -> {
                    Gym gym = gymRepository.findById(gymId)
                            .orElseThrow(() -> new RuntimeException("Gym introuvable"));
                    if (!membreExistant.getGyms().contains(gym)) {
                        membreExistant.getGyms().add(gym);
                    }
                });
            }
            userRepository.save(membreExistant);
            return existingUser;
        }

        TypeDeService typeDeService = typeDeServiceRepository.findById(dto.getTypeDeService())
                .orElseThrow(() -> new RuntimeException("Type de service introuvable."));
        if (!typeDeService.getGym().equals(staff.getGym())) {
            throw new RuntimeException("Ce type de service ne fait pas partie de ce gym.");
        }

        User nouveauMembre = new User();
        nouveauMembre.setNom(dto.getNomMembre());
        nouveauMembre.setPrenom(dto.getPrenomMembre());
        nouveauMembre.setEmail(dto.getEmailMembre());
        nouveauMembre.setTelephone(dto.getNumeroTelephoneMembre());
        nouveauMembre.setGenre(dto.getGenreMembre());
        nouveauMembre.setAdresse(dto.getAdresseMembre());
        nouveauMembre.setRole(Role.MEMBRE);
        nouveauMembre.setGym(staff.getGym());
        nouveauMembre.addGym(staff.getGym());
        nouveauMembre.setTypeDeService(typeDeService);
        if (dto.getGymsIds() != null) {
            dto.getGymsIds().forEach(gymId -> {
                Gym gym = gymRepository.findById(gymId)
                        .orElseThrow(() -> new RuntimeException("Gym non trouvé : " + gymId));
                nouveauMembre.addGym(gym);
            });
        }

        nouveauMembre.setDate_creation(LocalDateTime.now());
        nouveauMembre.setFraisInscription(typeDeService.getFraisInscription());
        nouveauMembre.setFraisInscriptionPayer(true);
        nouveauMembre.setDate_de_naissance(dto.getDate_de_naissanceMembre());

        String mdp = genererMotDePasse(nouveauMembre);
        nouveauMembre.setPassword(passwordEncoder.encode(mdp));

        if (file != null && !file.isEmpty()) {
            nouveauMembre.setProfil(file.getBytes());
        }

        // Sauvegarde du membre
        User savedMembre = userRepository.save(nouveauMembre);

        // Création de l'entrée dans liste_paiment pour les frais d'inscription
        if (nouveauMembre.getFraisInscriptionPayer() && nouveauMembre.getFraisInscription() != null) {
            ListePaiment paiement = new ListePaiment();
            paiement.setTypePaiement(TypePaiement.FRAIS_INSCRIPTION);
            paiement.setDatePaiement(LocalDateTime.now());
            paiement.setMontant(nouveauMembre.getFraisInscription());
            paiement.setModeDePaiement(dto.getModeDePaiement()); // Assurez-vous que MembreDTO inclut modeDePaiement
            paiement.setReferenceId(savedMembre.getId());
            paiement.setAcheteur(savedMembre);
            paiement.setStaffEnregistreur(staff);
            paiement.setGym(staff.getGym());
            paiement.setDetails("Frais d'inscription pour " + savedMembre.getNom() + " " + savedMembre.getPrenom() + " (" + typeDeService.getNom() + ")");
            listePaimentRepository.save(paiement);
            logger.info("Created ListePaiment for frais d'inscription: {}", paiement);
        }

        emailService.envoyerEmailBienvenu(nouveauMembre, mdp);
        notificationService.notifyGymAndMember(
                staff.getGym(),
                nouveauMembre,
                "Ajout de membre",
                "Vous avez été ajouté avec succès, suite à votre inscription physique à la salle de sport " + staff.getGym().getNom(),
                "Ajout",
                TypeNotification.INSCRIPTION,
                false
        );

        return Optional.of(nouveauMembre);
    }

    public byte[] getPhotoProduitMembre(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé."));
        return user.getProfil();
    }

    // 5. Modification des infos du compte
    public String modifierMembre(Long id, MembreDTO dto, User currentUser, MultipartFile file) throws IOException {
        User membre = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre introuvable !"));

        boolean isSelf = currentUser.getId().equals(membre.getId());
        if (!isSelf && !peutGererMembre(currentUser)) {
            throw new AccessDeniedException("Accès refusé.");
        }
        if ((currentUser.getRole() == Role.MEMBRE || currentUser.getRole() == Role.COACH) && !isSelf) {
            throw new RuntimeException("Vous pouvez uniquement modifier votre propre profil !");
        }

        if (dto.getNomMembre() != null) membre.setNom(dto.getNomMembre());
        if (dto.getPrenomMembre() != null) membre.setPrenom(dto.getPrenomMembre());
        if (dto.getRole() != null) membre.setRole(dto.getRole());
        if (dto.getEmailMembre() != null) membre.setEmail(dto.getEmailMembre());
        if (dto.getAdresseMembre() != null) membre.setAdresse(dto.getAdresseMembre());
        if (dto.getNumeroTelephoneMembre() != null) membre.setTelephone(dto.getNumeroTelephoneMembre());
        if (dto.getDate_de_naissanceMembre() != null) membre.setDate_de_naissance(dto.getDate_de_naissanceMembre());
        if (file != null && !file.isEmpty()) {
            membre.setProfil(file.getBytes());
        }

        userRepository.save(membre);
        return "Modification effectuée !";
    }

    public String modifierStaff(Long id, StaffDTO dto, User currentUser, MultipartFile file) throws IOException {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre introuvable !"));

        boolean isSelf = currentUser.getId().equals(staff.getId());
        if (!isSelf && !peutGererMembre(currentUser)) {
            throw new AccessDeniedException("Accès refusé.");
        }
        if ((currentUser.getRole() == Role.MEMBRE || currentUser.getRole() == Role.COACH || currentUser.getRole() == Role.RECEPTIONNISTE) && !isSelf) {
            throw new RuntimeException("Vous pouvez uniquement modifier votre propre profil !");
        }

        if (dto.getNomStaff() != null) staff.setNom(dto.getNomStaff());
        if (dto.getPrenomStaff() != null) staff.setPrenom(dto.getPrenomStaff());
        if (dto.getRoleStaff() != null) staff.setRole(dto.getRoleStaff());
        if (dto.getEmailStaff() != null) staff.setEmail(dto.getEmailStaff());
        if (dto.getAdresseStaff() != null) staff.setAdresse(dto.getAdresseStaff());
        if (dto.getNumeroTelephoneStaff() != null) staff.setTelephone(dto.getNumeroTelephoneStaff());
        if (dto.getDate_de_naissanceStaff() != null) staff.setDate_de_naissance(dto.getDate_de_naissanceStaff());
        if (file != null && !file.isEmpty()) {
            staff.setProfil(file.getBytes());
        }

        userRepository.save(staff);
        return "Modification effectuée !";
    }

    // 6. Consulter un profil
    public User consulterProfil(Long id, User currentUser) throws AccessDeniedException {
        User membre = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre introuvable."));
        boolean isSelf = currentUser.getId().equals(membre.getId());
        if (!isSelf && !peutGererMembre(currentUser)) {
            throw new AccessDeniedException("Accès refusé.");
        }
        if (!isSelf && membre.getGym() != null && !currentUser.getGyms().contains(membre.getGym())) {
            throw new AccessDeniedException("Membre d'une autre salle.");
        }
        if ((currentUser.getRole() == Role.MEMBRE || currentUser.getRole() == Role.COACH) && !isSelf) {
            throw new AccessDeniedException("Vous pouvez consulter uniquement votre profil");
        }
        return membre;
    }

    // 7. Afficher tous les utilisateurs
    public List<User> getAllUser() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        if (userGyms == null || userGyms.isEmpty()) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        Gym principalGym = currentUser.getGym();
        if (principalGym != null) {
            verificationAccesGym(currentUser, principalGym, "consulter la liste des utilisateurs dans");
        }
        logger.debug("Recherche des utilisateurs pour les salles : {}", userGyms);
        List<User> membres = userRepository.findByGymIn(userGyms);
        if (membres == null) {
            logger.warn("Aucun utilisateur trouvé pour les salles de l'utilisateur.");
            return List.of();
        }
        return membres;
    }

    // 8. Supprimer un membre
    @Transactional
    public void supprimerMembre(Long id, User currentUser) throws AccessDeniedException {
        logger.info("Tentative de suppression du membre ID: {} par l'utilisateur: {}", id, currentUser.getUsername());

        if (!peutGererMembre(currentUser)) {
            logger.warn("Accès refusé pour la suppression du membre ID: {} par l'utilisateur: {}", id, currentUser.getUsername());
            throw new AccessDeniedException("Seuls les admins, réceptionnistes ou gérants peuvent supprimer des membres");
        }

        User membre = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Membre non trouvé pour l'ID: {}", id);
                    return new IllegalArgumentException("Membre non trouvé avec l'ID: " + id);
                });

        if (membre.getRole() != Role.MEMBRE && membre.getRole() != Role.MEMBRE_TEMPORAIRE) {
            logger.warn("Tentative de suppression d'un non-membre (ID: {}, Role: {})", id, membre.getRole());
            throw new IllegalArgumentException("Seuls les membres peuvent être supprimés via cet endpoint");
        }

        if (currentUser.getGym() != null && membre.getGym() != null && !currentUser.getGym().equals(membre.getGym())) {
            logger.warn("Accès refusé : le membre ID {} n'appartient pas au gym de l'utilisateur: {}", id, currentUser.getUsername());
            throw new AccessDeniedException("Accès refusé : le membre n'appartient pas à votre gym");
        }

        // Dissocier les relations pour éviter les contraintes
        membre.setTypeDeService(null);
        membre.getGyms().clear();
        userRepository.save(membre);

        logger.debug("Suppression de l'entité User: {}", membre);
        userRepository.delete(membre);
        logger.info("Membre ID {} supprimé avec succès par l'utilisateur: {}", id, currentUser.getUsername());
    }

    public List<User> getAllMembre() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        if (userGyms == null || userGyms.isEmpty()) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        Gym principalGym = currentUser.getGym();
        if (principalGym != null) {
            verificationAccesGym(currentUser, principalGym, "consulter la liste des membres dans");
        }
        logger.debug("Recherche des membres pour les salles : {}", userGyms);
        List<User> users = userRepository.findByGymIn(userGyms);
        if (users == null) {
            logger.warn("Aucun membre trouvé pour les salles de l'utilisateur.");
            return List.of();
        }
        List<User> membres = users.stream()
                .filter(user -> user.getRole() == Role.MEMBRE || user.getRole() == Role.MEMBRE_TEMPORAIRE)
                .collect(Collectors.toList());
        if (membres.isEmpty()) {
            logger.warn("Aucun membre trouvé avec le rôle MEMBRE pour les salles de l'utilisateur.");
            return List.of();
        }
        return membres;
    }

    public Page<User> getAllMembreAvecPagination(Pageable pageable) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        if (userGyms == null || userGyms.isEmpty()) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        Gym principalGym = currentUser.getGym();
        if (principalGym != null) {
            verificationAccesGym(currentUser, principalGym, "consulter la liste des membres dans");
        }
        logger.debug("Recherche des membres pour les salles : {}", userGyms);
        Page<User> usersPage = userRepository.findByGymInAndRoleIn(
                userGyms,
                List.of(Role.MEMBRE, Role.MEMBRE_TEMPORAIRE),
                pageable
        );
        if (usersPage.isEmpty()) {
            logger.warn("Aucun membre trouvé pour les salles de l'utilisateur.");
            return Page.empty();
        }
        return usersPage;
    }

    public List<User> getAllStaff() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        if (userGyms == null || userGyms.isEmpty()) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        Gym principalGym = currentUser.getGym();
        if (principalGym != null) {
            verificationAccesGym(currentUser, principalGym, "consulter la liste des membres dans");
        }
        logger.debug("Recherche des membres pour les salles : {}", userGyms);
        List<User> users = userRepository.findByGymIn(userGyms);
        if (users == null) {
            logger.warn("Aucun membre trouvé pour les salles de l'utilisateur.");
            return List.of();
        }
        List<User> membres = users.stream()
                .filter(user -> user.getRole() != Role.MEMBRE && user.getRole() != Role.MEMBRE_TEMPORAIRE)
                .collect(Collectors.toList());
        if (membres.isEmpty()) {
            logger.warn("Aucun staff trouvé pour les salles de l'utilisateur.");
            return List.of();
        }
        return membres;
    }

    public Gym findGymById(Long id) {
        return gymRepository.findById(id).orElseThrow(() -> new RuntimeException("Gym introuvable"));
    }

    public List<Gym> findGymsByIds(List<Long> ids) {
        return gymRepository.findAllById(ids);
    }

    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONNISTE && currentUser.getRole() != Role.GERANT) {
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

    public void changerMotDePasse(ChangerMotDePasseDTO dto) {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!passwordEncoder.matches(dto.getAncienMotDePasse(), currentUser.getPassword())) {
            throw new RuntimeException("L'ancien mot de passe est incorrect.");
        }
        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmerMotDePasse())) {
            throw new RuntimeException("Le nouveau mot de passe ne correspond pas à la confirmation.");
        }
        currentUser.setPassword(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        userRepository.save(currentUser);
    }

    public void retirerStaff(Long staffId) {
        User admin = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff introuvable."));
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Seuls les admins peuvent retirer un membre du système.");
        }
        if (staff.getRole() == Role.ADMIN) {
            throw new RuntimeException("Un administrateur ne peut pas être bloqué.");
        }
        if (!admin.getGym().equals(staff.getGym())) {
            throw new RuntimeException("L'utilisateur n'appartient pas au même gym que le staff.");
        }
        staff.setEnabled(false);
        staff.setDateRetrait(LocalDateTime.now().toLocalDate());
        userRepository.save(staff);
    }

    public List<Gym> getGymsOfMembre() throws AccessDeniedException {
        User currentUser = initializeAccess(false);
        Long memberId = currentUser.getId();
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable."));
        if (!currentUser.getId().equals(memberId) && !peutGererMembre(currentUser)) {
            throw new AccessDeniedException("Accès refusé aux gyms de ce membre.");
        }
        return member.getGyms();
    }

    public boolean verifierMotDePasse(String motDePasseSaisi, String motDePasseEncode) {
        return passwordEncoder.matches(motDePasseSaisi, motDePasseEncode);
    }
}