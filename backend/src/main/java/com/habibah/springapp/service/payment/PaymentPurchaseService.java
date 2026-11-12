package com.habibah.springapp.service.payment;

import com.habibah.springapp.dto.detail.DetailRequest;
import com.habibah.springapp.dto.detail.DetailResponse;
import com.habibah.springapp.dto.payment.PaymentRequest;
import com.habibah.springapp.dto.purchase.PurchaseResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PaymentPurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Crée une commande en attente et réserve les produits demandés
     * @param paymentRequest données de la commande
     * @param email adresse e-mail de l'utilisateur authentifié
     * @return commande créée
     */
    @Transactional
    public PurchaseResponse createPendingPurchase(PaymentRequest paymentRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        Purchase purchase = Purchase.builder()
                .customerName(user.getFullName())
                .customerEmail(user.getEmail())
                .shippingAddress(paymentRequest.shippingAddress())
                .user(user)
                .status(PurchaseStatus.PENDING)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        // regroupe les éventuelles lignes portant sur le même produit
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (DetailRequest line : paymentRequest.items()) {
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> new NotFoundException("Produit introuvable" + entry.getKey()));
            int quantity = entry.getValue();
            if (product.getStock() < quantity) {
                throw new BusinessException("Stock insuffisant pour " + product.getName() + " (dispo : " + product.getStock() + ")");
            }
            // réservation du stock
            product.setStock(product.getStock() - quantity);
            // conservation du prix au moment de la commande
            purchase.addItem(Detail.builder()
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        purchase.setTotal(total);
        Purchase savedPurchase = purchaseRepository.saveAndFlush(purchase);
        return toResponse(savedPurchase);
    }

    /**
     * Associe une session Stripe à une commande.
     * @param purchaseId identifiant de la commande
     * @param sessionId identifiant de la session Stripe
     */
    @Transactional
    public void attachStripeSession(Long purchaseId, String sessionId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Commande introuvable"));
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new BusinessException("La commande n'est plus en attente de paiement");
        }
        purchase.setStripeSessionId(sessionId);
    }

    /**
     * Annule une commande en attente et restitue les stocks réservés
     * @param purchaseId identifiant de la commande
     */
    @Transactional
    public void cancelPendingPurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Commande introuvable"));
        // évite de restituer plusieurs fois le stock
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            return;
        }
        for (Detail item : purchase.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Produit introuvable"));
            product.setStock(product.getStock() + item.getQuantity());
        }
        purchase.setStatus(PurchaseStatus.CANCELLED);
    }

    /**
     * Confirme le paiement d'une commande à partir de sa session stripe
     * @param sessionId identifiant de la session stripe
     * @param paymentIntentId identifiant du paiement stripe
     */
    @Transactional
    public void markAsPaid(String sessionId, String paymentIntentId) {
        Purchase purchase = purchaseRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Commande introuvable pour la session " + sessionId));
        if (purchase.getStatus() == PurchaseStatus.PAID) {
            return;
        }
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new BusinessException("Impossible de confirmer une commande qui n'est plus en attente");
        }
        purchase.setStripePaymentIntentId(paymentIntentId);
        purchase.setStatus(PurchaseStatus.PAID);
    }

    /**
     * Annule une commande à partir de son identifiant de session stripe
     * @param sessionId identifiant de la session stripe
     */
    @Transactional
    public void cancelPendingPurchaseBySessionId(String sessionId) {
        Purchase purchase = purchaseRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Commande introuvable pour la session stripe" + sessionId));
        cancelPendingPurchase(purchase.getId());
    }

    /**
     * Convertit une commande en Dto
     * @param purchase commande à convertir
     * @return dto de la commande
     */
    private PurchaseResponse toResponse(Purchase purchase) {
        List<DetailResponse> items = purchase.getItems().stream()
                .map(item -> new DetailResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                )).toList();
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getCustomerName(),
                purchase.getCustomerEmail(),
                purchase.getShippingAddress(),
                purchase.getStatus(),
                purchase.getCreatedAt(),
                purchase.getTotal(),
                items
        );
    }
}
