/**
 * Représente les données envoyées au backend pour initialiser le paiement d'une commande.
 * L'adresse de livraison et les articles du panier sont transmis au backend, qui se charge
 * de vérifier les produits, les quantités, les prix et la disponibilité du stock.
 */
export interface PaymentRequest {
  shippingAddress: string;
  items: {
    productId: number;
    quantity: number;
  }[];
}

/**
 * Représente la réponse retournée par le backend après la création d'une session stripe.
 * Cette réponse permet au frontend de retrouver la commande créée et de rediriger
 * l'utilisateur vers la page de paiement stripe.
 */
export interface CheckoutResponse {
  purchaseId: number;
  sessionId: string;
  checkoutUrl: string;
}

/**
 * Représente les infos essentielles d'une commande nécessaire au suivi de son paiement.
 */
export interface PurchasePaymentStatus {
  id: number;
  status: 'PENDING' | 'PAID' | 'SHIPPED' | 'CANCELLED';
  total: number;
}
