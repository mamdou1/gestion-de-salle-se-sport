package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.DTO.DemandeInscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.DemandeInscription;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Repository.DemandeIncriptionRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DemandeInscriptionService {

    private final DemandeIncriptionRepository demandeIncriptionRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;

    public DemandeInscriptionService(DemandeIncriptionRepository demandeIncriptionRepository, GymRepository gymRepository, PasswordEncoder passwordEncoder) {
        this.demandeIncriptionRepository = demandeIncriptionRepository;
        this.gymRepository = gymRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void soumettreDemande(DemandeInscriptionDTO dto){

        Gym gym = gymRepository.findById(dto.getGym().getId())
                .orElseThrow(()-> new RuntimeException("Gym non trouvé."));

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

        if (dto.getPassword() == null || dto.getPassword().isBlank()){
            throw new RuntimeException("Mot de passe requis.");
        }

        demande.setPassword(passwordEncoder.encode(dto.getPassword()));

        demandeIncriptionRepository.save(demande);
    }

    public List<DemandeInscription> getDemandeNonValide(){
        return demandeIncriptionRepository.findByEstValideeFalse();
    }
}

