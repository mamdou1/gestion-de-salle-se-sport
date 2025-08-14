package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.ProduitDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import com.cwa.GestionDeSalleDeSportV2.Service.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    // 1. Ajouter un produit
    @PostMapping("/ajouter")
    public ResponseEntity<Produit> ajouterProduit(@RequestBody ProduitDTO dto) throws AccessDeniedException {
        Produit produit = produitService.ajouterProduit(dto);
        return new ResponseEntity<>(produit, HttpStatus.CREATED);
    }

    // 2. Modifier un produit
    @PutMapping("/modifier/{id}")
    public ResponseEntity<Produit> modifierProduit(@PathVariable Long id, @RequestBody ProduitDTO dto) throws AccessDeniedException {
        Produit produit = produitService.modifierProduit(id, dto);
        return ResponseEntity.ok(produit);
    }

    // 3. Supprimer un produit
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerProduit(@PathVariable Long id) throws AccessDeniedException {
        produitService.supprimerProduit(id);
        return ResponseEntity.noContent().build();
    }

    // 4. Lister les produits
    @GetMapping("/lister")
    public ResponseEntity<List<Produit>> listerProduits() throws AccessDeniedException {
        List<Produit> produits = produitService.listerProduits();
        return ResponseEntity.ok(produits);
    }

    //  5.  Consulter le details des produits
    @GetMapping("/{produitId}")
    public ResponseEntity<Produit> consulterDetailProd(@PathVariable Long produitId) throws AccessDeniedException {
        Produit produit = produitService.consulterDetailProd(produitId);
        return ResponseEntity.ok(produit);
    }
}