package com.cwa.GestionDeSalleDeSportV2.Service;

import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.FactureCollective;
import com.cwa.GestionDeSalleDeSportV2.Entity.Famille;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Repository.FactureCollectiveRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FactureCollectiveService {

    private final FactureCollectiveRepository factureCollectiveRepository;
    private final DocumentService documentService;
    private final EmailService emailService;

    public FactureCollectiveService(FactureCollectiveRepository factureCollectiveRepository, DocumentService documentService, EmailService emailService) {
        this.factureCollectiveRepository = factureCollectiveRepository;
        this.documentService = documentService;
        this.emailService = emailService;
    }

    public void creeFactureCollective(Famille famille, List<Abonnement> abonnements, BigDecimal total) throws MessagingException {

        FactureCollective facture = new FactureCollective();

        facture.setFamille(famille);

        // 🔥 CORRECTION : Définir le bénéficiaire principal
        User chefFamille = famille.getChefFamille();
        if (chefFamille == null && !famille.getMembres().isEmpty()) {
            chefFamille = famille.getMembres().get(0);
        }
        facture.setBeneficiairePrincipal(chefFamille);

        // 🔥 CORRECTION : Supprimer la ligne qui cause l'erreur
        // facture.setBeneficiaires(abonnements.stream().map(Abonnement::getMembre).toList());

        facture.setMontantTotal(total);
        BigDecimal fraisInscriptionTotal = abonnements.stream()
                .map(Abonnement::getMembre)
                .map(User::getFraisInscription)
                .filter(f-> f != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        facture.setFraisInscriptionTotal(fraisInscriptionTotal);
        facture.setDateEmission(LocalDate.now());
        facture.setDateDebut(abonnements.get(0).getDateDebutAbonnement());
        facture.setDateFin(abonnements.get(0).getDateFinAbonnement());
        facture.setAbonnementsInclus(abonnements);

        factureCollectiveRepository.save(facture);

        byte[] pdf = documentService.genereFactureFamilial(facture);

        emailService.envoyerEmailAvecPieceJointe(
                famille.getChefFamille().getEmail(),
                "Facture abonnement familial",
                "Voici votre facture pour l'abonnement à la salle" + famille.getGym().getNom() + ". Merci pour votre engagement.",
                pdf,
                "facture_familial" + facture.getId() +".pdf"
        );
    }
}