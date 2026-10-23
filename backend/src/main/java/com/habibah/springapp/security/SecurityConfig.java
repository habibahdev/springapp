package com.habibah.springapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configure la sécurité http de l'application.
 * <p>Cette configuration définit les règles d'autorisation des routes,
 * active la gestion des requêtes cors, désactive la protection csrf et
 * configure une authentification sans session fondée sur les jwt.</p>
 * <p>Elle déclare également les composants nécessaires à l'authentification
 * et à l'encodage des mots de passe.</p>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    /**
     * Origine autorisée à effectuer des requêtes vers l'API.
     * Cette valeur est chargée depuis la propriété {@code app.cors.allowed-origin}
     */
    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    /**
     * Filtre chargé d'authentifier les requêtes à partir des jwt.
     */
    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Configure la chaîne de filtres de sécurité http.
     * <p>Les routes d'authentification et les lectures de produits et de catégories
     * sont accessibles sans authentification. La création et la consultation des
     * commandes sont réservées aux utilisateurs autorisés selon leur rôle.</p>
     * <p>Les autres requêtes nécessitent une authentification. Le filtre jwt
     * est exécuté avant le filtre standard d'authentification par nom d'utilisateur
     * et mot de passe.</p>
     * @param http configuration de sécurité http à personnaliser
     * @return chaîne de filtres de sécurité configurée
     * @throws Exception si la configuration de sécurité échoue
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/purchases").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/purchases/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/purchases/**").hasAnyRole("ADMIN")
                        .requestMatchers("a/pi/products/**", "/api/categories/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Déclare le composant chargé d'encoder les mots de passe.
     * <p>BCrypt applique un hachage adapté au stockage des mots de passe.
     * Il ne s'agit pas d'un chiffrement réversible.</p>
     * @return encodeur bcrypt utilisé par l'application
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose le gestionnaire d'authentification de Sîng Security
     * @param config configuration d'authentification fournie par Spring
     * @return gestionnaire chargé de traiter les demandes d'authentification
     * @throws Exception si le gestionnaire ne peut pas être récupéré
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure les règles cors applicquées aux routes de l'API.
     * <p>L'origine autorisée est définie par la propriété {@code app.cors.allowed-origin}.
     * Les méthodes http get, post, put, patch, delete et options sont autorisées, ainsi
     * que toutes les en-têtes de requêtes.</p>
     * @return source de configuration cors appliquée au routes {@code /api/**}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(allowedOrigin));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource url = new UrlBasedCorsConfigurationSource();
        url.registerCorsConfiguration("/api/**", cors);
        return url;
    }
}
