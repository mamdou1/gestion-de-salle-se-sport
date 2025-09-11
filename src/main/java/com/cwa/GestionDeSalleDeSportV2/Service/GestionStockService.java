package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Entity.Produit;
import com.cwa.GestionDeSalleDeSportV2.Repository.ProduitRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GestionStockService {

    private final ProduitRepository produitRepository;

    public GestionStockService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    public void deduireStock(Long produitId, Integer quantite) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        if (produit.getQuantiteEnStock() < quantite) {
            throw new RuntimeException("Stock insuffisant pour: " + produit.getNom());
        }

        produit.setQuantiteEnStock(produit.getQuantiteEnStock() - quantite);
        produitRepository.save(produit);
    }
}