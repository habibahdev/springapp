package com.habibah.springapp.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 */
@RequiredArgsConstructor
@Service
public class StripeWebhookService {
    private final PaymentPurchaseService paymentPurchaseService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Vérifie la signature et traite un événement reçu de stripe
     * @param payload contenu brut de la requête
     * @param signature signature tripe transmise dans l'en-tête
     * @throws StripeException si la signature ne pas être vérifiée
     */
    public void handleEvent(String payload, String signature) throws StripeException {
        Event event = Webhook.constructEvent(payload, signature, webhookSecret);
        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = getSession(event);
                if ("paid".equals(session.getPaymentStatus())) {
                    paymentPurchaseService.markAsPaid(session.getId(), session.getPaymentIntent());
                }
            }
            case "checkout.session.async_payment_succeeded" -> {
                Session session = getSession(event);
                paymentPurchaseService.markAsPaid(session.getId(), session.getPaymentIntent());
            }
            case "checkout.session.async_payment_failed", "checkout.session.expired" -> {
                Session session = getSession(event);
                paymentPurchaseService.cancelPendingPurchaseBySessionId(session.getId());
            }
            default -> {}
        }
    }

    /**
     * Récupère la session checkout contenue dans un événement
     * @param event événement stripe reçu
     * @return session checkout
     */
    private Session getSession(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalArgumentException("Impossible de désérialiser la session stripe"));
        if (!(stripeObject instanceof Session session)) {
            throw new IllegalArgumentException("L'objet reçu n'est pas une session Checkout Stripe");
        }
        return session;
    }
}
