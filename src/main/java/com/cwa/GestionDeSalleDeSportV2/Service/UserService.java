package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.*;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypeNotification;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.TypeDeServiceRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(AbonnementService.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final FamilleRepository familleRepository;
    private final GymRepository gymRepository;
    private final TypeDeServiceRepository typeDeServiceRepository;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, FamilleRepository familleRepository, GymRepository gymRepository, TypeDeServiceRepository typeDeServiceRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.familleRepository = familleRepository;
        this.gymRepository = gymRepository;
        this.typeDeServiceRepository = typeDeServiceRepository;
        this.notificationService = notificationService;
    }

    //  1.  Vérifie si l'utilisateur peut gérer des membres
    public boolean peutGererMembre(User user){
        return user.getRole() == Role.ADMIN ||
                user.getRole() == Role.RECEPTIONNISTE ||
                user.getRole() == Role.GERANT;
    }

    //  2.  Generation du mot de passe
    public String genererMotDePasse(User user){

        String nom = user.getNom().length() >= 2 ? user.getNom().substring(0, 2) : user.getNom();
        String prenom = user.getPrenom().length() >= 2 ? user.getPrenom().substring(0, 2) : user.getPrenom();
        String tel = user.getTelephone().replaceAll("\\D", "");
        tel = tel.length() >= 4 ? tel.substring(0, 4) : tel;

        return (nom + prenom + tel).toLowerCase();
    }


    //  3.  Ajouter nouveau staff
    public String ajouterStaff(StaffDTO dto, User admin) throws AccessDeniedException, MessagingException {
        if (admin.getRole() != Role.ADMIN){
            throw new AccessDeniedException("Seul un admin peut ajouter des membres du staff.");
        }

        // 1. Validation de l'âge
        if (dto.getDate_de_naissanceStaff() != null && !dto.getDate_de_naissanceStaff().trim().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(dto.getDate_de_naissanceStaff(), formatter);
            LocalDate currentDate = LocalDate.now(); // 2025-08-28, 11:21 AM GMT
            int age = Period.between(birthDate, currentDate).getYears();

            if (age < 16 || age > 80) {
                throw new RuntimeException("error l'âge doit être compris entre 16 et 80 ans.");
            }
        } else {
            throw new RuntimeException("error la date de naissance est requise.");
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
        staff.setGym(admin.getGym()); // Gym principal
        staff.addGym(admin.getGym()); // Ajouter à gyms

        //  mdp == mot de passe
        String mdp = genererMotDePasse(staff);
        staff.setPassword(passwordEncoder.encode(mdp));

        userRepository.save(staff);
        emailService.envoyerEmailBienvenu(staff, mdp);

        return "Staff ajouté avec succès";

    }

    //  4.  Ajout d'un membre par le Staff a qui de droit(par Réceptionniste, Gérant, Admin)
    public String ajouterMembre(MembreDTO dto, User staff) throws MessagingException, AccessDeniedException {
        if (!peutGererMembre(staff)){
            throw new AccessDeniedException("Seul le staff authorisé peut ajouter un membre.");
        }

        Optional<User> existingUser = userRepository.findByTelephoneOrEmail(dto.getNumeroTelephoneMembre(), dto.getEmailMembre());

        if (existingUser.isPresent()){
            User membreExistant = existingUser.get();

            // Ajouter le membre au gym du staff s'il n'est pas déjà associé
            if (!membreExistant.getGyms().contains(staff.getGym())){
                membreExistant.getGyms().add(staff.getGym());
            }

            // Ajouter les gyms supplémentaires
            if (dto.getGymsIds() != null){
                dto.getGymsIds().forEach(gymId->{
                    Gym gym = gymRepository.findById(gymId)
                            .orElseThrow(()->new RuntimeException("Gym introuvable"));
                    if (!membreExistant.getGyms().contains(gym)){
                        membreExistant.getGyms().add(gym);
                    }
                });
            }

            userRepository.save(membreExistant);
            return "Membe déjà existant. Gym ajouté avec succès.";
        }

//        if (dto.getDate_de_naissanceMembre() != null && !dto.getDate_de_naissanceMembre().trim().isEmpty()) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate birthDate = LocalDate.parse(dto.getDate_de_naissanceMembre(), formatter);
//            LocalDate currentDate = LocalDate.now(); // 2025-08-28, 11:21 AM GMT
//            int age = Period.between(birthDate, currentDate).getYears();
//
//            if (age < 16 || age > 80) {
//                throw new RuntimeException("error l'âge doit être compris entre 16 et 80 ans.");
//            }
//        } else {
//            throw new RuntimeException("error la date de naissance est requise.");
//        }

        TypeDeService typeDeService = typeDeServiceRepository.findById(dto.getTypeDeService())
                .orElseThrow(()->new RuntimeException("Type de service introuvable."));
        if (!typeDeService.getGym().equals(staff.getGym())){
            throw new RuntimeException("Ce type de service ne fait pas partie de ce gym.");
        }

        // Création d'un nouveau membre
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

        userRepository.save(nouveauMembre);
        emailService.envoyerEmailBienvenu(nouveauMembre, mdp);
        notificationService.notifyGymAndMember(
                staff.getGym(),
                nouveauMembre,
                "Ajout de menbre",
                "Ajout d'un nouveau membre suite à son inscription physique à la salle de gym",
                "Ajout",
                TypeNotification.INSCRIPTION,
                false

        );




        return "Membre ajouter avec succès !";
    }

    //  5.  Modification des info du compte
    public String modifierMembre(Long id, MembreDTO dto, User currentUser) throws AccessDeniedException {

        User membre = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Membre introuvable !"));

        boolean isSelf = currentUser.getId().equals(membre.getId());

        if (!isSelf && !peutGererMembre(currentUser)){
            throw new AccessDeniedException("Accès refusé.");
        }

        if ((currentUser.getRole() == Role.MEMBRE || currentUser.getRole() == Role.COACH) && !isSelf){
            throw new RuntimeException("Vous pouvez uniquement modifier votre propre profil !");
        }

        if (dto.getNomMembre() != null) membre.setNom(dto.getNomMembre());
        if (dto.getPrenomMembre() !=null) membre.setPrenom(dto.getPrenomMembre());
        if (dto.getRole() !=null) membre.setRole(dto.getRole());
        if (dto.getEmailMembre() !=null) membre.setEmail(dto.getEmailMembre());
        if (dto.getAdresseMembre() !=null) membre.setAdresse(dto.getAdresseMembre());
        if (dto.getNumeroTelephoneMembre() !=null) membre.setTelephone(dto.getNumeroTelephoneMembre());
        if (dto.getDate_de_naissanceMembre() !=null) membre.setDate_de_naissance(dto.getDate_de_naissanceMembre());

        userRepository.save(membre);
        return "Modification effectuée !";
    }

    public String modifierStaff(Long id, StaffDTO dto, User currentUser) throws AccessDeniedException {

        User staff = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Membre introuvable !"));

        boolean isSelf = currentUser.getId().equals(staff.getId());

        if (!isSelf && !peutGererMembre(currentUser)){
            throw new AccessDeniedException("Accès refusé.");
        }

        if ((currentUser.getRole() == Role.MEMBRE || currentUser.getRole() == Role.COACH && currentUser.getRole() == Role.RECEPTIONNISTE) && !isSelf){
            throw new RuntimeException("Vous pouvez uniquement modifier votre propre profil !");
        }

        if (dto.getNomStaff() != null) staff.setNom(dto.getNomStaff());
        if (dto.getPrenomStaff() !=null) staff.setPrenom(dto.getPrenomStaff());
        if (dto.getRoleStaff() !=null) staff.setRole(dto.getRoleStaff());
        if (dto.getEmailStaff() !=null) staff.setEmail(dto.getEmailStaff());
        if (dto.getAdresseStaff() !=null) staff.setAdresse(dto.getAdresseStaff());
        if (dto.getNumeroTelephoneStaff() !=null) staff.setTelephone(dto.getNumeroTelephoneStaff());
        if (dto.getDate_de_naissanceStaff() !=null) staff.setDate_de_naissance(dto.getDate_de_naissanceStaff());

        userRepository.save(staff);
        return "Modification effectuée !";
    }

    //  6.  Consulter un profil
    public User consulterProfil(Long id, User curentUser) throws AccessDeniedException {

        User membre = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Membre introuvable."));

        boolean isSelf = curentUser.getId().equals(membre.getId());
        if (!isSelf && !peutGererMembre(curentUser)){
            throw new AccessDeniedException("Accès refusé.");
        }

        if (!isSelf && ! curentUser.getGyms().contains(membre.getGym())){
            throw new AccessDeniedException("Membre d'une autre salle.");
        }

        if ((curentUser.getRole() == Role.MEMBRE || curentUser.getRole() == Role.COACH) && !isSelf){
            throw new AccessDeniedException("Vous pouvez consulter uniquement votre profil");
        }

        return  membre;
    }

    //  7.  Afficher tout les utilisateur
    public List<User> getAllUser() throws AccessDeniedException {
        User currentUser = initializeAccess(true);
        List<Gym> userGyms = currentUser.getGyms(); // Utilise la liste des gyms
        if (userGyms == null || userGyms.isEmpty()) {
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }

        // Vérifie l'accès pour le gym principal (optionnel, selon vos besoins)
        Gym principalGym = currentUser.getGym();
        if (principalGym != null) {
            verificationAccesGym(currentUser, principalGym, "consulter la liste des abonnements dans");
        }

        logger.debug("Recherche des abonnements pour les salles : {}", userGyms); // Utilisation de logger
        List<User> membres = userRepository.findByGymIn(userGyms);
        if (membres ==null){
            logger.warn("Aucun abonnement trouvé pour les salles de l'utilisateur.");
            return List.of();
        }
        return membres;
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

        // Filtrer les utilisateurs dont le rôle est MEMBRE
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
        // Utiliser la méthode paginée avec les rôles MEMBRE et MEMBRE_TEMPORAIRE
        Page<User> usersPage = userRepository.findByGymInAndRoleIn(
                userGyms,
                List.of(Role.MEMBRE, Role.MEMBRE_TEMPORAIRE),
                pageable
        );

        if (usersPage.isEmpty()) {
            logger.warn("Aucun membre trouvé pour les salles de l'utilisateur.");
            return Page.empty(); // Retourne une page vide
        }

        return usersPage;
    }



    public List<User> getAllStaff() throws AccessDeniedException{
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

        // Filtrer les utilisateurs dont le rôle est MEMBRE
        List<User> membres = users.stream()
                .filter(user -> user.getRole() != Role.MEMBRE && user.getRole() != Role.MEMBRE_TEMPORAIRE)
                .collect(Collectors.toList());

        if (membres.isEmpty()) {
            logger.warn("Aucun membre trouvé avec le rôle MEMBRE pour les salles de l'utilisateur.");
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
        if (currentUser.getGym() == null && requireStaff) { // Vérification du gym uniquement pour staff
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }

    private void verificationAccesGym(User staff,
                                      Gym gym, String action) throws AccessDeniedException {
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)) {
            throw new AccessDeniedException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + " cette gym");
        }
    }

    /*

    public ResponseEntity<Map<String, Object>> changerMotDePasse(ChangerMotDePassDTO dto){
        Map<String, Object> reponse = new HashMap<>();

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();

        currentUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(currentUser);

        reponse.put("message: ", "Mot de passe changer avec succès");
        return  ResponseEntity.ok(reponse);

    }

     */

    public boolean verifierMotDePasse(String motDePasseSaisi, String motDePasseEncode) {
        return passwordEncoder.matches(motDePasseSaisi, motDePasseEncode);
    }
}
