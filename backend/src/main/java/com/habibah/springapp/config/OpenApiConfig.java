package com.habibah.springapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI de SpringApp.
 * <p>Définit les infos générales de l'API et le mécanisme
 * d'authentification jwt utilisé pour les endpoints protégés.</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SpringApp API",
                version = "1.0.0",
                description = """
                        API REST d'une boutique en ligne.
                        Elle permet de gérer les utilisateurs, les produits, les
                        catégories et les commandes.
                        L'authentification repose sur des jetons jwt
                        """,
                contact = @Contact(name = "Habibah Ouattara")
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Serveur de développement local"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Saisissez votre jeton JWT dans le champ prévu à cet effet"
)
public class OpenApiConfig {
}
