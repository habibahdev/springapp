import {PurchaseStatus} from '../core/models/purchase.model';

export const PURCHASE_STATUSES: PurchaseStatus[] = ['PENDING', 'PAID', 'SHIPPED', 'CANCELLED'];

export const PURCHASE_STATUS_LABEL: Record<PurchaseStatus, string> = {
  PENDING: 'En attente',
  PAID: 'Payée',
  SHIPPED: 'Expédiée',
  CANCELLED: 'Annulée'
};

export const PURCHASE_STATUS_BADGE: Record<PurchaseStatus, string> = {
  PENDING: 'text-bg-warning',
  PAID: 'text-bg-info',
  SHIPPED: 'text-bg-success',
  CANCELLED: 'text-bg-secondary'
};
