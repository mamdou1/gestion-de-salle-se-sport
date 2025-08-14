package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Configuration.UtilisateurActuellementConnecter;
import com.cwa.GestionDeSalleDeSportV2.DTO.FamilleDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.FamilleRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FamilleService {

    private final FamilleRepository familleRepository;
    private final UtilisateurActuellementConnecter utilisateurActuellementConnecter;
    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;

    public FamilleService(FamilleRepository familleRepository, UtilisateurActuellementConnecter utilisateurActuellementConnecter, UserRepository userRepository, AbonnementRepository abonnementRepository) {
        this.familleRepository = familleRepository;
        this.utilisateurActuellementConnecter = utilisateurActuellementConnecter;
        this.userRepository = userRepository;
        this.abonnementRepository = abonnementRepository;
    }

    //  1.  Constitution d'une famille appartir de des membres prealablememnt
    public void creerFamille(FamilleDTO dto) throws AccessDeniedException {

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)) {
            throw new RuntimeException("Seul le staff peut créer une famille");
        }

        User chefFamille = chargerChefFamille(dto.getChefFamilleId());
        if (!currentUser.getGyms().contains(chefFamille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }
        List<User> membres = chargerEtMettreAJourMembres(dto.getMembresId(), chefFamille);

        String telephoneChefFamille = chefFamille.getTelephone();

        Famille famille = convertirDTOEnFamille(dto, chefFamille, membres);
        for (User membre : membres) {
            if (!membre.getId().equals(chefFamille.getId())){
                membre.setTelephoneReference(telephoneChefFamille);
            }
            membre.setFamille(famille);
            userRepository.save(membre);
        }

        familleRepository.save(famille);
    }

    private Famille convertirDTOEnFamille(FamilleDTO dto, User chefFamille, List<User> membres) {
        if (chefFamille == null) {
            throw new RuntimeException("Le chef de famille est obligatoire !");
        }

        Famille famille = new Famille();
        famille.setNom(dto.getNom());
        if (dto.getGymId() != null){
            User userAvecGym = userRepository.findById(dto.getGymId())
                    .orElseThrow(()->new RuntimeException("Utilisateur avec gymId introuvable."));
            famille.setGym(userAvecGym.getGym());
        }else {
            famille.setGym(chefFamille.getGym());
        }
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

                membre.setTelephoneReference(chefFamille.getTelephone());
                membre.setFraisInscriptionPayer(false);
                membres.add(membre);
            }
        }
        return membres;
    }

    //  2.  Consulter la liste des familles
    public List<Famille> consulterFamille(){

        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw new RuntimeException("Seul les membres du staff peuvent consulter la liste des famille");
        }

        // Filtrer les familles par les gyms de l'utilisateur connecté
        return familleRepository.findAll().stream()
                .filter(f -> currentUser.getGyms().contains(f.getGym()))
                .collect(Collectors.toList());    }

    //  3.  Supprimer une famille
    public void suprimerFamille(Long id) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw  new RuntimeException("Seul les membres du staff peuvent supprimer une famille");
        }

        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Famille introuvable"));
        if (!currentUser.getGyms().contains(famille.getGym())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        Abonnement abonnement = abonnementRepository.findByFamille(famille);
        if (abonnement == null){
            throw  new RuntimeException("Aucun abonnement familial trouvé.");
        }

        if (abonnement.getStatut() != StatutAbonnement.EXPIRE && abonnement.getStatut() != StatutAbonnement.RESILIE){
            throw new RuntimeException("Impossible de supprimer un abonnement dont le staut est actif ou bientôt expirer");
        }

        familleRepository.deleteById(id);
    }

    //  4.  Resilier abonnement familial
    public void resilierAbonnementFamilial(Long familleId) throws AccessDeniedException {
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw new AccessDeniedException("Seul les membres du staff peuvent resilier un abonnement");
        }

        // Récupérer la famille
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(()->new RuntimeException("Famille introuvable"));

        // Vérifier l'accès à la gym de la famille
        if (!currentUser.getGyms().contains(famille.getGym())){
            throw new RuntimeException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        // Récupérer l'abonnement familial
        Abonnement abonnement = abonnementRepository.findByFamille(famille);
        if (abonnement == null){
            throw new RuntimeException("Aucun abonnement familial n'est trouvé pour cette famille.");
        }

        // Vérifier si la résiliation est autorisée (par exemple, pas déjà résilié ou expiré)
        if (abonnement.getStatut() == StatutAbonnement.RESILIE ){
            throw new RuntimeException("L'abonnement est déjà résilier");
        } else if (abonnement.getStatut() == StatutAbonnement.EXPIRE) {
            throw new RuntimeException("L'abonnement est déjà expiré");
        }

        // Pour chaque membre dans <<famille.getMembres()>>
        // Parcourez la liste <<membre.getAbonnements()>>
        // Vérifiez que l'abonnement n'est pas déjà RESILIE ou EXPIRE pour éviter des mises à jour inutiles.
        // Définissez le statut à RESILIE
        // Sauvegardez via abonnementRepository.save.
        for (User membre : famille.getMembres()){
            for (Abonnement membreAbonnement : membre.getAbonnements()){
                if (membreAbonnement.getStatut() != StatutAbonnement.RESILIE && membreAbonnement.getStatut() != StatutAbonnement.EXPIRE){
                    membreAbonnement.setStatut(StatutAbonnement.RESILIE);
                    abonnementRepository.save(membreAbonnement);
                }
            }
        }

        abonnement.setStatut(StatutAbonnement.RESILIE);
        abonnementRepository.save(abonnement);
    }

    //  5.  Mettre à jour le telephone de référence
    public void mettreAJourTelephoneRefrenceMembre(Long familleId){
        User currentUser = utilisateurActuellementConnecter.getUtilisateurActuellementConnecter();
        if (!estMembreDuStaff(currentUser)){
            throw new RuntimeException("Seul les membre du staff peuvent mettre à jour le téléphone de référence des membre d'une famille");
        }
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(()->new RuntimeException("Famille introuvable"));

        if (!currentUser.getGyms().contains(famille.getGym())){
            throw new RuntimeException("Accès refusé : vous n'êtes pas autorisé à gérer ce gym.");
        }

        User chefFamille = famille.getChefFamille();
        if (chefFamille == null){
            throw new RuntimeException("Chef de famille introuvable.");
        }

        String telephoneChefFamille = chefFamille.getTelephone();
        for (User membre : famille.getMembres()){
            if (!membre.getId().equals(chefFamille.getId())){
                if (membre.getTelephoneReference() == null || !membre.getTelephoneReference().equals(telephoneChefFamille)){
                    membre.setTelephoneReference(telephoneChefFamille);
                    userRepository.save(membre);
                }
            }
        }
    }

}
