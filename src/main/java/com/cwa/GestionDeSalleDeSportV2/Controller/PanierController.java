package com.cwa.GestionDeSalleDeSportV2.Controller;


import com.cwa.GestionDeSalleDeSportV2.Entity.Panier;
import com.cwa.GestionDeSalleDeSportV2.Service.PanierService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paniers")
public class PanierController {

    private final PanierService panierService;

    public PanierController(PanierService panierService) {
        this.panierService = panierService;
    }

    //  1.  Creer un panier
    @PostMapping("/creer_panier")
    public ResponseEntity<String> creerPanier(){
        panierService.creerPanier();
        return new ResponseEntity<>("Panier créer avec succès.", HttpStatus.CREATED);
    }

    //  2.  Ajouter un produit dans panier
    @PostMapping("/ajout_produit/{panierId}/{produitId}")
    public ResponseEntity<String> ajouterProduit(@PathVariable Long panierId, @PathVariable Long produitId, @RequestParam int quantite) {
        panierService.ajouterProduit(panierId, produitId, quantite);
        return new ResponseEntity<>("Produit ajouter avec succès.", HttpStatus.CREATED);

    }

    //  3.  Modifier une ligne de vente dans le panier
    @PutMapping("/modifier_quantite/{ligneId}")
    public ResponseEntity<String> modifierQuantite(@PathVariable Long ligneId, @RequestParam int quantite) {
        panierService.modifierPanier(ligneId, quantite);
        return new ResponseEntity<>("Produit modifier avec succès.", HttpStatus.CREATED);
    }

    //  4.  supprimer une ligne de vente
    @DeleteMapping("/supprimer_ligne/{ligneId}")
    public void supprimerLigne(@PathVariable Long ligneId) {
        panierService.supprimerLigne(ligneId);
    }

    //  5.  supprimer un panier
    @DeleteMapping("/supprimer_panier/{panierId}")
    public ResponseEntity<String> supprimerPanier(@PathVariable Long panierId) {
        panierService.supprimerPanier(panierId);
        return new ResponseEntity<>("Panier avec succès.", HttpStatus.CREATED);
    }

    //  6.  Envoie Panier
    @PostMapping("/envoie_panier/{panierId}")
    public ResponseEntity<String> envoyerPanier(@PathVariable Long panierId) throws MessagingException {
        panierService.envoyerPanier(panierId);
        return new ResponseEntity<>("Panier envoyer avec succès.", HttpStatus.CREATED);

    }

    //  7.  Consulter les panier en attente
    @GetMapping("/attente")
    public List<Panier> getPaniersEnAttente() {
        return panierService.ConsulterPanierEnAttente();
    }


}

