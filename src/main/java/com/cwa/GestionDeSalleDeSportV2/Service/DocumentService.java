package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.FactureCollective;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Entity.Vente;
import com.cwa.GestionDeSalleDeSportV2.Repository.AbonnementRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import java.util.ArrayList;
import java.util.List;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DocumentService {

    private final AbonnementRepository abonnementRepository;

    public DocumentService(AbonnementRepository abonnementRepository) {
        this.abonnementRepository = abonnementRepository;
    }

    //  Génère un PDF de facture pour l’abonnement donné
    public byte[] genererFactureAbonnement(Abonnement abonnement) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);


        BigDecimal montantAbonnement = abonnement.getPrixAbonnement();
        BigDecimal fraisInscription = abonnement.getMembre().getFraisInscription(); // recuperation du frais d'inscription
        List<Abonnement> historique = abonnement.getMembre().getAbonnements() != null ? abonnement.getMembre().getAbonnements() : new ArrayList<>();
        Boolean estPremierAbonnement = historique.isEmpty() || historique.size() == 1;  // Vérifie si c'est le premier ou aucun abonnement

        document.add(new Paragraph("Facture de l'abonnement")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setBold());
        document.add(new Paragraph(""));

        document.add(new Paragraph("Prenom : " + abonnement.getMembre().getNom()));
        document.add(new Paragraph("Nom : " + abonnement.getMembre().getPrenom()));
        document.add(new Paragraph("Type : " + abonnement.getTypes()));
        document.add(new Paragraph("Périod : " + abonnement.getPeriodAbonnement()));
        document.add(new Paragraph("Durée : " + abonnement.getNombreDeMois() + " mois"));
        if (estPremierAbonnement && fraisInscription != null && fraisInscription.compareTo(BigDecimal.ZERO) > 0){
            document.add(new Paragraph("Frais d'inscription" + fraisInscription + "FCFA" ));
            document.add(new Paragraph("Montant total : " + abonnement.getPrixAbonnement().add(fraisInscription) + "FCFA"));
        }else {
            document.add(new Paragraph("Montant : " +abonnement.getPrixAbonnement() + "FCFA"));
        }
        document.add(new Paragraph("Date d'émission : " + LocalDate.now()));
        document.add(new Paragraph("Enregistré par : " + abonnement.getEnregistrerPar().getPrenom() + " " + abonnement.getEnregistrerPar().getNom()));

        document.close();
        return out.toByteArray();
    }

    public byte[] genereFactureFamilial(FactureCollective facture){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Facture Abonnement Familial")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Famille : " + facture.getFamille().getNom()));
        document.add(new Paragraph("Date : " + facture.getDateEmission()));
        document.add(new Paragraph("Période : " + facture.getDateDebut() + " → " + facture.getDateFin()));
        document.add(new Paragraph(" "));

        Table table = new Table(5);

        table.addCell("Nom complet");
        table.addCell("Genre");
        table.addCell("Tarif initial");
        table.addCell("Reduction");
        table.addCell("frais d'inscription");
        table.addCell("tarif final");

        for (User membre : facture.getBeneficiaires()){

            BigDecimal tarifDeBase = membre.getGenre().name().equals("FEMME") ? new BigDecimal("25000") : new BigDecimal("30000");
            BigDecimal tarifFinal = facture.getAbonnementsInclus().stream()
                    .filter(a-> a.getMembre().getId().equals(membre.getId()))
                    .map(Abonnement::getPrixAbonnement)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            BigDecimal reduction = tarifDeBase.subtract(tarifFinal);

            table.addCell(membre.getNom()+ " " +membre.getPrenom());
            table.addCell(membre.getGenre().name());
            table.addCell(tarifDeBase.toString());
            table.addCell(reduction.toString());
            table.addCell(membre.getFraisInscription() != null ? membre.getFraisInscription().toString():"0");
            table.addCell(tarifFinal.toString());
        }

        document.add(table);
        document.add(new Paragraph("Frais d'inscription total : " +facture.getFraisInscriptionTotal() +" FCFA"));
        document.add(new Paragraph("Montant total : " + facture.getMontantTotal() + " FCFA"));
        document.close();
        return out.toByteArray();
    }

//    public byte[] genererFactureVente(Vente vente){
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PdfWriter writer = new PdfWriter(out);
//        PdfDocument pdf = new PdfDocument(writer);
//        Document doc = new Document(pdf);
//
//        doc.add(new Paragraph("Facteur de vente")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(16)
//                .setBold());
//
//        doc.add(new Paragraph("Client : " +vente.))
//    }
}

