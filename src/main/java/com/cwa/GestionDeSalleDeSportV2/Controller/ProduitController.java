package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.ProduitDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import com.cwa.GestionDeSalleDeSportV2.Service.ProduitService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    public ResponseEntity<Produit> ajouterProduit(@ModelAttribute ProduitDTO dto, @RequestParam(required = false) MultipartFile file) throws IOException {
        Produit produit = produitService.ajouterProduit(dto, file);
        return new ResponseEntity<>(produit, HttpStatus.CREATED);
    }

    // 2. Récupérer la photo d'un produit
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        try {
            byte[] image = produitService.getPhotoProduit(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // Adjust to "image/png" if needed
                    .body(image);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read photo file: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // 3. Modifier un produit
    @PutMapping("/modifier/{id}")
    public ResponseEntity<Produit> modifierProduit(
            @PathVariable Long id,
            @ModelAttribute ProduitDTO dto,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        Produit produit = produitService.modifierProduit(id, dto, file);
        return ResponseEntity.ok(produit);
    }

    // 4. Supprimer un produit
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerProduit(@PathVariable Long id) throws IOException {
        produitService.supprimerProduit(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Lister les produits
    @GetMapping("/lister")
    public ResponseEntity<List<Produit>> listerProduits() throws AccessDeniedException {
        List<Produit> produits = produitService.listerProduits();
        return ResponseEntity.ok(produits);
    }

    // 6. Consulter le détail d'un produit
    @GetMapping("/{produitId}")
    public ResponseEntity<Produit> consulterDetailProd(@PathVariable Long produitId) throws AccessDeniedException {
        Produit produit = produitService.consulterDetailProd(produitId);
        return ResponseEntity.ok(produit);
    }


}