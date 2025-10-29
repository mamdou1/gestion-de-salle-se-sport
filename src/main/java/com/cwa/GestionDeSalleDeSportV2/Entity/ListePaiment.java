package com.cwa.GestionDeSalleDeSportV2.Entity;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.ModeDePaiement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.TypePaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListePaiment {

    private static final Logger logger = LoggerFactory.getLogger(ListePaiment.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypePaiement typePaiement; // ABONNEMENT, FRAIS_INSCRIPTION, CASIER, VENTE

    private LocalDateTime datePaiement;
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private ModeDePaiement modeDePaiement;

    private Long referenceId; // ID de l'abonnement, vente, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    private User acheteur;

    @ManyToOne(fetch = FetchType.LAZY)
    private User staffEnregistreur;

    @ManyToOne(fetch = FetchType.LAZY)
    private Gym gym;

    private String details;

    @Override
    public String toString() {
        logger.debug("Converting ListePaiment to string: id={}", id);
        return "ListePaiment{" +
                "id=" + id +
                ", typePaiement=" + typePaiement +
                ", datePaiement=" + datePaiement +
                ", montant=" + montant +
                ", modeDePaiement=" + modeDePaiement +
                ", referenceId=" + referenceId +
                ", details='" + details + '\'' +
                '}';
    }
}