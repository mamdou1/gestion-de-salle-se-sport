package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FamilleService {

    private final FamilleRepository familleRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final UserRepository userRepository;

    public FamilleService(FamilleRepository familleRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, UserRepository userRepository) {
        this.familleRepository = familleRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.userRepository = userRepository;
    }

    //  1.  Constitution d'une famille appartir de des membres prealablememnt
    public void creerFamille(FamilleDTO dto) {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new RuntimeException("Seul le staff peut créer une famille");
        }

        User chefFamille = chargerChefFamille(dto.getChefFamilleId());
        List<User> membres = chargerEtMettreAJourMembres(dto.getMembresId(), chefFamille);

        Famille famille = convertirDTOEnFamille(dto, chefFamille, membres);
        familleRepository.save(famille);

        for (User membre : membres) {
            membre.setFamille(famille);
            userRepository.save(membre);
        }
    }

    private Famille convertirDTOEnFamille(FamilleDTO dto, User chefFamille, List<User> membres) {
        if (chefFamille == null) {
            throw new RuntimeException("Le chef de famille est obligatoire !");
        }

        Famille famille = new Famille();
        famille.setNom(dto.getNom());
        famille.setGym(chefFamille.getGym());
        famille.setChefFamille(chefFamille);
        famille.setMembres(membres);
        return famille;
    }

    private boolean estMembreDuStaff(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.RECEPTIONNISTE || role == Role.GERANT;
    }

    private User chargerChefFamille(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chef de famille introuvable"));
    }

    private List<User> chargerEtMettreAJourMembres(List<Long> membresId, User chefFamille) {
        List<User> membres = new ArrayList<>();

        if (membresId != null) {
            for (Long membreId : membresId) {
                User membre = userRepository.findById(membreId)
                        .orElseThrow(() -> new RuntimeException("Membre " + membreId + " introuvable"));

                membre.setTelephoneReference(chefFamille.getTelephoneReference());
                membre.setFraisInscriptionPayer(false);
                membres.add(membre);
            }
        }
        return membres;
    }

    public List<Famille> consulterFamille(){

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw new RuntimeException("Seul les membres du staff peuvent consulter la liste des famille");
        }
        return familleRepository.findAll();
    }

    public void suprimerFamille(Long id){
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw  new RuntimeException("Seul les membres du staff peuvent supprimer une famille");
        }

        familleRepository.deleteById(id);
    }

}
