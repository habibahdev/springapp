package com.habibah.springapp.service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service chargé de créer et de lire les jetons JWT.
 * <p>Les jetons générés contiennent l'adresse e-mail de l'utilisateur,
 * son rôle et les dates d'émission et d'expiration.</p>
 * <p>La clé de signature et la durée de validité sont configurées
 * dans les propriétés de l'application.</p>
 */
@Service
public class JwtService {
    /**
     * Clé secrète utilisée pour signer et vérifier les jetons JWT.
     */
    private final SecretKey secretKey;

    /**
     * Durée de validité d'un jeton, exprimée en milisecondes
     */
    private final Long expirationMs;

    /**
     * Initialise le service JWT à partir de sa configuration.
     * <p>La clé secrète doit être encodée en base64 et respecter les
     * exigences de longueur de l'algorithme de signature utilisé</p>
     * @param secret clé secrète encodée en base 64
     * @param expirationMs durée de validité des jetons en millisecondes
     */
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Génère un jwt signé pour un utilisateur.
     * <p>Le jeton contient l'adresse e-mail comme sujet, le rôle dans
     * une revendication personnalisée et les dates d'émission et d'expiration.</p>
     * @param email adresse e-mail de l'utilisateur, utilisée comme sujet du jeton
     * @param role rôle de l'utilisateur à inclure dans le jeton
     * @return jwt signé et compacté sous forme de chaîne
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrait l'adresse e-mail du sujet d'une jwt.
     * <p>La signature du jeton est vérifiée avant que son contenu soit lu.
     * Un jeton mal formé, expiré ou dont la signature est invalide
     * peut provoquer une exception jwt</p>
     * @param token jeton jwt à analyser
     * @return adresse e-mail enregistrée comme sujet du jeton
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
