package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.Salle;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.SalleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalleService {

    private final SalleRepository salleRepository;
    private final UserRepository userRepository;

    public SalleService(SalleRepository salleRepository, UserRepository userRepository) {
        this.salleRepository = salleRepository;
        this.userRepository = userRepository;
    }

    //  1.  Ajouter une salle pour le gym du staff
    public Salle ajouterSalle(Gym gym ,String nom){

        Optional<Salle> existante = salleRepository.findByNomAndGym(nom,gym);
        if (existante.isPresent()){
            throw new RuntimeException("Ce numéro de salle existe déjà dans ce gym.");
        }

        Salle salle = new Salle();
        salle.setNom(nom);
        salle.setGym(gym);

        return salleRepository.save(salle);
    }

    private void verificationAccesSalle(User staff, Gym gym, String action){
        if (!userRepository.existsById(staff.getId()) || !staff.getGyms().contains(gym)){
            throw new RuntimeException("Accès refusé : l'utilisateur n'est pas autorisé à " + action + "cette salle");
        }
    }

    //  2.  Consulter toutes les salles d’un gym
    public List<Salle> ListerSalleParGym(User staff ,Gym gym){
        verificationAccesSalle(staff, gym, "Consulter");
        return salleRepository.findByGym(gym);
    }

    //  3.  Trouver une salle par ID (getById)
    public Salle getSalleById(User staff ,Long id){
        Salle salle = salleRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Salle introuvable"));
        verificationAccesSalle(staff, salle.getGym(), "accéder à");
        return salle;
    }

    //  4.  Modifier une salle
    public Salle modifierSalle(User staff, Long id, String nouveauNom){
        Salle salle = salleRepository.findById(id)
                        .orElseThrow(()->new RuntimeException("Salle introuvable"));
        verificationAccesSalle(staff, salle.getGym(), "modifier");
        salle.setNom(nouveauNom);
        return salleRepository.save(salle);
    }

    //  5.  Supprimer une salle
    public void supprimerSalle(User staff, Long salleId){
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(()->new RuntimeException("Salle introuvable"));
        verificationAccesSalle(staff, salle.getGym(), "supprimer");
        salleRepository.deleteById(salleId);
    }
}
