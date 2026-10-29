export type PurchaseStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'CANCELLED';

export interface DetailRequest {
  productId: number;
  quantity: number;
}

export interface PurchaseRequest {
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  items: DetailRequest[];
}

export interface Detail {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface Purchase {
  id: number;
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  status: PurchaseStatus;
  createdAt: string;
  total: number;
  items: Detail[];
}
