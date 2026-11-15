# Scénarios de test

[Retour à la documentation](index.md)

---

## Sommaire

- [1. Préparation de l'environnement](#préparation-de-lenvironnement)
- [2. Authentification](#authentification)
- [3. Consultation des produits](#consultation-des-produits)
- [4. Création d'une commande avec Stripe](#création-dune-commande-avec-stripe)
- [5. Confirmation de paiement](#confirmation-de-paiement)
- [6. Consultation des commandes](#consultation-des-commandes)
- [7. Tests de sécurité](#tests-de-sécurité)

---

## Préparation de l'environnement

Avant de commencer, vérifier que les services sont démarrés :

```bash
docker compose up -d
```

Vérifier que le backend est accessible :

```bash
curl http://localhost:8080
```

L'API est accessible à l'adresse :

```text
http://localhost:8080/api
```

Pour faciliter les tests, définir les variables suivantes dans le terminal :

```bash
export API_URL="http://localhost:8080/api"
export USER_TOKEN=""
export ADMIN_TOKEN=""
```

Les tokens seront renseignés après les opérations de connexion.

**Prérequis :**
- Un compte utilisateur avec le role `USER`
- Un compte administrateur avec le role `ADMIN`
- Des catégories, des produits déjà présents dans la base de données
- Stripe configuré en mode test

## Authentification

### Inscription d'un utilisateur

**Requête :**

```http request
POST /api/auth/register
```

**cURL :**

```bash
curl -i -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Client Test",
    "email": "client.test@example.com",
    "password": "Password123!"
  }'
```

### Connexion d'un utilisateur

**Requête :**

```http
POST /api/auth/login
```

**cURL :**

```bash
curl -i -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client.test@example.com",
    "password": "Password123!"
  }'
```

Récupérer le JWT retourné et le stocker :

```bash
export USER_TOKEN="COLLER_LE_JWT_ICI"
```

### Connexion d'un administrateur

**Requête :**

```bash
curl -i -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPassword123!"
  }'
```

Stocker le token administrateur :

```bash
export ADMIN_TOKEN="COLLER_LE_JWT_ICI"
```

## Consultation des produits

Les produits sont accessibles publiquement.

### Consulter tous les produits

```bash
curl -i "$API_URL/products"
```

### Rechercher un produit

```bash
curl -i "$API_URL/products?q=ordinateur"
```

### Filtrer par catégorie

```bash
curl -i "$API_URL/products?categoryId=1"
```

### Tester la pagination

```bash
curl -i "$API_URL/products?page=0&size=5&sort=price"
```

## Création d'une commande avec Stripe

La création d'une commande s'effectue à travers une session Stripe Checkout.

L'utilisateur doit être authentifié avec un compte `USER`.

### Créer une session de paiement

**Requête :**

```http
POST /api/payments/checkout
```

**cURL :**

```bash
curl -i -X POST "$API_URL/payments/checkout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "shippingAddress": "10 rue de la Paix, 76000 Rouen",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

Exemple de réponse :

```json
{
  "purchaseId": 1,
  "sessionId": "cs_test_...",
  "checkoutUrl": "https://checkout.stripe.com/..."
}
```

Enregistrer l'identifiant de la commande :

```bash
export PURCHASE_ID=1
```

Ouvrir ensuite la valeur de `checkoutUrl` dans un navigateur.

### Tester un stock insuffisant

Envoyer une quantité supérieure au stock disponible :

```bash
curl -i -X POST "$API_URL/payments/checkout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "shippingAddress": "10 rue de la Paix, 76000 Rouen",
    "items": [
      {
        "productId": 1,
        "quantity": 99999
      }
    ]
  }'
```

### Tester un produit inexistant

```bash
curl -i -X POST "$API_URL/payments/checkout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "shippingAddress": "10 rue de la Paix, 76000 Rouen",
    "items": [
      {
        "productId": 999999,
        "quantity": 1
      }
    ]
  }'
```

## Confirmation du paiement

### Démarrer Stripe CLI

Dans un terminal séparé :

```bash
stripe login
```

Puis lancer l'écoute des événements :

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

Stripe CLI affiche une clé de signature commençant par `whsec_`.

Cette clé doit correspondre à la variable `STRIPE_WEBHOOK_SECRET` de l'environnement du backend.

### Effectuer un paiement de test

Ouvrir l'URL `checkoutUrl` obtenue précédemment.

Utiliser les informations de carte de test Stripe :

| Champ | Valeur |
|---|---|
| Numéro de carte | `4242 4242 4242 4242` |
| Date d'expiration | Une date future |
| CVC | N'importe quelle valeur à trois chiffres |
| Code postal | N'importe quelle valeur |


### Vérifier la réception du webhook

Consulter le terminal dans lequel Stripe CLI écoute les événements.

Un événement `checkout.session.completed` doit être transmis au backend.

Consulter également les logs :

```bash
docker compose logs -f backend
```

## 6. Consultation des commandes

### Consulter ses commandes

**Requête :**

```http
GET /api/purchases/me
```

**cURL :**

```bash
curl -i "$API_URL/purchases/me" \
  -H "Authorization: Bearer $USER_TOKEN"
```


### Consulter une commande précise

**Requête :**

```http
GET /api/purchases/me/{id}
```

**cURL :**

```bash
curl -i "$API_URL/purchases/me/$PURCHASE_ID" \
  -H "Authorization: Bearer $USER_TOKEN"
```


### Consulter toutes les commandes en tant qu'administrateur

```bash
curl -i "$API_URL/purchases" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```


### Modifier le statut d'une commande

Cette opération est réservée à l'administrateur.

```bash
curl -i -X PATCH "$API_URL/purchases/$PURCHASE_ID/status?status=SHIPPED" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```


## 7. Tests de sécurité

Ces scénarios permettent de vérifier que les règles Spring Security sont correctement appliquées.

| Scénario | Requête | Résultat attendu |
|---|---|---|
| Consulter les produits sans token | GET `/api/products` | HTTP 200 |
| Consulter ses commandes sans token | GET `/api/purchases/me` | HTTP 401 |
| Créer une session Stripe sans token | POST `/api/payments/checkout` | HTTP 401 |
| Créer une session Stripe avec un ADMIN | POST `/api/payments/checkout` | HTTP 403 |
| Consulter toutes les commandes avec un USER | GET `/api/purchases` | HTTP 403 |
| Modifier une commande avec un USER | PATCH `/api/purchases/{id}/status` | HTTP 403 |
| Consulter la commande d'un autre utilisateur | GET `/api/purchases/me/{id}` | HTTP 404 |

### Tester l'accès refusé sans authentification

```bash
curl -i "$API_URL/purchases/me"
```


### Tester l'accès interdit à un utilisateur classique

```bash
curl -i "$API_URL/purchases" \
  -H "Authorization: Bearer $USER_TOKEN"
```

### Tester l'accès interdit à l'administrateur pour le checkout

```bash
curl -i -X POST "$API_URL/payments/checkout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "shippingAddress": "10 rue de la Paix, 76000 Rouen",
    "items": [
      {
        "productId": 1,
        "quantity": 1
      }
    ]
  }'
```
