package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.staffsDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GymService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GymRepository gymRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;

    public GymService(EmailService emailService, UserRepository userRepository, PasswordEncoder passwordEncoder, GymRepository gymRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.gymRepository = gymRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
    }

    //  1.  Consulter liste des gym
    public List<Gym> ConsulterGymListe() throws AccessDeniedException {
        initializeAccess(true);

        List<Gym> gym = gymRepository.findAll();
        return gym;
    }

//    public Gym modifierGym(InscriptionDTO dto, Long gymId) throws AccessDeniedException {
//        User admin = initializeAccessGym(true);
//        Gym gym = gymRepository.findById(gymId)
//                .orElseThrow(()->new RuntimeException("Gym non trouvé."));
//
//    }

    //  2.  GetById d'un Gym pour voir les details
    public Gym getGymById(Long gymId) throws AccessDeniedException {
        initializeAccess(true);

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(()->new RuntimeException("Gym introuvable"));
        return gym;
    }

    //  3. Ajouter un membre à l'équipe technique
    public User ajouterUnMembreEquipeTech(staffsDTO dto) throws AccessDeniedException, MessagingException {



        User equipe = new User();

        equipe.setNom(dto.getNomStaff());
        equipe.setPrenom(dto.getPrenomStaff());
        equipe.setAdresse(dto.getAdresseStaff());
        equipe.setGenre(dto.getGenreStaff());
        equipe.setRole(Role.ADMIN_PRINCIPAL);
        equipe.setEmail(dto.getEmailStaff());
        equipe.setDate_creation(LocalDateTime.now());
        equipe.setDate_de_naissance(dto.getDate_de_naissanceStaff());
        equipe.setTelephone(dto.getNumeroTelephoneStaff());

        // mdp == mot de passe
        String mdp = genererMotDePasse(equipe);
        equipe.setPassword(passwordEncoder.encode(mdp));

        userRepository.save(equipe);
        emailService.envoyerEmailBienvenu(equipe, mdp);

        return equipe;
    }

    // Nombre de membre par Gym
    @Transactional
    public Map<Long, Long> getNombreMembresParGym() {
        Map<Long, Long> nombreMembresParGym = new HashMap<>();
        List<Object[]> results = gymRepository.countMembresParGym();

        for (Object[] result : results) {
            nombreMembresParGym.put((Long) result[0], (Long) result[1]);
        }

        return nombreMembresParGym;
    }

    // 4. Nombre de membre par Gym et par statut
    @Transactional
    public Map<Long, Map<StatutAbonnement, Long>> getNombreMembresParStatutEtGym() {
        Map<Long, Map<StatutAbonnement, Long>> result = new HashMap<>();
        List<Object[]> counts = gymRepository.countMembresParStatutEtGym();

        for (Object[] count : counts) {
            Long gymId = (Long) count[0];
            StatutAbonnement statut = (StatutAbonnement) count[1];
            Long nombre = (Long) count[2];

            result.computeIfAbsent(gymId, k -> new HashMap<>())
                    .put(statut, nombre);}

        // Remplir avec 0 pour les statuts manquants
        for (StatutAbonnement statut : StatutAbonnement.values()) {
            for (Map<StatutAbonnement, Long> statutMap : result.values()) {
                statutMap.putIfAbsent(statut, 0L);
            }
        }

        return result;
    }


    private User initializeAccess(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN_PRINCIPAL) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        return currentUser;
    }

    public User initializeAccessGym(boolean requireStaff) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (requireStaff && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Seul un staff autorisé peut effectuer cette opération.");
        }
        if (currentUser.getGym() == null && requireStaff) { // Vérification du gym uniquement pour staff
            throw new AccessDeniedException("Aucun gym associé à l'utilisateur courant.");
        }
        return currentUser;
    }

    //  2.  Generation du mot de passe
    private String genererMotDePasse(User user){

        String nom = user.getNom().length() >= 2 ? user.getNom().substring(0, 2) : user.getNom();
        String prenom = user.getPrenom().length() >= 2 ? user.getPrenom().substring(0, 2) : user.getPrenom();
        String tel = user.getTelephone().replaceAll("\\D", "");
        tel = tel.length() >= 4 ? tel.substring(0, 4) : tel;

        return (nom + prenom + tel).toLowerCase();
    }
}
