import {Component, inject, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CurrencyPipe} from '@angular/common';
import {PaginationComponent} from '../../../shared/pagination';
import {ProductService} from '../../../core/services/product.service';
import {CategoryService} from '../../../core/services/category.service';
import {Page, Product, ProductRequest} from '../../../core/models/product.model';
import {Category} from '../../../core/models/category.model';
import {catchError, of} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {apiErrorMessage} from '../../../shared/api_error';

/**
 * identifie les champs du formulaire de produit pouvant faire l'objet
 * d'une vérification de validité
 */
type FieldName = 'name' | 'price' | 'stock';

/**
 * Composant d'administration des produits.
 * <p>Ce composant permet à l'administrateur de gérer les produits de
 * la boutique. Il propose un affichage paginé des produits, un
 * formulaire de création et de modification ainsi qu'une fonction
 * de suppression</p>
 * <p>Les données sont récupérées et modifiées à travers {@link ProductService}.
 * Les catégories disponibles sont chargées aec {@link CategoryService} afin
 * de pouvoir associer un produit à une catégorie</p>
 */
@Component({
  selector: 'app-admin-products',
  templateUrl: './product.component.html',
  imports: [ReactiveFormsModule, CurrencyPipe, PaginationComponent],
  styleUrl: './product.component.css',
  standalone: true,
})
export class AdminProductComponent {
  /** service responsable des opérations sur les produits */
  private readonly productService = inject(ProductService);

  /** service responsable de la récupération des catégories */
  private readonly categoryService = inject(CategoryService);

  /** résultat paginé contenant les produits affichés */
  protected readonly result = signal<Page<Product> | null>(null);

  /** liste des catégories proposées dans le formulaire */
  protected readonly categories = signal<Category[]>([]);

  /** index de la page actuellement affichée */
  protected readonly pageIndex = signal(0);

  /** indique si le formulaire de cr&ation ou de modification est visible */
  protected readonly formOpen = signal(false);

  /** identifiant du produit en cours de modification, ou null en création */
  protected readonly editingId = signal<number | null>(null);

  /** indique si l'enregistrement d'un produit est en cours */
  protected readonly saving = signal(false);

  /** message d'erreur affiché à la suite d'une opération échouée */
  protected readonly error = signal<string | null>(null);

  /** message de confirmation affiché après une opération réussie */
  protected readonly notice = signal<string | null>(null);

  /**
   * formulaire de création et de modification d'un produit
   * @protected
   */
  protected readonly form = new FormGroup({
    name: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(200)]}),
    description: new FormControl('', {nonNullable: true, validators:[Validators.maxLength(2000)]}),
    price: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    stock: new FormControl<number | null>(0, [Validators.required, Validators.min(0)]),
    imageUrl: new FormControl('', {nonNullable:true}),
    categoryId: new FormControl<number | null>(null)
  });

  /**
   * initialise le composant.
   * les catégories sont chargées en premier pour alimenter le
   * formulaire de produit
   */
  constructor() {
    this.categoryService
      .list()
      .pipe(
        catchError(() => of([] as Category[])),
        takeUntilDestroyed()
      )
      .subscribe((list) => this.categories.set(list));
    this.load(0);
  }

  /**
   * charge une page de produits depuis le backend
   * @param index index de la page à charger, à partir de zéro
   * @protected
   */
  protected load(index: number): void {
    this.pageIndex.set(index);
    this.productService.search({page:index, size:10,sort:'name'}).subscribe({
      next: (page) => {
        if (page.content.length === 0 && index > 0) {
          this.load(index - 1);
          return;
        }
        this.result.set(page);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'Impossible de charger les produits'))
    });
  }

  /**
   * détermine si un champ du formulaire doit afficher une erreur
   * @param name nom du champ à vérifier
   * @protected
   */
  protected invalid(name: FieldName): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

  /**
   * prépare le formulaire pour la création d'un nouveau produit
   * @protected
   */
  protected startNew(): void {
    this.editingId.set(null);
    this.form.reset({name:'', description: '', price: null, stock: 0, imageUrl: '', categoryId: null});
    this.error.set(null);
    this.notice.set(null);
    this.formOpen.set(true);
  }

  /**
   * préremplit le formulaire avec les infos d'un produit existant
   * @param product produit à modifier
   * @protected
   */
  protected edit(product: Product): void {
    this.editingId.set(product.id);
    this.form.reset({
      name: product.name,
      description: product.description ?? '',
      price: product.price,
      stock: product.stock,
      imageUrl: product.imageUrl ?? '',
      categoryId: product.categoryId
    });
    this.error.set(null);
    this.notice.set(null);
    this.formOpen.set(true);
  }

  /**
   * ferme le formulaire et réinitialise le mode d'édition
   * @protected
   */
  protected closeForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
  }

  /**
   * valide et enregistre les informations d'un produit
   * <p>si le formulaire est invalide, tous les champs sont marqués
   * comme visités afin de rendre visible les erreurs de validation</p>
   * <p>les valeurs saisies sont ensuite nettoyées et converties au format
   * attendu par le backend. les champs facultatifs vides sont transmis
   * avec la valeur null</p>
   * @protected
   */
  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const request: ProductRequest = {
      name: v.name.trim(),
      description: v.description.trim() || null,
      price: Number(v.price),
      stock: Number(v.stock),
      imageUrl: v.imageUrl.trim() || null,
      categoryId: v.categoryId
    };
    this.saving.set(true);
    this.error.set(null);

    const id = this.editingId();
    const request$ = id === null ? this.productService.create(request) : this.productService.update(id, request);

    request$.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.closeForm();
        this.notice.set(id === null ? `Produit "${saved.name}" créée` : `Produit "${saved.name}" enregistré`);
        this.load(this.pageIndex());
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(apiErrorMessage(err));
      }
    });
  }

  /**
   * supprime un produit après confirmation de l'admin
   * <p>une confirmation est demandée avant l'envoi de la requête,
   * en cas de succès, un message est affiché et la page courante
   * est actualisée</p>
   * @param product
   * @protected
   */
  protected remove(product: Product): void {
    if (!confirm(`Supprimer le produit "${product.name}" ?`)) return;
    this.error.set(null);
    this.notice.set(null);
    this.productService.delete(product.id).subscribe({
      next: () => {
        this.notice.set(`Produit "${product.name}" supprimé`);
        this.load(this.pageIndex());
      },
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }
}
