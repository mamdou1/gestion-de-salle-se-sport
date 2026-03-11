package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Genre;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.NoSuchElementException;
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
    private final Path fileStorageLocation;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService,
                       UtilisateurActuellementConnecter utilisateurActuellementConnecter, FamilleRepository familleRepository,
                       GymRepository gymRepository, TypeDeServiceRepository typeDeServiceRepository,
                       NotificationService notificationService, ListePaimentRepository listePaimentRepository,
                       @Value("${file.upload-dir:uploads/}") String uploadDir) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.familleRepository = familleRepository;
        this.gymRepository = gymRepository;
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.notificationService = notificationService;
        this.listePaimentRepository = listePaimentRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create file storage directory: " + this.fileStorageLocation, e);
        }
    }

    // Method to store file and return relative path
    private String storeFile(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }
        // Valider le type de fichier (seulement JPEG ou PNG)
        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException("Seuls les fichiers JPEG ou PNG sont autorisés.");
        }
        // Valider la taille du fichier (par exemple, max 10 Mo)
        long maxFileSize = 10 * 1024 * 1024; // 10 Mo
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Le fichier ne doit pas dépasser 10 Mo.");
        }
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve("users").resolve(userId.toString()).resolve(fileName);
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation);
        return "/uploads/users/" + userId + "/" + fileName;
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

        String mdp = genererMotDePasse(staff);
        staff.setPassword(passwordEncoder.encode(mdp));

        // Sauvegarder l'utilisateur pour obtenir un ID avant de stocker l'image
        User savedStaff = userRepository.save(staff);

        if (file != null && !file.isEmpty()) {
            String imageUrl = storeFile(file, savedStaff.getId());
            savedStaff.setImageUrl(imageUrl);
            userRepository.save(savedStaff);
        }

        emailService.envoyerEmailBienvenu(savedStaff, mdp);
        return "Staff ajouté avec succès";
    }

    public byte[] getPhotoProduitStaff(Long id) {
        throw new UnsupportedOperationException("Photo storage in database is disabled. Use imageUrl instead.");
    }

    // 4. Ajout d'un membre par le staff autorisé
    public Optional<User> ajouterMembre(MembreDTO dto, User staff, MultipartFile file) throws MessagingException, IOException {
        if (!peutGererMembre(staff)) {
            throw new AccessDeniedException("Seul le staff autorisé peut ajouter un membre.");
        }

        // Log des données d'entrée (sans données sensibles)
        logger.info("Tentative d'ajout de membre par staff {} (gym {}): téléphone={}, email={}",
                staff.getId(), staff.getGym().getId(), dto.getNumeroTelephoneMembre(), dto.getEmailMembre());

        // Vérification de l'email pour les nouveaux membres (si obligatoire)
        if (dto.getEmailMembre() == null || dto.getEmailMembre().trim().isEmpty()) {
            throw new IllegalArgumentException("L'adresse email est obligatoire pour l'ajout d'un membre.");
        }

        // RECHERCHE DES MEMBRES EXISTANTS (maintenant retourne une liste)
        List<User> existingUsers = userRepository.findByTelephoneOrEmail(dto.getNumeroTelephoneMembre(), dto.getEmailMembre());

        if (!existingUsers.isEmpty()) {
            // Si plusieurs membres existent avec les mêmes coordonnées → conflit de données (anormal)
            if (existingUsers.size() > 1) {
                logger.error("Conflit : plusieurs membres trouvés avec téléphone {} ou email {}. Nombre: {}",
                        dto.getNumeroTelephoneMembre(), dto.getEmailMembre(), existingUsers.size());
                throw new RuntimeException("Plusieurs membres avec ce téléphone ou cet email existent. Veuillez contacter l'administrateur.");
            }

            // Un seul membre existant : on l'associe au gym courant s'il n'y est pas déjà
            User membreExistant = existingUsers.get(0);
            logger.info("Membre existant trouvé (id={}), mise à jour des gyms uniquement. Aucun email de bienvenue envoyé.", membreExistant.getId());

            if (!membreExistant.getGyms().contains(staff.getGym())) {
                membreExistant.getGyms().add(staff.getGym());
            }
            if (dto.getGymsIds() != null) {
                dto.getGymsIds().forEach(gymId -> {
                    Gym gym = gymRepository.findById(gymId)
                            .orElseThrow(() -> new NoSuchElementException("Gym introuvable"));
                    if (!membreExistant.getGyms().contains(gym)) {
                        membreExistant.getGyms().add(gym);
                    }
                });
            }
            userRepository.save(membreExistant);
            return Optional.of(membreExistant);
        }

        // Validation du type de service
        TypeDeService typeDeService = typeDeServiceRepository.findById(dto.getTypeDeService())
                .orElseThrow(() -> new NoSuchElementException("Type de service introuvable."));
        if (!typeDeService.getGym().equals(staff.getGym())) {
            throw new RuntimeException("Ce type de service ne fait pas partie de ce gym.");
        }

        BigDecimal tarifUnique = typeDeService.getTarifUnique();
        if (tarifUnique == null) {
            throw new RuntimeException("Le type de service n'a pas de tarif unique défini.");
        }

        logger.info("Tarif unique récupéré: {} pour le service: {}", tarifUnique, typeDeService.getNom());

        // Création du nouveau membre
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
                        .orElseThrow(() -> new NoSuchElementException("Gym non trouvé : " + gymId));
                nouveauMembre.addGym(gym);
            });
        }

        nouveauMembre.setDate_creation(LocalDateTime.now());
        nouveauMembre.setFraisInscription(tarifUnique);
        nouveauMembre.setFraisInscriptionPayer(true);
        nouveauMembre.setDate_de_naissance(dto.getDate_de_naissanceMembre());

        String mdp = genererMotDePasse(nouveauMembre);
        if (mdp == null || mdp.isEmpty()) {
            logger.error("Le mot de passe généré est vide pour le membre {}", dto.getEmailMembre());
            throw new RuntimeException("Erreur lors de la génération du mot de passe.");
        }
        nouveauMembre.setPassword(passwordEncoder.encode(mdp));

        User savedMembre = userRepository.save(nouveauMembre);
        logger.info("Nouveau membre enregistré avec id: {}, email: {}", savedMembre.getId(), savedMembre.getEmail());

        // Gestion de l'image
        if (file != null && !file.isEmpty()) {
            String imageUrl = storeFile(file, savedMembre.getId());
            savedMembre.setImageUrl(imageUrl);
            userRepository.save(savedMembre);
            logger.debug("Image stockée pour le membre {}: {}", savedMembre.getId(), imageUrl);
        }

        // Enregistrement du paiement
        if (nouveauMembre.getFraisInscriptionPayer() && nouveauMembre.getFraisInscription() != null) {
            ListePaiment paiement = new ListePaiment();
            paiement.setTypePaiement(TypePaiement.FRAIS_INSCRIPTION);
            paiement.setDatePaiement(LocalDateTime.now());
            paiement.setMontant(tarifUnique);
            paiement.setModeDePaiement(dto.getModeDePaiement());
            paiement.setReferenceId(savedMembre.getId());
            paiement.setAcheteur(savedMembre);
            paiement.setStaffEnregistreur(staff);
            paiement.setGym(staff.getGym());
            paiement.setDetails("Frais d'inscription pour " + savedMembre.getNom() + " " + savedMembre.getPrenom() + " (" + typeDeService.getNom() + " - Tarif: " + tarifUnique + ")");

            listePaimentRepository.save(paiement);
            logger.info("Paiement enregistré: {} pour l'inscription de {}", paiement.getMontant(), savedMembre.getEmail());
        }

        // Envoi de l'email de bienvenue avec gestion des exceptions et logs
        try {
            logger.info("Tentative d'envoi d'email de bienvenue à {}", savedMembre.getEmail());
            emailService.envoyerEmailBienvenu(savedMembre, mdp);
            logger.info("Email de bienvenue envoyé avec succès à {}", savedMembre.getEmail());
        } catch (MessagingException e) {
            logger.error("Échec de l'envoi de l'email de bienvenue à {} : {}", savedMembre.getEmail(), e.getMessage(), e);
            throw e;  // exception déclarée dans la signature
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email à {} : {}", savedMembre.getEmail(), e.getMessage(), e);
            // On wrappe pour ne pas masquer l'erreur et rester compatible avec la signature
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }

        // Notification
        try {
            logger.info("Appel à notificationService avec type: {}", TypeNotification.INSCRIPTION);
            notificationService.notifyGymAndMember(
                    staff.getGym(),
                    nouveauMembre,
                    "Ajout de membre",
                    "Vous avez été ajouté avec succès, suite à votre inscription physique à la salle de sport " + staff.getGym().getNom(),
                    "Ajout",
                    TypeNotification.INSCRIPTION,
                    false
            );
            logger.info("Notification envoyée pour le membre {}", savedMembre.getEmail());
        } catch (Exception e) {
            logger.error("Échec de l'envoi de la notification pour {} : {}", savedMembre.getEmail(), e.getMessage(), e);
            // On ne bloque pas le processus pour une notification
        }

        return Optional.of(savedMembre);
    }

    public byte[] getPhotoProduitMembre(Long id) {
        throw new UnsupportedOperationException("Photo storage in database is disabled. Use imageUrl instead.");
    }

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
            // Supprimer l'ancienne image si elle existe
            if (membre.getImageUrl() != null) {
                Path oldImagePath = fileStorageLocation.resolve(membre.getImageUrl().replace("/uploads/", "")).normalize();
                Files.deleteIfExists(oldImagePath);
            }
            String imageUrl = storeFile(file, id);
            membre.setImageUrl(imageUrl);
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
            // Supprimer l'ancienne image si elle existe
            if (staff.getImageUrl() != null) {
                Path oldImagePath = fileStorageLocation.resolve(staff.getImageUrl().replace("/uploads/", "")).normalize();
                Files.deleteIfExists(oldImagePath);
            }
            String imageUrl = storeFile(file, id);
            staff.setImageUrl(imageUrl);
        }

        userRepository.save(staff);
        return "Modification effectuée !";
    }

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

        // Supprimer l'image associée si elle existe
        if (membre.getImageUrl() != null) {
            try {
                Path oldImagePath = fileStorageLocation.resolve(membre.getImageUrl().replace("/uploads/", "")).normalize();
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                logger.warn("Impossible de supprimer l'image pour l'utilisateur {}: {}", id, e.getMessage());
            }
        }

        // Dissocier les relations pour éviter les contraintes
        membre.setTypeDeService(null);
        membre.getGyms().clear();
        userRepository.save(membre);

        logger.debug("Suppression de l'entité User: {}", membre);
        userRepository.delete(membre);
        logger.info("Membre ID {} supprimé avec succès par l'utilisateur: {}", id, currentUser.getUsername());
    }

    @Transactional
    public void supprimerStaff(Long id, User currentUser) throws AccessDeniedException {
        logger.info("Tentative de suppression du staff ID: {} par l'utilisateur: {}", id, currentUser.getUsername());

        if (currentUser.getRole() != Role.ADMIN) {
            logger.warn("Accès refusé pour la suppression du staff ID: {} par l'utilisateur: {}", id, currentUser.getUsername());
            throw new AccessDeniedException("Seul un admin peut supprimer un membre du staff.");
        }

        User staff = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Staff non trouvé pour l'ID: {}", id);
                    return new IllegalArgumentException("Staff non trouvé avec l'ID: " + id);
                });

        if (staff.getRole() == Role.MEMBRE || staff.getRole() == Role.MEMBRE_TEMPORAIRE) {
            logger.warn("Tentative de suppression d'un membre via l'endpoint staff (ID: {}, Role: {})", id, staff.getRole());
            throw new IllegalArgumentException("Seuls les membres du staff peuvent être supprimés via cet endpoint");
        }

        if (currentUser.getGym() != null && staff.getGym() != null && !currentUser.getGym().equals(staff.getGym())) {
            logger.warn("Accès refusé : le staff ID {} n'appartient pas au gym de l'utilisateur: {}", id, currentUser.getUsername());
            throw new AccessDeniedException("Accès refusé : le staff n'appartient pas à votre gym");
        }

        // Supprimer l'image associée si elle existe
        if (staff.getImageUrl() != null) {
            try {
                Path oldImagePath = fileStorageLocation.resolve(staff.getImageUrl().replace("/uploads/", "")).normalize();
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                logger.warn("Impossible de supprimer l'image pour l'utilisateur {}: {}", id, e.getMessage());
            }
        }

        // Dissocier les relations pour éviter les contraintes
        staff.setTypeDeService(null);
        staff.getGyms().clear();
        userRepository.save(staff);

        logger.debug("Suppression de l'entité Staff: {}", staff);
        userRepository.delete(staff);
        logger.info("Staff ID {} supprimé avec succès par l'utilisateur: {}", id, currentUser.getUsername());
    }

    public String modifierProfilApp(Long id, MembreDTO dto, User currentUser, MultipartFile file) throws IOException {
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
            // Supprimer l'ancienne image si elle existe
            if (membre.getImageUrl() != null) {
                Path oldImagePath = fileStorageLocation.resolve(membre.getImageUrl().replace("/uploads/", "")).normalize();
                Files.deleteIfExists(oldImagePath);
            }
            String imageUrl = storeFile(file, id);
            membre.setImageUrl(imageUrl);
        }

        userRepository.save(membre);
        return "Modification effectuée !";
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
        logger.info("🔍 DEBUG getAllMembre - Après initializeAccess");
        logger.info("🔍 DEBUG getAllMembre - CurrentUser Gym: {}", currentUser.getGym());
        logger.info("🔍 DEBUG getAllMembre - CurrentUser Gyms: {}", currentUser.getGyms());
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
        logger.info("🔍 DEBUG initializeAccess - User ID: {}", currentUser.getId());
        logger.info("🔍 DEBUG initializeAccess - User Role: {}", currentUser.getRole());
        logger.info("🔍 DEBUG initializeAccess - User Gym: {}", currentUser.getGym());
        logger.info("🔍 DEBUG initializeAccess - User Gyms list: {}", currentUser.getGyms());
        logger.info("🔍 DEBUG initializeAccess - User Gyms list size: {}",
                currentUser.getGyms() != null ? currentUser.getGyms().size() : 0);

        if (requireStaff && !peutGererMembre(currentUser)) {
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

    public void reactiverStaff(Long staffId) {
        User admin = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff introuvable."));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Seuls les admins peuvent réactiver un membre du staff.");
        }
        if (!admin.getGym().equals(staff.getGym())) {
            throw new RuntimeException("L'utilisateur n'appartient pas au même gym que le staff.");
        }

        staff.setEnabled(true);
        staff.setDateRetrait(null); // Optionnel : supprimer la date de retrait
        userRepository.save(staff);
    }

    private String getNomComplet(User user) {
        if (user == null) return "Utilisateur inconnu";
        String nom = user.getNom() != null ? user.getNom() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        String nomComplet = (nom + " " + prenom).trim();
        return nomComplet.isEmpty() ? "Sans nom" : nomComplet;
    }


    // Méthode pour récupérer le nombre total de membres
    public long getTotalMembresCount() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        return userRepository.countByRole(Role.MEMBRE);
    }

    // Méthode pour récupérer les membres récemment ajoutés
    public List<User> getRecentMembres(int days) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return userRepository.findByDateCreationAfterAndRole(sinceDate, Role.MEMBRE);
    }

    // Méthode pour rechercher des membres par nom ou prénom
    public List<User> searchMembres(String searchTerm) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        return userRepository.findByNomContainingOrPrenomContainingAndGymInAndRole(
                searchTerm, searchTerm, userGyms, Role.MEMBRE);
    }

    // Méthode pour vérifier si un email existe déjà
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Méthode pour vérifier si un téléphone existe déjà
    public boolean telephoneExists(String telephone) {
        return userRepository.existsByTelephone(telephone);
    }

    // Méthode pour récupérer les membres par genre
    public List<User> getMembresByGenre(Genre genre) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        return userRepository.findByGenreAndGymInAndRole(genre, userGyms, Role.MEMBRE);
    }

    // Méthode pour récupérer les membres par type de service
    public List<User> getMembresByTypeService(Long typeServiceId) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        TypeDeService typeDeService = typeDeServiceRepository.findById(typeServiceId)
                .orElseThrow(() -> new RuntimeException("Type de service introuvable"));
        return userRepository.findByTypeDeServiceAndGymIn(typeDeService, userGyms);
    }

    // Méthode pour récupérer les membres sans famille
    public List<User> getMembresSansFamille() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();
        return userRepository.findByFamilleIsNullAndGymInAndRole(userGyms, Role.MEMBRE);
    }

    // Méthode pour mettre à jour la photo de profil
   /* public String updateProfilePhoto(Long membreId, MultipartFile file) throws IOException {
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        // Supprimer l'ancienne image si elle existe
        if (membre.getImageUrl() != null && membre.getImageUrl().startsWith("/images/")) {
            try {
                Path oldPath = Paths.get(uploadDir, membre.getImageUrl().replace("/images/", ""));
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                logger.warn("Erreur lors de la suppression de l'ancienne image: {}", e.getMessage());
            }
        }

        // Sauvegarder la nouvelle image
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String savedFileName = stockageDeFichierService.saveFile(file, fileName);
        membre.setImageUrl("/images/" + savedFileName);

        userRepository.save(membre);
        return "Photo de profil mise à jour avec succès";
    } */

    // Méthode pour réinitialiser le mot de passe d'un membre
    public String reinitialiserMotDePasse(Long membreId) throws MessagingException {
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        String nouveauMotDePasse = genererMotDePasse(membre);
        membre.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        userRepository.save(membre);

        // Envoyer le nouveau mot de passe par email
        emailService.envoyerNouveauMotDePasse(membre, nouveauMotDePasse);

        return "Mot de passe réinitialisé avec succès";
    }

    // Méthode pour exporter la liste des membres
    public List<Map<String, Object>> exporterMembres() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<User> membres = getAllMembre();

        return membres.stream().map(membre -> {
            Map<String, Object> membreData = new HashMap<>();
            membreData.put("id", membre.getId());
            membreData.put("nom", membre.getNom());
            membreData.put("prenom", membre.getPrenom());
            membreData.put("email", membre.getEmail());
            membreData.put("telephone", membre.getTelephone());
            membreData.put("genre", membre.getGenre());
            membreData.put("dateNaissance", membre.getDate_de_naissance());
            membreData.put("adresse", membre.getAdresse());
            membreData.put("typeService", membre.getTypeDeService() != null ? membre.getTypeDeService().getNom() : "Non défini");
            membreData.put("famille", membre.getFamille() != null ? membre.getFamille().getNom() : "Aucune");
            membreData.put("dateCreation", membre.getDate_creation());
            return membreData;
        }).collect(Collectors.toList());
    }

    // Méthode pour obtenir les statistiques des membres
    public Map<String, Object> getMembresStatistics() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMembres", userRepository.countByGymInAndRole(userGyms, Role.MEMBRE));
        stats.put("hommes", userRepository.countByGenreAndGymInAndRole(Genre.HOMME, userGyms, Role.MEMBRE));
        stats.put("femmes", userRepository.countByGenreAndGymInAndRole(Genre.FEMME, userGyms, Role.MEMBRE));
        stats.put("membresSansFamille", userRepository.countByFamilleIsNullAndGymInAndRole(userGyms, Role.MEMBRE));
        stats.put("nouveauxMembresMois", userRepository.countByDateCreationAfterAndGymInAndRole(
                LocalDateTime.now().minusMonths(1), userGyms, Role.MEMBRE));

        return stats;
    }

    // Méthode pour transférer un membre vers un autre gym
    public String transfererMembre(Long membreId, Long nouveauGymId) throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        Gym nouveauGym = gymRepository.findById(nouveauGymId)
                .orElseThrow(() -> new RuntimeException("Gym introuvable"));

        // Vérifier que l'utilisateur courant a accès au nouveau gym
        if (!currentUser.getGyms().contains(nouveauGym)) {
            throw new AccessDeniedException("Vous n'avez pas accès à ce gym");
        }

        // Retirer l'ancien gym et ajouter le nouveau
        membre.getGyms().clear();
        membre.addGym(nouveauGym);
        membre.setGym(nouveauGym);

        userRepository.save(membre);
        return "Membre transféré avec succès vers " + nouveauGym.getNom();
    }

    // Méthode pour désactiver un membre
    public String desactiverMembre(Long membreId) {
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        membre.setEnabled(false);
        membre.setDateRetrait(LocalDate.now());
        userRepository.save(membre);

        return "Membre désactivé avec succès";
    }

    // Méthode pour réactiver un membre
    public String reactiverMembre(Long membreId) {
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        membre.setEnabled(true);
        membre.setDateRetrait(null);
        userRepository.save(membre);

        return "Membre réactivé avec succès";
    }
}