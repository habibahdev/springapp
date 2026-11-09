import {Component, computed, inject, signal} from '@angular/core';
import {CurrencyPipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import {PaginationComponent} from '../../../shared/pagination';
import {ProductService} from '../../../core/services/product.service';
import {CategoryService} from '../../../core/services/category.service';
import {CartService} from '../../../core/services/cart.service';
import {Category} from '../../../core/models/category.model';
import {Page, Product, ProductQuery} from '../../../core/models/product.model';
import {catchError, debounceTime, of, switchMap, tap} from 'rxjs';
import {takeUntilDestroyed, toObservable} from '@angular/core/rxjs-interop';
import {apiErrorMessage} from '../../../shared/api_error';

/**
 * composant d'affichage du catalogue de produits
 * <p>ce composant permet aux visiteurs et aux utilisateurs de consulter
 * les produits disponibles dans la boutique</p>
 * il propose plusieurs fonctionnalités :
 * <ul>
 *   <li>recherche textuelle de produits</li>
 *   <li>filtrage par catégorie</li>
 *   <li>tri par nom ou par prix</li>
 *   <li>pagination des résultats</li>
 *   <li>ajout de produits au panier</li>
 * </ul>
 * <p>les données sont récupérées via {@link ProductService} et {@link CategoryService}
 * la gestion du panier est déléguée à {@link CartService}</p>
 */
@Component({
  selector: 'app-products',
  imports: [CurrencyPipe, RouterLink, PaginationComponent],
  standalone: true,
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent {
  /** service de consultation et de recherche des produits */
  private readonly productService = inject(ProductService);

  /** service permettant de récupérer les catégories disponibles */
  private readonly categoryService = inject(CategoryService);

  /** service responsable de la gestion du panier */
  protected readonly cart = inject(CartService);

  /** liste des catégories disponibles pour le filtrage */
  protected readonly categories = signal<Category[]>([]);

  /** résultat paginé de la recherche de produits */
  protected readonly result = signal<Page<Product> | null>(null);

  /** indique si une requête de chargement des produits est en cours */
  protected readonly loading = signal(true);

  /** message d'erreur lié au chargement du catalogue */
  protected readonly error = signal<string | null>(null);

  /** terme saisi dans le champ de recherche */
  protected readonly search = signal('');

  /** identifiant de la catégorie sélectionnée, ou null si aucun filtre */
  protected readonly categoryId = signal<number | null>(null);

  /** critère de tri appliqué aux résultats du catalogue */
  protected readonly sort = signal<'name' | 'price'>('name');

  /** index de la page actuellement consultée */
  protected readonly pageIndex = signal(0);

  /**
   * indique si au moins un filtre de recherche est actif
   * <p>un filtre est considéré comme actif lorsqu'un terme de recherche
   * est renseigné ou qu'une catégorie est sélectionnée</p>
   * @protected
   */
  protected readonly hasFilters = computed(
    () => this.search().trim() !== '' || this.categoryId() !== null
  );

  /**
   * construit les critères de recherche transmis au backend
   * @private
   */
  private readonly query = computed<ProductQuery>(() => ({
    q: this.search().trim() || undefined,
    categoryId: this.categoryId() ?? undefined,
    sort: this.sort(),
    page: this.pageIndex(),
    size: 12
  }));

  /**
   * initialise le composant et configure le chargement automatique du catalogue
   */
  constructor() {
    this.categoryService
      .list()
      .pipe(
        catchError(() => of([] as Category[])),
        takeUntilDestroyed()
      )
      .subscribe((categories) => this.categories.set(categories));

    toObservable(this.query)
      .pipe(
        debounceTime(250),
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((query) =>
          this.productService.search(query).pipe(
            catchError((err) => {
              this.error.set(apiErrorMessage(err, 'Impossible de charger les produits'));
              return of(null);
            }),
          )
        ),
        takeUntilDestroyed(),
      )
      .subscribe((page) => {
        this.result.set(page);
        this.loading.set(false);
      });
  }

  /**
   * met à jour le terme de recherche
   * @param value nouveau terme de recherche
   * @protected
   */
  protected onSearch(value: string): void {
    this.search.set(value);
    this.pageIndex.set(0);
  }

  /**
   * met à jour la catégorie utilisée comme filtre
   * @param value identifiant de la catégorie sélectionnée
   * @protected
   */
  protected onCategory(value: string): void {
    this.categoryId.set(value ? Number(value) : null);
    this.pageIndex.set(0);
  }

  /**
   * modifie le critère de tri des produits
   * @param value critère de tri sélectionné
   * @protected
   */
  protected onSort(value: string): void {
    this.sort.set(value === 'price' ? 'price' : 'name');
    this.pageIndex.set(0);
  }

  /**
   * navigue vers une page précise du catalogue
   * @param index index de la page à afficher
   * @protected
   */
  protected goTo(index: number): void {
    this.pageIndex.set(index);
    window.scrollTo({top: 0, behavior: 'smooth'});
  }

  /**
   * réinitialise les filtres de recherche
   * @protected
   */
  protected resetFilters(): void {
    this.search.set('');
    this.categoryId.set(null);
    this.pageIndex.set(0);
  }

  /**
   * ajoute un produit au panier
   * @param product produit à ajouter
   * @protected
   */
  protected addToCart(product: Product): void {
    this.cart.add(product);
  }

  /**
   * détermine le libellé du bouton d'ajout au panier
   * @param product produit concerné
   * @protected
   */
  protected addLabel(product: Product): string {
    if (product.stock === 0) return 'Ruputure de stock';
    if (this.cart.quantityOf(product.id) >= product.stock) return 'Quantité max atteinte';
    return 'Ajouter au panier';
  }

  /**
   * vérifie si un produit peut être ajouté au panier
   * @param product produit à vérifier
   * @protected
   */
  protected canAdd(product: Product): boolean {
    return product.stock > 0 && this.cart.quantityOf(product.id) < product.stock;
  }
}
