import {computed, effect, Injectable, signal} from '@angular/core';
import {CartLine} from '../models/cart.model';
import {Product} from '../models/product.model';
import {Observable} from 'rxjs';
import {DetailRequest} from '../models/purchase.model';

/** Clé utilisée pour enregistrer le panier dans le localStorage */
const STORAGE_KEY = 'springapp_cart';

/**
 * Service responsable de la gestion du panier d'achat.
 * Il permet de :
 * <ul>
 *   <li>ajouter un produit au panier</li>
 *   <li>modifier la quantité d'un produit</li>
 *   <li>supprimer un produit du panier</li>
 *   <li>calculer le nombre d'articles et le montant total</li>
 * </ul>
 *
 */
@Injectable({providedIn: 'root'})
export class CartService {
  /** signal contenant les lignes du panier. */
  private readonly _lines = signal<CartLine[]>(this.load());

  /** contenu du panier en lecture seule */
  readonly lines = this._lines.asReadonly();

  /** nombre total d'articles dans le panier */
  readonly count = computed(() => this._lines().reduce((n, l) => n + l.quantity, 0));

  /** montant total du panier */
  readonly total = computed(() => this._lines().reduce((sum, l) => sum + l.price * l.quantity, 0));

  /** indique si le panier est vide ou non */
  readonly isEmpty = computed(() => this._lines().length === 0);

  /**
   * Initialise le service et met en place la sauvegarde automatique du panier
   */
  constructor() {
    effect(() => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this._lines()));
      } catch {}
    });
  }

  /**
   * ajoute un produit au panier. Si le produit existe, sa quantité est augmentée.
   * dans tous les cas, la quantité ne peut pas dépasser le stock disponible.
   * @param product produit à ajouter au panier.
   * @param quantity quantité à ajouter (1 par défaut)
   */
  add(product: Product, quantity = 1): void {
    if (product.stock <= 0) return;
    this._lines.update((lines) => {
      const existing = lines.find((l) => l.productId === product.id);
      if (existing) {
        return lines.map((l) =>
          l.productId === product.id
          ? {...l, stock: product.stock, quantity: Math.min(l.quantity + quantity, product.stock)}
          : l,
        );
      }
      return [
        ...lines,
        {
          productId: product.id,
          name: product.name,
          price: product.price,
          imageUrl: product.imageUrl,
          stock: product.stock,
          quantity: Math.min(quantity, product.stock)
        }
      ];
    });
  }

  /**
   * Récupère la quantité d'un produit présent dans le panier.
   * @param productId identifiant du produit recherché
   */
  quantityOf(productId: number): number {
    return this._lines().find((l) => l.productId === productId)?.quantity ?? 0;
  }

  /**
   * modifie la quantité d'un produit dans le panier. Si la quantité est inférieure ou égale
   * à zéro, le produit est supprimé
   * @param productId identifiant du produit à modifier
   * @param quantity nouvelle quantité à soumettre
   */
  setQuantity(productId: number, quantity: number): void {
    if (quantity <= 0) {
      this.remove(productId);
      return;
    }
    this._lines.update((lines) =>
    lines.map((l) =>
    l.productId === productId ? {...l, quantity: Math.min(quantity, l.stock)} : l));
  }

  /**
   * supprime un produit du panier.
   * @param productId identifiant du produit à supprimer
   */
  remove(productId: number): void {
    this._lines.update((lines) => lines.filter((l) => l.productId !== productId));
  }

  /**
   * vide complètement le panier.
   */
  clear(): void {
    this._lines.set([]);
  }

  /**
   * transforme le contenu du panier en liste de détails de commande.
   * suels l'identifiant du produit et sa quantité sont conservés.
   */
  toDetail(): DetailRequest[] {
    return this._lines().map((l) => ({productId: l.productId, quantity: l.quantity}));
  }

  /**
   * charge le contenu du panier depuis le localStorage.
   * si aucune donnée n'est enregistrée ou si une erreur survient pendant la lecture,
   * un panier vide est retourné
   * @private
   */
  private load(): CartLine[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as CartLine[]) : [];
    } catch {
      return [];
    }
  }
}
