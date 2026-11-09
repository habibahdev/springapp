import {Component, inject, signal} from '@angular/core';
import {CurrencyPipe, DatePipe} from '@angular/common';
import {PurchaseService} from '../../../core/services/purchase.service';
import {PURCHASE_STATUS_BADGE, PURCHASE_STATUS_LABEL, PURCHASE_STATUSES} from '../../../shared/purchase_status';
import {Purchase, PurchaseStatus} from '../../../core/models/purchase.model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {apiErrorMessage} from '../../../shared/api_error';

/**
 * Composant d'administration des commandes
 * <p>Ce composant permet à l'administrateur de consulter les commandes
 * passées par les clients de la boutique</p>
 * <p>il propose l'affichage de la liste des commandes, la consultation
 * du détail de chaque commande et la modification de leur statut</p>
 * <p>les opérations sont effectuées à travers {@link PurchaseService}.
 * les dates et les montants sont formatés grâce aux pipes angular</p>
 */
@Component({
  selector: 'app-admin-purchases',
  templateUrl: './purchase.component.html',
  imports: [CurrencyPipe, DatePipe],
  styleUrl: './purchase.component.css',
  standalone: true,
})
export class AdminPurchasesComponent {
  /** service responsable de la consultation et de la modification des commandes */
  private readonly purchaseService = inject(PurchaseService);

  /** liste des statuts de commande autorisés */
  protected readonly statuses = PURCHASE_STATUSES;

  /** association entre les statuts et leurs libellés destinés à l'affichage */
  protected readonly statusLabel = PURCHASE_STATUS_LABEL;

  /** association entre les status et leurs classes css de badge */
  protected readonly statusBadge = PURCHASE_STATUS_BADGE;

  /** liste des commandes récupérées depuis le backend */
  protected readonly purchases = signal<Purchase[] | null>(null);

  /** identifiant de la commande dont le détail est actuellement affiché */
  protected readonly expandedId = signal<number | null>(null);

  /** message d'erreur destinée à être affiché dans l'interface */
  protected readonly error = signal<string | null>(null);

  /**
   * initialise le composant et charge les commandes
   */
  constructor() {
    this.load();
  }

  /**
   * récupère toutes les commandes depuis le backend
   * <p>les commandes sont triées par date de création décroissante</p>
   * @private
   */
  private load(): void {
    this.purchaseService
      .all()
      .pipe(takeUntilDestroyed())
      .subscribe({
        next: (purchases) => this.purchases.set([...purchases].sort((a, b) => b.createdAt.localeCompare(a.createdAt))),
        error: (err) => this.error.set(apiErrorMessage(err, 'Impossible de charger les commandes'))
      });
  }

  /**
   * affiche ou masque le détail d'une commande
   * <p>si la commande sélectionnée est déjà développée, son détail est masqué,
   * sinon son identifiant devient l'identifiant de la commande développée</p>
   * @param id
   * @protected
   */
  protected toggle(id: number): void {
    this.expandedId.update((current) => (current === id ? null : id));
  }

  /**
   * modifie le statut d'une commande
   * <p>la valeur reçue est vérifiée par rapport à la liste des statuts
   * autorisés. si elle est invalide ou identique au statut actuel,
   * aucune requête n'est envoyée</p>
   * @param purchase
   * @param value
   * @protected
   */
  protected changeStatus(purchase: Purchase, value: string): void {
    const status = PURCHASE_STATUSES.find((s) => s === value);
    if (!status || status === purchase.status) return;
    this.error.set(null);
    this.purchaseService.updateStatus(purchase.id, status as PurchaseStatus).subscribe({
      next: (updated) => this.purchases.update((list) => list?.map((o) => (o.id === updated.id ? updated : o)) ?? null),
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.load();
      }
    });
  }
}
