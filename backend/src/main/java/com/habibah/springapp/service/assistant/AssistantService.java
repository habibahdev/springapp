package com.habibah.springapp.service.assistant;

import com.habibah.springapp.dto.assistant.ChatResponse;
import com.habibah.springapp.dto.assistant.ProductSearchCriteria;
import com.habibah.springapp.dto.assistant.RecommendedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service métier chargé de coordonner les interactions avec
 * l'assistant ia
 */
@RequiredArgsConstructor
@Service
public class AssistantService {
    private final AIService aiService;
    private final ProductSearchService productSearchService;

    /**
     * traite une demande du client et recherche les produits correspondants.
     * @param message message formulé par le client
     * @return réponse de l'assistant accompagnée des produits trouvés
     */
    public ChatResponse chat(String message) {
        ProductSearchCriteria criteria = aiService.extractSearchCriteria(message);
        List<RecommendedProduct> products = productSearchService.search(criteria);
        String responseMessage = buildResponseMessage(products);
        return new ChatResponse(responseMessage, products);
    }

    /**
     * Construit le message adapté aux résultats de la recherche
     * @param products produits trouvés
     * @return message déstiné au client
     */
    private String buildResponseMessage(List<RecommendedProduct> products) {
        if (products.isEmpty()) {
            return "Je n'ai trouvé aucun produit correspondant à votre recherche."
                    + "Vous pouvez essayer d'autres critères.";
        }
        return "J'ai trouvé " + products.size()
                + (products.size() == 1
        ? " produit correspondant à votre recherche."
                : " produits correspondant à votre recherche.")
                + " Voici ce que je vous propose :";
    }
}
