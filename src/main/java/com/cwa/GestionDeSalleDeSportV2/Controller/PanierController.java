package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutPanier;
import com.cwa.GestionDeSalleDeSportV2.Entity.Panier;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.PanierRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import com.cwa.GestionDeSalleDeSportV2.Service.PanierService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paniers")
public class PanierController {

    private final PanierService panierService;
    private final PanierRepository panierRepository;// ← AJOUTÉ : indispensable pour l'historique


    @Autowired
    private UserRepository userRepository;

    public PanierController(PanierService panierService, PanierRepository panierRepository) {
        this.panierService = panierService;
        this.panierRepository = panierRepository; // ← injection
    }

    // 1. Créer un panier
    @PostMapping("/creer_panier")
    public ResponseEntity<Long> creerPanier() {
        Panier panier = panierService.creerPanier();
        return new ResponseEntity<>(panier.getId(), HttpStatus.CREATED);
    }

    // 2. Ajouter un produit dans panier
    @PostMapping("/ajout_produit/{panierId}/{produitId}")
    public ResponseEntity<String> ajouterProduit(@PathVariable Long panierId, @PathVariable Long produitId, @RequestParam int quantite) {
        panierService.ajouterProduit(panierId, produitId, quantite);
        return new ResponseEntity<>("Produit ajouter avec succès.", HttpStatus.CREATED);
    }

    // 3. Modifier une ligne de vente dans le panier
    @PutMapping("/modifier_quantite/{ligneId}")
    public ResponseEntity<String> modifierQuantite(@PathVariable Long ligneId, @RequestParam int quantite) {
        panierService.modifierPanier(ligneId, quantite);
        return new ResponseEntity<>("Produit modifier avec succès.", HttpStatus.CREATED);
    }

    // 4. Supprimer une ligne de vente
    @DeleteMapping("/supprimer_ligne/{ligneId}")
    public void supprimerLigne(@PathVariable Long ligneId) {
        panierService.supprimerLigne(ligneId);
    }

    // 5. Supprimer un panier
    @DeleteMapping("/supprimer_panier/{panierId}")
    public ResponseEntity<String> supprimerPanier(@PathVariable Long panierId) {
        panierService.supprimerPanier(panierId);
        return new ResponseEntity<>("Panier supprimé avec succès.", HttpStatus.CREATED);
    }

    // 6. Envoyer le panier
    @PostMapping("/envoie_panier/{panierId}")
    public ResponseEntity<String> envoyerPanier(@PathVariable Long panierId) throws MessagingException {
        panierService.envoyerPanier(panierId);
        return new ResponseEntity<>("Panier envoyé avec succès.", HttpStatus.CREATED);
    }

    // 7. Consulter les paniers en attente (staff)
    @GetMapping("/attente")
    public List<Panier> getPaniersEnAttente() {
        return panierService.ConsulterPanierEnAttente();
    }

    @GetMapping("/historique")
    public ResponseEntity<List<Panier>> getHistoriquePaniers(Authentication authentication) {
        String telephone = authentication.getName(); // toujours le numéro de téléphone

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + telephone));

        List<Panier> paniers = panierRepository.findByMembreIdOrderByDateCreationDesc(user.getId());

        return ResponseEntity.ok(paniers);
    }
    // 8. VALIDER UN PANIER (STAFF UNIQUEMENT) ← C'EST ÇA QUI MANQUAIT !
    @PostMapping("/valider/{panierId}")
    public ResponseEntity<String> validerPanier(@PathVariable Long panierId) {
        try {
            // On récupère le panier
            Panier panier = panierRepository.findById(panierId)
                    .orElseThrow(() -> new RuntimeException("Panier non trouvé"));

            // Sécurité : on ne valide que les paniers EN_ATTENTE
            if (panier.getStatut() != StatutPanier.EN_ATTENTE_VALIDATION) {
                return ResponseEntity.badRequest().body("Ce panier a déjà été traité");
            }

            // ON VALIDE LE PANIER DANS LE SERVICE
            panierService.validerPanier(panierId);

            return ResponseEntity.ok("Panier validé avec succès ! Le membre a été notifié.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la validation : " + e.getMessage());
        }
    }

    // 9. REJETER UN PANIER (STAFF UNIQUEMENT)
    @PostMapping("/rejeter/{panierId}")
    public ResponseEntity<String> rejeterPanier(@PathVariable Long panierId) {
        try {
            panierService.rejeterPanier(panierId);
            return ResponseEntity.ok("Panier rejeté avec succès. Le membre a été notifié.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du rejet : " + e.getMessage());
        }
    }
}