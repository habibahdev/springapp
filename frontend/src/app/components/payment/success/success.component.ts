import {Component, inject, OnInit, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {CartService} from '../../../core/services/cart.service';
import {PurchaseService} from '../../../core/services/purchase.service';
import {PurchasePaymentStatus} from '../../../core/models/payment.model';
import {switchMap, take, takeWhile, timer} from 'rxjs';

/**
 * Composant affiché après la redirection de l'utilisateur depuis la page
 * de paiement stripe checkout.
 * Il interroge périodiquement le backend afin de connaître le statut réel
 * de la commande. La confirmation du paiement repose sur le traitement
 * du webhook stripe côté serveur, et non sur la simple redirection vers cette page.
 * Lorsque le paiement est confirmé, le panier est vidé et l'identifiant temporaire
 * de la commande est supprimé
 */
@Component({
  selector: 'app-payment-success',
  imports: [RouterLink],
  templateUrl: './success.component.html'
})
export class PaymentSuccessComponent implements OnInit {
  /** service permettant de consulter les commandes de l'utilisateur */
  private readonly purchaseService = inject(PurchaseService);

  /** service permettant de consulter et de vider le panier */
  private readonly cart = inject(CartService);

  /** contient les infos de paiement de la commande consultée */
  protected readonly purchase = signal<PurchasePaymentStatus | null>(null);

  /** indique si la vérification du statut de la commande est en cours */
  protected readonly checking = signal(true);

  /** indique si le paiement est toujours en attente de confirmation */
  protected readonly waiting = signal(false);

  /** contient un éventuel message d'erreur lors de la vérification */
  protected readonly error = signal<string | null>(null);

  /**
   * Initialise la vérification du paiement. Récupère l'identifiant de la commande depuis
   * le sessionStorage, vérifie sa validité, puis interroge le backend toutes les deux secondes.
   * La vérification s'arrête lorsque le statut de la commande n'est plus PENDING ou lorsque le
   * nombre maximal de quinze requêtes est atteint.
   * Si le paiement est confirmé (PAID ou SHIPPED), le panier est vidé. Si la commande est annulée,
   * son identifiant temporaire est supprimé.
   * En cas d'erreur http, un message d'erreur est affiché.
   */
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
