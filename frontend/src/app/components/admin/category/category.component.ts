import {Component, inject, signal} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {CategoryService} from '../../../core/services/category.service';
import {Category} from '../../../core/models/category.model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {apiErrorMessage} from '../../../shared/api_error';

/**
 * Composant d'administration des catégories de produits.
 * <p>Ce composant permet à l'administrateur de gérer les catégories
 * disponibles dans la boutique. Il assure le chargement de la liste
 * des catégories ainsi que les opérations de création, de modification
 * et de suppression</p>
 * <p>La gestion du formulaire repose sur les Reactive Forms d'angular
 * et les données sont synchronisées avec le backend à travers
 * {@link CategoryService}</p>
 * <p>Les états d'interface sont gérés à l'aide des signaux</p>
 */
@Component({
  selector: 'app-admin-categories',
  templateUrl: './category.component.html',
  imports: [ReactiveFormsModule, FormsModule],
  standalone: true,
  styleUrl: './category.component.css'
})
export class AdminCategoriesComponent {
  /** service permettant d'effectuer les opérations sur les catégories */
  private readonly categoryService = inject(CategoryService);

  /** liste des catégories récupérées depuis le backend */
  protected readonly categories = signal<Category[] | null>(null);

  /** identifiant de la catégorie en cours de modification, ou null en création */
  protected readonly editingId = signal<number | null>(null);

  /** message d'erreur destiné à être affiché dans l'interface */
  protected readonly error = signal<string | null>(null);

  /** indique si une opération de création ou de modification est en cours */
  protected readonly saving = signal(false);

  /**
   * champ de saisie du nom de la catégorie
   * @protected
   */
  protected readonly name = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(100)]
  });

  /**
   * initialise le composant et déclenche le chargement des catégories
   */
  constructor() {
    this.load();
  }

  /**
   * récupère la liste des catégories depuis le backend
   * @private
   */
  private load(): void {
    this.categoryService
      .list()
      .pipe(takeUntilDestroyed())
      .subscribe({
        next: (list) => this.categories.set(list),
        error: (err) => this.error.set(apiErrorMessage(err))
      });
  }

  /**
   * prépare le formulaire pour modifier une catégorie existante
   * @param category catégorie à modifier
   * @protected
   */
  protected edit(category: Category): void {
    this.editingId.set(category.id);
    this.name.setValue(category.name);
    this.error.set(null);
  }

  /**
   * annule l'opération en cours et réinitialise le formulaire
   * @protected
   */
  protected cancel(): void {
    this.editingId.set(null);
    this.name.reset('');
  }

  /**
   * enregistre une catégorie dans le backend
   * @protected
   */
  protected save(): void {
    const value = this.name.value.trim();
    if (!value) {
      this.name.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const id = this.editingId();
    const request$= id === null ? this.categoryService.create({name:value}) : this.categoryService.update(id, {name: value});
    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancel();
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(apiErrorMessage(err));
      }
    });
  }

  /**
   * supprime une categorie après confirmation de l'administrateur
   * @param category catégorie à supprimer
   * @protected
   */
  protected remove(category: Category): void {
    if (!confirm(`Supprimer la catégorie "${category.name}" ?`)) return;
    this.error.set(null);
    this.categoryService.delete(category.id).subscribe({
      next: () => this.load(),
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }
}
