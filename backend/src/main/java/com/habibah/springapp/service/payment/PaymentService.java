package com.habibah.springapp.service.payment;

import com.habibah.springapp.dto.payment.CheckoutResponse;
import com.habibah.springapp.dto.detail.DetailResponse;
import com.habibah.springapp.dto.payment.PaymentRequest;
import com.habibah.springapp.dto.purchase.PurchaseResponse;
import com.habibah.springapp.exception.BusinessException;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service chargé de la gestion des paiements
 */
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final StripeClient stripeClient;
    private final PaymentPurchaseService paymentPurchaseService;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    /**
     * Prépare une commande et crée une session Stripe checkout.
     * @param paymentRequest informations de la commande
     * @param email adresse e-mail de l'utilisateur connecté
     * @return url de paiement stripe
     */
    public CheckoutResponse createCheckout(PaymentRequest paymentRequest, String email) {
        PurchaseResponse purchase = paymentPurchaseService.createPendingPurchase(paymentRequest, email);
        try {
            List<SessionCreateParams.LineItem> lineItems = purchase.items().stream()
                    .map(this::buildLineItem)
                    .toList();
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(purchase.customerEmail())
                    .setClientReferenceId(purchase.id().toString())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("purchaseId", purchase.id().toString())
                    .addAllLineItem(lineItems)
                    .build();
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("purchase-checkout-" + purchase.id())
                    .build();
            Session session = stripeClient.v1()
                    .checkout()
                    .sessions()
                    .create(params, options);
            paymentPurchaseService.attachStripeSession(purchase.id(), session.getId());
            return new CheckoutResponse(purchase.id(), session.getId(), session.getUrl());
        } catch (StripeException exception) {
            paymentPurchaseService.cancelPendingPurchase(purchase.id());
            throw new BusinessException("Impossible de créer la session de paiement stripe");
        }
    }

    /**
     * Construit une ligne de paiement à partir d'une ligne de commande
     * @param item ligne de commande
     * @return ligne Stripe
     */
    private SessionCreateParams.LineItem buildLineItem(DetailResponse item) {
        long unitAmount = item.unitPrice()
                .movePointRight(2)
                .longValueExact();

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(item.productName())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("eur")
                        .setUnitAmount(unitAmount)
                        .setProductData(productData)
                        .build();
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.quantity())
                .setPriceData(priceData)
                .build();
    }
}
