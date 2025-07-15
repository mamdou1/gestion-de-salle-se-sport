package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.DTO.MembreDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.StaffDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        staff.setGym(admin.getGym());

        //  mdp == mot de passe
        String mdp = genererMotDePasse(staff);
        staff.setPassword(passwordEncoder.encode(mdp));

        userRepository.save(staff);
        emailService.envoyerEmailBienvenu(staff, mdp);

        return "Staff ajouté avec succès";

    }

    //  4.  Ajout d'un membre par le Staff a qui de droit(par Réceptionniste, Gérant, Admin)
    public String ajouterMembre(MembreDTO dto, User staff) throws MessagingException {

        User membre = new User();
        membre.setNom(dto.getNomMembre());
        membre.setPrenom(dto.getPrenomMembre());
        membre.setEmail(dto.getEmailMembre());
        membre.setTelephone(dto.getNumeroTelephoneMembre());
        membre.setGenre(dto.getGenreMembre());
        membre.setAdresse(dto.getAdresseMembre());
        membre.setRole(Role.MEMBRE);
        membre.setGym(staff.getGym());
        membre.setDate_creation(LocalDateTime.now());
        membre.setDate_de_naissance(dto.getGetDate_de_naissanceMembre());

        String mdp = genererMotDePasse(membre);
        membre.setPassword(passwordEncoder.encode(mdp));

        userRepository.save(membre);
        emailService.envoyerEmailBienvenu(membre, mdp);

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

        if (!isSelf && ! curentUser.getGym().equals(membre.getGym())){
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
}
