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

type FieldName = 'name' | 'price' | 'stock';

@Component({
  selector: 'app-admin-products',
  templateUrl: './product.component.html',
  imports: [ReactiveFormsModule, CurrencyPipe, PaginationComponent],
  styleUrl: './product.component.css',
  standalone: true,
})
export class AdminProductComponent {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);

  protected readonly result = signal<Page<Product> | null>(null);
  protected readonly categories = signal<Category[]>([]);
  protected readonly pageIndex = signal(0);
  protected readonly formOpen = signal(false);
  protected readonly editingId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly notice = signal<string | null>(null);

  protected readonly form = new FormGroup({
    name: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(200)]}),
    description: new FormControl('', {nonNullable: true, validators:[Validators.maxLength(2000)]}),
    price: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    stock: new FormControl<number | null>(0, [Validators.required, Validators.min(0)]),
    imageUrl: new FormControl('', {nonNullable:true}),
    categoryId: new FormControl<number | null>(null)
  });

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

  protected invalid(name: FieldName): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

  protected startNew(): void {
    this.editingId.set(null);
    this.form.reset({name:'', description: '', price: null, stock: 0, imageUrl: '', categoryId: null});
    this.error.set(null);
    this.notice.set(null);
    this.formOpen.set(true);
  }

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

  protected closeForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
  }

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
