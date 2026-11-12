package com.habibah.springapp.controller;

import com.habibah.springapp.dto.payment.CheckoutResponse;
import com.habibah.springapp.dto.payment.PaymentRequest;
import com.habibah.springapp.service.payment.PaymentService;
import com.habibah.springapp.service.payment.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Contrôleur REST responsable de la gestion des paiements.
 */
@Tag(name = "Paiements", description = "Gestion des paiements Stripe")
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final StripeWebhookService stripeWebhookService;

    @Operation(summary = "Créer une session de paiement",
            description = "Prépare une commande et crée une session Stripe Checkout")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/checkout")
    public CheckoutResponse checkout(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication
            ) {
        return paymentService.createCheckout(request, authentication.getName());
    }

    /**
     * Reçoit les événements envoyés par Stripe.
     * @param payload corps brut de la requête stripe
     * @param signature signature permettant de vérifier l'origine de la requête
     * @return réponse http indiquant le résultat du traitement
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            stripeWebhookService.handleEvent(payload, signature);
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
