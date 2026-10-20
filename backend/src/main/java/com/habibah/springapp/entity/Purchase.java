package com.habibah.springapp.entity;

import com.habibah.springapp.entity.enums.PurchaseStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Setter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une commande passée dans la boutique en ligne.
 *
 * <p>Cette entité regroupe les informations relatives au client, à la
 * livraison, au statut de la commande et au montant total.</p>
 *
 * <p>Une commande est associée à un utilisateur et contient une collection
 * de lignes de commande représentées par {@link Detail}</p>
 *
 * <p>La suppression d'une ligne de la collection peut entraîner sa
 * suppression en base de données grâce à la propriété orphanRemoval.
 * Les opérations de persistance sont propagées aux lignes associées.</p>
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "purchase")
public class Purchase {
    /**
     * Identifiant unique de la commande.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom du client associé à la commande.
     */
    @Column(name = "customer_name")
    private String customerName;

    /**
     * Adresse e-mail du client.
     */
    @Column(name = "customer_email")
    private String customerEmail;

    /**
     * Adresse de livraison de la commande.
     */
    @Column(name = "shipping_address")
    private String shippingAddress;

    /**
     * État actuel de la commande.
     * La valeur par défaut est PENDING.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    /**
     * Date et heure de création de la commande.
     * Une valeur initiale est définie lors de la création de l'objet.
     */
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Montant total de la commande.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Utilisateur ayant passé la commande.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Lignes de commande associées à cette commande.
     */
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Detail> items = new ArrayList<>();

    /**
     * Ajoute une ligne à la commande en synchronisant la relation
     * avec l'entité Detail.
     *
     * @param item ligne de commande à ajouter
     */
    public void addItem(Detail item) {
        item.setPurchase(this);
        items.add(item);
    }
}
