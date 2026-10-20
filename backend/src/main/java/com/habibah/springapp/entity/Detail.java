package com.habibah.springapp.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import lombok.Setter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Représente une ligne de commande dans a boutique en ligne.
 *
 * <p>Une ligne de commande associe un produit à une commande et conserve
 * la quantité achetée ainsi que le prix unitaire appliqué.</p>
 *
 * <p>Le prix unitaire enregistré permet de conserver l'historique du prix
 * au moment de l'achat, même si le prix du produit évolue ultérieurement.</p>
 *
 * <p>Chaque ligne doit être associée à une commande et à un produit.</p>
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Detail {
    /**
     * Identifiant unique de la ligne de commande.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Commande à laquelle appartient cette ligne.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    /**
     * Produit concerné par cette ligne de commande.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * Nombre d'unités du produit commandées.
     */
    private int quantity;

    /**
     * Prix unitaire du produit au moment de la commande.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;
}
