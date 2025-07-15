package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Abonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Vente;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class DocumentService {

    //  Génère un PDF de facture pour l’abonnement donné
    public byte[] genererFactureAbonnement(Abonnement abonnement) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Facture de l'abonnement")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setBold());
        document.add(new Paragraph(""));

        document.add(new Paragraph("Prenom : " + abonnement.getMembre().getNom()));
        document.add(new Paragraph("Nom : " + abonnement.getMembre().getPrenom()));
        document.add(new Paragraph("Type : " + abonnement.getType()));
        document.add(new Paragraph("Durée : " + abonnement.getNombreDeMois() + " mois"));
        document.add(new Paragraph("Montant : " + abonnement.getPrixAbonnement() + " FCFA"));
        document.add(new Paragraph("Date d'émission : " + LocalDate.now()));
        document.add(new Paragraph("Enregistré par : " + abonnement.getEnregistrerPar().getPrenom() + " " + abonnement.getEnregistrerPar().getNom()));

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

