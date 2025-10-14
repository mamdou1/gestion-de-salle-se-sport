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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final StockageDeFichierService stockageDeFichierService;

    public GymService(EmailService emailService, UserRepository userRepository, PasswordEncoder passwordEncoder, GymRepository gymRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, StockageDeFichierService stockageDeFichierService) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.gymRepository = gymRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.stockageDeFichierService = stockageDeFichierService;
    }

    //  1.  Consulter liste des gym
    public List<Gym> ConsulterGymListe() throws AccessDeniedException {
        initializeAccess(true);

        List<Gym> gym = gymRepository.findAll();
        return gym;
    }

    public String modifierGym(InscriptionDTO dto, Long gymId, MultipartFile file) throws IOException {
        initializeAccessGym(true);
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(()->new RuntimeException("Gym non trouvé."));
        if(dto.getNomGym() != null) gym.setNom(dto.getNomGym());
        if(dto.getAdresseGym() != null) gym.setAdresse(dto.getAdresseGym());
        if(dto.getEmailGym() != null) gym.setEmail(dto.getEmailGym());
        if(dto.getTelephoneGym() != null) gym.setTelephone(dto.getTelephoneGym());
        if(dto.getDescription() != null) gym.setDescription(dto.getDescription());
        if(file != null && !file.isEmpty()){
//            gym.setPhoto(file.getBytes());

            // Supprimer ancienne image si elle existe
            if (gym.getImageUrl() != null){
                Path oldPath = Paths.get(gym.getImageUrl());
                Files.deleteIfExists(oldPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String savedFileName = stockageDeFichierService.saveFile(file, fileName);
            gym.setImageUrl(savedFileName);
        }

        gymRepository.save(gym);
        return "Gym modifier avec succès.";

    }

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
        if (
                requireStaff &&
                        currentUser.getRole() != Role.ADMIN_PRINCIPAL &&
                        currentUser.getRole() != Role.ADMIN &&
                        currentUser.getRole() != Role.RECEPTIONNISTE &&
                        currentUser.getRole() != Role.MEMBRE &&
                        currentUser.getRole() != Role.GERANT &&
                        currentUser.getRole() != Role.COACH) {
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
