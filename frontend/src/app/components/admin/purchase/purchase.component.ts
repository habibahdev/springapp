import {Component, inject, signal} from '@angular/core';
import {CurrencyPipe, DatePipe} from '@angular/common';
import {PurchaseService} from '../../../core/services/purchase.service';
import {PURCHASE_STATUS_BADGE, PURCHASE_STATUS_LABEL, PURCHASE_STATUSES} from '../../../shared/purchase_status';
import {Purchase, PurchaseStatus} from '../../../core/models/purchase.model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {apiErrorMessage} from '../../../shared/api_error';

@Component({
  selector: 'app-admin-purchases',
  templateUrl: './purchase.component.html',
  imports: [CurrencyPipe, DatePipe],
  styleUrl: './purchase.component.css',
  standalone: true,
})
export class AdminPurchasesComponent {
  private readonly purchaseService = inject(PurchaseService);
  protected readonly statuses = PURCHASE_STATUSES;
  protected readonly statusLabel = PURCHASE_STATUS_LABEL;
  protected readonly statusBadge = PURCHASE_STATUS_BADGE;
  protected readonly purchases = signal<Purchase[] | null>(null);
  protected readonly expandedId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  private load(): void {
    this.purchaseService
      .all()
      .pipe(takeUntilDestroyed())
      .subscribe({
        next: (purchases) => this.purchases.set([...purchases].sort((a, b) => b.createdAt.localeCompare(a.createdAt))),
        error: (err) => this.error.set(apiErrorMessage(err, 'Impossible de charger les commandes'))
      });
  }

  protected toggle(id: number): void {
    this.expandedId.update((current) => (current === id ? null : id));
  }

  protected changeStatus(purchase: Purchase, value: string): void {
    const status = PURCHASE_STATUSES.find((s) => s === value);
    if (!status || status === purchase.status) return;
    this.error.set(null);
    this.purchaseService.updateStatus(purchase.id, status as PurchaseStatus).subscribe({
      next: (updated) => this.purchases.update((list) => list?.map((o) => (o.id === updated.id ? updated : o)) ?? null),
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.load()
      }
    });
  }
}
