import {Component, inject, OnInit, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {CartService} from '../../../core/services/cart.service';
import {PurchaseService} from '../../../core/services/purchase.service';
import {PurchasePaymentStatus} from '../../../core/models/payment.model';
import {switchMap, take, takeWhile, timer} from 'rxjs';

@Component({
  selector: 'app-payment-success',
  imports: [RouterLink],
  templateUrl: './success.component.html'
})
export class PaymentSuccessComponent implements OnInit {
  private readonly purchaseService = inject(PurchaseService);
  private readonly cart = inject(CartService);

  protected readonly purchase = signal<PurchasePaymentStatus | null>(null);
  protected readonly checking = signal(true);
  protected readonly waiting = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const storedId = sessionStorage.getItem('springapp_pending_purchase_id');
    const purchaseId = Number(storedId);

    if (!storedId || !Number.isInteger(purchaseId) || purchaseId <= 0) {
      this.checking.set(false);
      this.error.set(
        'Impossible de retrouver votre commande. Consultez votre historique de commandes.'
      );
      return;
    }

    timer(0, 2000).pipe(
      switchMap(() => this.purchaseService.findMyPurchaseById(purchaseId)),
      takeWhile(
        (result) => result.status === 'PENDING',
        true
      ),
      take(15)
    ).subscribe({
      next: (result) => {
        this.purchase.set(result);

        if (result.status === 'PAID' || result.status === 'SHIPPED') {
          this.cart.clear();
          sessionStorage.removeItem('springapp_pending_purchase_id');
        }

        if (result.status === 'CANCELLED') {
          sessionStorage.removeItem('springapp_pending_purchase_id');
        }
      },
      error: () => {
        this.error.set(
          'Nous ne parvenons pas à vérifier le paiement de votre commande.'
        );
        this.checking.set(false);
      },
      complete: () => {
        this.checking.set(false);

        if (this.purchase()?.status === 'PENDING') {
          this.waiting.set(true);
        }
      }
    });
  }
}
