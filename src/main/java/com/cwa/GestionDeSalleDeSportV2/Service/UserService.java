package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.StaffDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final FamilleRepository familleRepository;
    private final GymRepository gymRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, UtilisateurActuellementConnecter utilisateurActuellementConnecter, FamilleRepository familleRepository, GymRepository gymRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.familleRepository = familleRepository;
        this.gymRepository = gymRepository;
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

            // Ajouter le gum du staff s'il n'est pas déjà associé
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
        if (dto.getGymsIds() != null) {
            dto.getGymsIds().forEach(gymId -> {
                Gym gym = gymRepository.findById(gymId)
                        .orElseThrow(() -> new RuntimeException("Gym non trouvé : " + gymId));
                nouveauMembre.addGym(gym);
            });
        }

        nouveauMembre.setDate_creation(LocalDateTime.now());
        nouveauMembre.setFraisInscription(dto.getFraisInscriptionMembre());
        nouveauMembre.setFraisInscriptionPayer(true);
        nouveauMembre.setDate_de_naissance(dto.getGetDate_de_naissanceMembre());

        String mdp = genererMotDePasse(nouveauMembre);
        nouveauMembre.setPassword(passwordEncoder.encode(mdp));

        userRepository.save(nouveauMembre);
        emailService.envoyerEmailBienvenu(nouveauMembre, mdp);

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

        if ((currentUser.getRole() == Role.MEMBRE || currentUser.getRole() == Role.COACH && currentUser.getRole() == Role.RECEPTIONNISTE) && !isSelf){
            throw new RuntimeException("Vous pouvez uniquement modifier votre propre profil !");
        }

        if (dto.getNomMembre() != null) membre.setNom(dto.getNomMembre());
        if (dto.getPrenomMembre() !=null) membre.setPrenom(dto.getPrenomMembre());
        if (dto.getRole() !=null) membre.setRole(dto.getRole());
        if (dto.getEmailMembre() !=null) membre.setEmail(dto.getEmailMembre());
        if (dto.getAdresseMembre() !=null) membre.setAdresse(dto.getAdresseMembre());
        if (dto.getNumeroTelephoneMembre() !=null) membre.setTelephone(dto.getNumeroTelephoneMembre());
        if (dto.getGetDate_de_naissanceMembre() !=null) membre.setDate_de_naissance(dto.getGetDate_de_naissanceMembre());

        userRepository.save(membre);
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

    //  7.  Afficher tout les utilesateur
    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public Gym findGymById(Long id) {
        return gymRepository.findById(id).orElseThrow(() -> new RuntimeException("Gym introuvable"));
    }

    public List<Gym> findGymsByIds(List<Long> ids) {
        return gymRepository.findAllById(ids);
    }
}
