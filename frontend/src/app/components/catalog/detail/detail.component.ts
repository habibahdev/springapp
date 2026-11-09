import {Component, inject, signal} from '@angular/core';
import {CurrencyPipe} from '@angular/common';
import {RouterLink, ActivatedRoute} from '@angular/router';
import {Product} from '../../../core/models/product.model';
import {ProductService} from '../../../core/services/product.service';
import {CartService} from '../../../core/services/cart.service';
import {toSignal} from '@angular/core/rxjs-interop';
import {catchError, of, switchMap, map} from 'rxjs';

/**
 * composant d'affichage détaillé d'un produit
 * <p>ce composant permet de consulter les infos complètes d'un
 * produit sélectionné dans le catalogue : nom, description, catégorie,
 * prix, image et disponibilité en stock</p>
 * <p>il permet également de choisir une quantité et d'ajouter le
 * produit au panier, dans la limite du stock disponible</p>
 * <p>l'identifiant du produit est récupéré depuis les paramètres
 * de la route angular. les données sont ensuite chargées via
 * {@link ProductService} et la gestion su panier est assurée par
 * {@link CartService}</p>
 */
@Component({
  selector: 'app-detail',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './detail.component.html',
  standalone: true,
  styleUrl: './detail.component.css'
})
export class DetailComponent {
  /** route active permettant de récupérer l'identifiant du produit */
  private readonly route = inject(ActivatedRoute);

  /** service responsable de la récupération des infos du produit */
  private readonly productService = inject(ProductService);

  /** service permettant de consulter et de modifier le panier */
  protected readonly cart = inject(CartService);

  /**
   * produit actuellement affiché
   * le signal peut contenir trois états :
   * <ul>
   *   <li>{@code undefined} : le changement du produit est en cours</li>
   *   <li>{@code null} : le produit est introuvable ou une erreur est survenue</li>
   *   <li>{@link Product} : le produit a été correctement chargé</li>
   * </ul>
   * <p>à chaque changement du paramètre {@code id} de la route, une route
   * requête est envoyée au backend</p>
   * @protected
   */
  protected readonly product = toSignal(
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((id) => this.productService.get(id).pipe(catchError(() => of(null)))),
    ),
    {initialValue: undefined}
  );

  /** quantité de produit sélectionnée par l'utilisateur */
  protected readonly quantity = signal(1);

  /** indique si le produit vient d'être ajouté au panier */
  protected readonly added = signal(false);

  /**
   * calcule la quantité restante pouvant être ajoutée au panier
   * <p>la quantité déjà présente dans le panier est soustraite du stock
   * disponible. le résultat ne peut jamais être négatif
   * @param product produit concerné
   * @protected
   */
  protected remaining(product: Product): number {
    return Math.max(product.stock - this.cart.quantityOf(product.id), 0);
  }

  /**
   * augmente la quantité sélectionnée sans dépasser le stock restant
   * @param max quantité maximale autorisée
   * @protected
   */
  protected increment(max: number): void {
    this.quantity.update((q) => Math.min(q + 1, max));
  }

  /**
   * diminue la quantité sélectionnée sans descendre sous une unité
   * @protected
   */
  protected decrement(): void {
    this.quantity.update((q) => Math.max(q - 1, 1));
  }

  /**
   * ajoute le produit sélectionné au panier
   * <p>la quantité ajoutée est limitée à la quantité restante en stock.
   * après l'ajout, la quantité sélectionnée est réinitialisée à une unité
   * et le message de confirmation est activé
   * @param product produit à ajouter au panier
   * @protected
   */
  protected addToCart(product: Product): void {
    this.cart.add(product, Math.min(this.quantity(), this.remaining(product)));
    this.quantity.set(1);
    this.added.set(true);
  }
}
