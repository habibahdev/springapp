package com.habibah.springapp.security;

import com.habibah.springapp.service.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification chargé d'identifier les utilisateurs à partir d'un jeton
 * JWT transmis dans l'en-tête HTTP Authorization.
 * <p>Lorsqu'un jeton Bearer est présent et qu'aucune authentification n'est déjà
 * établie, le filtre extrait l'adresse e-mail du jeton, charge les informations
 * de l'utilisateur et initialise le contexte de sécurité Spring</p>
 * <p>Si le jeton est invalide ou si l'utilisateur est introuvable, aucune
 * authentification n'est créée. La chaîne de filtres poursuit son exécution ; les
 * règles de sécurité déterminent ensuite si la requête peut accéder à la ressource
 * demandée.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * Service chargé de charger les informations des utilisateurs
     * à partir de leur identifiant de connexion.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Service chargé d'extraire les informations contenues dans un JWT
     * et d'en vérifier la signature et la validité.
     */
    private final JwtService jwtService;

    /**
     * Examine la requête http et établit l'authentification lorsque l'en-tête
     * Authorization contient un jeton Bearer valide.
     * <p>Si une authentification existe déjà, ou si aucun jeton Bearer n'est
     * fourni, aucune nouvelle authentification n'est créée.</p>
     * @param request requête http entrante
     * @param response réponse http associée à la requête
     * @param filterChain chaîne de filtres à poursuivre après le traitement
     * @throws ServletException si une erreur liée au traitement de la requête survient
     * @throws IOException si une erreur d'entrée-sortie survient
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtService.extractEmail(header.substring(7));
                UserDetails user = userDetailsService.loadUserByUsername(email);
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ignored) {}
        }
        filterChain.doFilter(request, response);
    }
}
