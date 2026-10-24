package com.habibah.springapp.controller;

import com.habibah.springapp.dto.PurchaseRequest;
import com.habibah.springapp.dto.PurchaseResponse;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import com.habibah.springapp.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

/**
 * Contrôleur REST responsable de la gestion des commandes.
 * <p>Expose les endpoints permettant de créer une commande,
 * de consulter les commandes d'un utilisateur ou l'ensemble des commandes,
 * de récupérer une commande par son identifiant et de modifier son statut.</p>
 */
@Tag(
        name = "Commandes",
        description = "Création, consultation et suivi des commandes"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
public class PurchaseController {
    /**
     * Service chargé de la gestion métier des commandes.
     */
    private final PurchaseService purchaseService;

    /**
     * Crée une nouvelle commande pour l'utilisateur authentifié.
     * @param request données nécessaires à la création de la commande,
     *                validées avant le traitement
     * @param authentication infos d'authentification de l'utilisateur connecté,
     *                       notamment son identifiant
     * @return la commande créée sous la forme d'un {@link PurchaseResponse}
     */
    @Operation(
            summary = "Créer une commande",
            description = "Crée une commande pour l'utilisateur authentifié "
            + "à partir des produits et quantités transmis"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody PurchaseRequest request, Authentication authentication) {
        return purchaseService.create(request, authentication.getName());
    }

    /**
     * Récupère toutes les commandes de l'utilisateur authentifié.
     * @param authentication infos d'authentification de l'utilisateur connecté
     * @return la liste des commandes associées à cet utilisateur
     */
    @Operation(
            summary = "Consulter ses commandes",
            description = "Retourne les commandes associées à l'utilisateur connecté"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public List<PurchaseResponse> myPurchases(Authentication authentication) {
        return purchaseService.findByUser(authentication.getName());
    }

    /**
     * Récupère l'ensemble des commandes enregistrées.
     * @return la liste de toutes les commandes
     */
    @Operation(
            summary = "Lister toutes les commandes",
            description = "Retourne l'ensemble des commandes enregistrées"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public List<PurchaseResponse> purchases() {
        return purchaseService.findAll();
    }

    /**
     * Récupère une commande à partir de son identifiant
     * @param id identifiant de la commande recherchée
     * @return les infos de la commande correspondante
     */
    @Operation(
            summary = "Consulter une commande",
            description = "Récupère les détail d'une commande à partir de son identifiant"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public PurchaseResponse get(@PathVariable Long id) {
        return purchaseService.findById(id);
    }

    /**
     * Modifie le statut d'une commande.
     * <p>Le nouveau statut est transmis sous la forme d'un paramètre
     * de requête nommé {@code status}</p>
     * @param id identifiant de la commande à modifier
     * @param status nouveau statut à attribuer à la commande
     * @return la commande mise à jour
     */
    @Operation(
            summary = "Modifier le statut d'une commande",
            description = "Met à jour le statut d'une commande existante"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/status")
    public PurchaseResponse updateStatus(@PathVariable Long id, @RequestParam PurchaseStatus status) {
        return purchaseService.updateStatus(id, status);
    }
}
