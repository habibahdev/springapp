package com.habibah.springapp.initializer;

import com.habibah.springapp.entity.*;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import com.habibah.springapp.entity.enums.Role;
import com.habibah.springapp.repository.CategoryRepository;
import com.habibah.springapp.repository.ProductRepository;
import com.habibah.springapp.repository.PurchaseRepository;
import com.habibah.springapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Initialise les données de démo de la boutique au démarrage.
 * <p>Crée les catégories, les produits, un compte client et plusieurs
 * commandes lorsque ces données n'existent pas encore</p>
 * <p>Les données sont créées de manière idempotente afin d'éviter
 * les doublons lors des redémarrages de l'application</p>
 */
@Component
public class ProductInitializer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInitializer.class);
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_DEMO_DATA_ENABLED:true}")
    private boolean enabled;

    @Value("${APP_DEMO_CLIENT_EMAIL:client@springapp.test}")
    private String clientEmail;

    @Value("${APP_DEMO_CLIENT_PASSWORD:client123}")
    private String clientPassword;

    public ProductInitializer(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PurchaseRepository purchaseRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lance l'initialisation des données de démo
     * @param args incoming application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            LOGGER.info("L'initialisation des données de démo est désactivée");
            return;
        }
        Map<String, Category> categories = initializeCategories();
        Map<String, Product> products = initializeProducts(categories);
        User client = initializeCustomer();
        if (client != null) {
            initializePurchases(client, products);
        }
        LOGGER.info("Initialisation des données de démo terminée");
    }

    /**
     * Crée les catégories de la boutique si elles n'existent pas
     * @return les catégories disponibles
     */
    private Map<String, Category> initializeCategories() {
        Map<String, Category> categories = new LinkedHashMap<>();
        categoryRepository.findAll().forEach(category -> categories.put(category.getName(), category));
        List<String> categoryName = List.of(
                "Informatique",
                "Téléphones et accessoires",
                "Audio",
                "Maison et décoration",
                "Livres",
                "Gaming"
        );
        for (String name : categoryName) {
            if (!categories.containsKey(name)) {
                Category category = Category.builder()
                        .name(name)
                        .build();
                Category savedCategory = categoryRepository.save(category);
                categories.put(savedCategory.getName(), savedCategory);
                LOGGER.info("Catégorie créée : {}", name);
            }
        }
        return categories;
    }

    /**
     * Cré les produits de démo et les associe à leurs catégories
     * @param categories catégories disponibles
     * @return produits disponibles
     */
    private Map<String, Product> initializeProducts(Map<String, Category> categories) {
        Map<String, Product> products = new LinkedHashMap<>();
        productRepository.findAll().forEach(product -> products.putIfAbsent(product.getName(), product));
        List<Product> productList = List.of(
                createProduct(
                        "Ordinateur portable Lenovo IdeaPad",
                        "Ordinateur portable polyvalent avec processeur performant, "
                                + "16 Go de RAM et SSD de 512 Go.",
                        "649.99",
                        12,
                        "https://images.unsplash.com/photo-1496181133206-80ce9b88a853",
                        categories.get("Informatique")
                ),
                createProduct(
                        "Ordinateur portable ASUS VivoBook",
                        "Ordinateur portable léger et élégant, idéal pour les études "
                                + "et le travail quotidien.",
                        "729.99",
                        8,
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
                        categories.get("Informatique")
                ),
                createProduct(
                        "Clavier mécanique Logitech",
                        "Clavier mécanique confortable avec rétroéclairage "
                                + "et touches réactives.",
                        "89.90",
                        25,
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3",
                        categories.get("Informatique")
                ),
                createProduct(
                        "Souris sans fil Logitech",
                        "Souris ergonomique sans fil avec connexion USB "
                                + "et autonomie longue durée.",
                        "34.99",
                        30,
                        "https://images.unsplash.com/photo-1527814050087-3793815479db",
                        categories.get("Informatique")
                ),
                createProduct(
                        "Samsung Galaxy S24",
                        "Smartphone avec écran AMOLED, appareil photo haute résolution "
                                + "et stockage de 256 Go.",
                        "899.00",
                        10,
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9",
                        categories.get("Téléphones et accessoires")
                ),
                createProduct(
                        "Écouteurs Bluetooth",
                        "Écouteurs sans fil avec réduction de bruit "
                                + "et boîtier de recharge.",
                        "59.90",
                        20,
                        "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1",
                        categories.get("Audio")
                ),
                createProduct(
                        "Casque audio Sony",
                        "Casque audio confortable avec son immersif "
                                + "et réduction de bruit.",
                        "199.99",
                        15,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
                        categories.get("Audio")
                ),
                createProduct(
                        "Enceinte Bluetooth JBL",
                        "Enceinte portable résistante à l'eau "
                                + "avec un son puissant.",
                        "79.99",
                        18,
                        "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1",
                        categories.get("Audio")
                ),
                createProduct(
                        "Lampe de bureau design",
                        "Lampe de bureau moderne avec éclairage LED "
                                + "et intensité réglable.",
                        "39.90",
                        14,
                        "https://images.unsplash.com/photo-1507473885765-e6ed057f782c",
                        categories.get("Maison et décoration")
                ),
                createProduct(
                        "Roman Le Petit Prince",
                        "Un grand classique de la littérature française "
                                + "dans une édition illustrée.",
                        "12.50",
                        40,
                        "https://images.unsplash.com/photo-1544947950-fa07a98d237f",
                        categories.get("Livres")
                ),
                createProduct(
                        "Manette sans fil",
                        "Manette ergonomique compatible avec plusieurs plateformes "
                                + "de jeu.",
                        "64.90",
                        16,
                        "https://images.unsplash.com/photo-1592840496694-26d035b52b48",
                        categories.get("Gaming")
                ),
                createProduct(
                        "Support pour ordinateur portable",
                        "Support ajustable en aluminium pour améliorer "
                                + "le confort de travail.",
                        "29.90",
                        22,
                        "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46",
                        categories.get("Informatique")
                ),
                createProduct(
                        "Clé USB SanDisk 128 Go",
                        "Clé USB compacte de 128 Go pour stocker et transférer facilement vos fichiers.",
                        "19.90",
                        35,
                        "https://images.unsplash.com/photo-1618410320928-25228d811631",
                        categories.get("Informatique")
                ),
                createProduct(
                        "Coque Samsung Galaxy",
                        "Coque de protection transparente et résistante aux chocs pour smartphone Samsung.",
                        "14.90",
                        28,
                        "https://images.unsplash.com/photo-1601593346740-925612772716",
                        categories.get("Téléphones et accessoires")
                ),
                createProduct(
                        "Microphone USB Blue",
                        "Microphone USB idéal pour les visioconférences, le streaming et l'enregistrement audio.",
                        "119.90",
                        9,
                        "https://images.unsplash.com/photo-1590602847861-f357a9332bbc",
                        categories.get("Audio")
                ),
                createProduct(
                        "Souris gaming RGB",
                        "Souris gaming ergonomique avec éclairage RGB et sensibilité réglable.",
                        "49.90",
                        0,
                        "https://images.unsplash.com/photo-1527814050087-3793815479db",
                        categories.get("Gaming")
                )
        );

        for (Product product : productList) {
            if (!products.containsKey(product.getName())) {
                Product savedProduct = productRepository.save(product);
                products.put(savedProduct.getName(), savedProduct);
                LOGGER.info("Produit créé : {}", savedProduct.getName());
            }
        }
        return products;
    }

    /**
     * Crée le compte client de démo s'il n'existe pas
     * @return le client existant ou nouvellement créé
     */
    private User initializeCustomer() {
        if (clientEmail.isBlank() || clientPassword.isBlank()) {
            LOGGER.warn("Création du client de démo ignorée : identifiants non configurés");
            return null;
        }
        if (userRepository.existsByEmail(clientEmail)) {
            User client = userRepository.findByEmail(clientEmail).orElseThrow();
            if (client.getRole() != Role.USER) {
                throw new IllegalStateException("L'adresse du client de démo appartient à un utilisateur qui n'a" +
                        "pas le rôle USER");
            }
            LOGGER.info("Le compte client de démo existe déjà");
            return client;
        }

        User client = User.builder()
                .fullName("Client Test")
                .email(clientEmail)
                .password(passwordEncoder.encode(clientPassword))
                .role(Role.USER)
                .build();
        User savedClient = userRepository.save(client);
        LOGGER.info("Compte client de démo créé : {}", clientEmail);
        return savedClient;
    }

    /**
     * Crée deux commandes de démo pour le client.
     * <p>Les commandes ne sont créées que si le client ne possède
     * encore aucune commande</p>
     * @param client client associé à la commande
     * @param products produits disponibles
     */
    private void initializePurchases(User client, Map<String, Product> products) {
        List<Purchase> existingPurchases = purchaseRepository.findByUserEmailOrderByCreatedAtDesc(client.getEmail());
        if (!existingPurchases.isEmpty()) {
            LOGGER.info("Les commandes du client de démo existent déjà");
            return;
        }
        Purchase firstPurchase = createPurchase(
                client,
                "15 rue de la république, 76000 Rouen",
                PurchaseStatus.SHIPPED,
                LocalDateTime.now().minusDays(12),
                products,
                Map.of("Ordinateur portable Lenovo IdeaPad", 1, "Souris sans fil Logitech", 1)
        );
        Purchase secondPurchase = createPurchase(
                client,
                "15 rue de la république, 76000 Rouen",
                PurchaseStatus.PAID,
                LocalDateTime.now().minusDays(3),
                products,
                Map.of("Samsung Galaxy S24", 1, "Écouteurs Bluetooth", 2)
        );
        purchaseRepository.saveAll(List.of(firstPurchase, secondPurchase));
        LOGGER.info("Deux commandes de démo ont été créées");
    }

    /**
     * Construit un produit de démonstration
     * @param name nom du produit
     * @param description description du produit
     * @param price prix du produit
     * @param stock quantité disponible
     * @param imageUrl adresse de l'image
     * @param category catégorie associée
     * @return produit prêt à être enregistré
     */
    private Product createProduct(String name, String description, String price, int stock, String imageUrl, Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .stock(stock)
                .imageUrl(imageUrl)
                .category(category)
                .build();
    }

    /**
     * Construit une commande avec ses lignes et calcule son montant total
     * @param client utilisateur de la commande
     * @param address adresse de la livraison
     * @param status statut de la commande
     * @param createdAt date de création
     * @param products noms des produits
     * @param quantities quantités commandées
     * @return commande
     */
    private Purchase createPurchase(User client, String address, PurchaseStatus status, LocalDateTime createdAt, Map<String, Product> products, Map<String, Integer> quantities) {
        Purchase purchase = Purchase.builder()
                .customerEmail(client.getEmail())
                .customerName(client.getFullName())
                .shippingAddress(address)
                .user(client)
                .status(status)
                .createdAt(createdAt)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product == null) {
                throw new IllegalStateException("Produit introuvable pour la commande : " + entry.getKey());
            }

            int qty = entry.getValue();
            Detail detail = Detail.builder()
                    .product(product)
                    .quantity(qty)
                    .unitPrice(product.getPrice())
                    .build();

            purchase.addItem(detail);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
        }
        purchase.setTotal(total);
        return purchase;
    }
}
