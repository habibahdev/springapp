import {Component, OnInit} from '@angular/core';
import {RouterLink} from '@angular/router';

/**
 * Composant affiché lorsque l'utilisateur revient de stripe, sans avoir
 * finalisé son paiement. Il nettoie l'identifiant de la commande temporairement
 * conservé dans le sessionStorage. Le panier est conservé afin que l'utilisateur
 * puisse reprendre son parcours d'achat.
 */
@Component({
  selector: 'app-payment-cancel',
  imports: [RouterLink],
  templateUrl: './cancel.component.html'
})
export class PaymentCancelComponent implements OnInit {

  /**
   * Initialise la page d'annulation du paiement. Supprime l'identifiant ne
   * signifie pas nécessairement que la commande a déjà été annulée côté backend : son
   * statut dépend notamment du traitement des événements stripe.
   */
  ngOnInit(): void {
    sessionStorage.removeItem('springapp_pending_purchase_id');
  }
}
