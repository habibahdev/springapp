package com.habibah.springapp.service;

import com.habibah.springapp.dto.DetailRequest;
import com.habibah.springapp.dto.DetailResponse;
import com.habibah.springapp.dto.PurchaseRequest;
import com.habibah.springapp.dto.PurchaseResponse;
import com.habibah.springapp.entity.Detail;
import com.habibah.springapp.entity.Product;
import com.habibah.springapp.entity.Purchase;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import com.habibah.springapp.exception.BusinessException;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.ProductRepository;
import com.habibah.springapp.repository.PurchaseRepository;
import com.habibah.springapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service métier chargé de gérer les commandes des utilisateurs.
 * <p>Il permet de créer une commande, de calculer son montant total,
 * de mettre à jour son statut et de consulter les commandes existnates.</p>
 * <p>Lors de la création d'une commande, le service vérifie l'existence
 * de l'utilisateur et des produits demandés, contrôle le stock disponible,
 * met à jour les quantités en stock et associe les lignes de commande à
 * la commande créée.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Crée une commande pour l'utilisateur identifié par son adresse e-mail.
     * <p>Le service recherche l'utilisateur et les produits commandés, vérifie
     * les stocks disponibles, diminue les quantités en stock, construit
     * les lignes de commande et calcule le montant total.</p>
     * <p>L'opération est transactionnelle : en cas d'échec, les modifications
     * effectuées dans cette transaction sont normalement annulées.</p>
     * @param request informations du client et articles de la commande
     * @param email adresse e-mail de l'utilisateur passant la commande
     * @return commande créée sous forme de dto
     */
    @Transactional
    public PurchaseResponse create(PurchaseRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Purchase purchase = Purchase.builder()
                .customerEmail(request.customerEmail())
                .customerName(request.customerName())
                .shippingAddress(request.shippingAddress())
                .user(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (DetailRequest line : request.items()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("Produit introuvable " + line.productId()));
            if (product.getStock() < line.quantity()) {
                throw new BusinessException("Stock insuffisant pour " + product.getName() + " (dispo : " + product.getStock());
            }
            product.setStock(product.getStock() - line.quantity());
            purchase.addItem(Detail.builder()
                    .product(product)
                    .quantity(line.quantity())
                    .unitPrice(product.getPrice())
                    .build());

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        purchase.setTotal(total);
        return toResponse(purchaseRepository.save(purchase));
    }

    /**
     * Modifie le statut d'une commande existante.
     * @param id identifiant de la commande à modifier
     * @param status nouveau statut de la commande
     * @return commande mise à jour sous forme de dto
     */
    @Transactional
    public PurchaseResponse updateStatus(Long id, PurchaseStatus status) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commande introuvable " + id));
        purchase.setStatus(status);
        return toResponse(purchase);
    }

    /**
     * Récupère toutes les commandes enregistrées.
     * @return liste des commandes converties en dto
     */
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Récupère les commandes associées à un utilisateur.
     * <p>Les commandes sont retournées par date de création décroissante,
     * conformément à la méthode du dépôt appelée.</p>
     * @param email adresse e-mail de l'utilisateur recherché
     * @return liste des commandes de cet utilisateur
     */
    public List<PurchaseResponse> findByUser(String email) {
        return purchaseRepository.findByUserEmailOrderByCreatedAtDesc(email).stream().map(this::toResponse).toList();
    }

    /**
     * Récupère une commande à partir de son identifiant.
     * @param id identifiant de la commande recherchée
     * @return commande trouvée sous forme de dto
     */
    public PurchaseResponse findById(Long id) {
        return purchaseRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Commande introuvable " + id));
    }

    /**
     * Convertit une commande et ses lignes en dto de réponse.
     * <p>Chaque ligne expose l'identifiant et le nom du produti,
     * la quantité commandée et le prix unitaire enregistré</p>
     * @param p commande àc convertir
     * @return dto contenant les infos de la commande et de ses lignes
     */
    private PurchaseResponse toResponse(Purchase p) {
        List<DetailResponse> items = p.getItems().stream()
                .map(i -> new DetailResponse(
                        i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getUnitPrice()
                )).toList();
        return new PurchaseResponse(
                p.getId(), p.getCustomerName(), p.getCustomerEmail(),
                p.getShippingAddress(), p.getStatus(), p.getCreatedAt(),
                p.getTotal(), items
        );
    }
}
