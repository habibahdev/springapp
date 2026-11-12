export interface PaymentRequest {
  shippingAddress: string;
  items: {
    productId: number;
    quantity: number;
  }[];
}

export interface CheckoutResponse {
  purchaseId: number;
  sessionId: number;
  checkoutUrl: string;
}

export interface PurchasePaymentStatus {
  id: number;
  status: 'PENDING' | 'PAID' | 'SHIPPED' | 'CANCELLED';
  total: number;
}
