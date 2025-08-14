package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.DTO.DemandeInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.DemandeIncriptionRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DemandeInscriptionService {

    private final DemandeIncriptionRepository demandeIncriptionRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DemandeInscriptionService(DemandeIncriptionRepository demandeIncriptionRepository, GymRepository gymRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.gymRepository = gymRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public void soumettreDemande(DemandeInscriptionDTO dto){

        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(()-> new RuntimeException("Gym non trouvé."));

        // Vérifier si un utilisateur existe déjà avec le même telephone ou email
        Optional<User> existingUser = userRepository.findByTelephoneOrEmail(dto.getTelephone(), dto.getEmail());

        DemandeInscription demande = new DemandeInscription();

        demande.setNom(dto.getNom());
        demande.setPrenom(dto.getPrenom());
        demande.setAdresse(dto.getAdresse());
        demande.setEmail(dto.getEmail());
        demande.setGenre(dto.getGenre());
        demande.setGym(gym);
        demande.setTelephone(dto.getTelephone());
        demande.setDateSoumission(LocalDateTime.now());
        demande.setDate_de_naissance(dto.getDate_de_naissance());

       if (existingUser.isPresent()){
           demande.setUser(existingUser.get()); // Lier à l'utilisateur existant
           demande.setPassword(null); // Pas besoin de mot de passe si compte existe déjà
       }else {
           if (dto.getPassword() == null || dto.getPassword().isBlank()) {
               throw new RuntimeException("Mot de passe requis.");
           }
           demande.setPassword(passwordEncoder.encode(dto.getPassword()));
       }

        demandeIncriptionRepository.save(demande);
    }

    public List<DemandeInscription> getDemandeNonValide(){
        return demandeIncriptionRepository.findByEstValideeFalse();
    }
}

