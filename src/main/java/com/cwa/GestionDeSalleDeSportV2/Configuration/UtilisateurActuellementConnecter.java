package com.cwa.GestionDeSalleDeSportV2.Configuration;


import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurActuellementConnecter {

    private final UserRepository userRepository;

    public UtilisateurActuellementConnecter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cette méthode récupère l'utilisateur connecté à partir du contexte de sécurité
    public User getUtilisateurActuellementConnecter(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTH = " +authentication);

        if (authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("Utilisateur non authentifié.");
        }

        // Le nom d'utilisateur (ici, numéro de téléphone) est utilisé comme identifiant
        String username = authentication.getName();

        // Recherche l'utilisateur par son numéro de téléphone
        return userRepository.findByTelephone(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}
