package com.habibah.springapp.config;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client Stripe utilisé pour les paiements.
 */
@Configuration
public class StripeConfig {
    /**
     * Initialise le client Stripe avec la clé secrète de l'application
     * @param secretKey clè secrète
     * @return client Stripe configuré
     */
    @Bean
    public StripeClient stripeClient(@Value("${stripe.secret-key}") String secretKey) {
        return new StripeClient(secretKey);
    }
}
