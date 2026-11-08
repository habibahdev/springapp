import {Component, inject, signal} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {CategoryService} from '../../../core/services/category.service';
import {Category} from '../../../core/models/category.model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {apiErrorMessage} from '../../../shared/api_error';

@Component({
  selector: 'app-admin-categories',
  templateUrl: './category.component.html',
  imports: [ReactiveFormsModule, FormsModule],
  standalone: true,
  styleUrl: './category.component.css'
})
export class AdminCategoriesComponent {
  private readonly categoryService = inject(CategoryService);
  protected readonly categories = signal<Category[] | null>(null);
  protected readonly editingId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);

  protected readonly name = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(100)]
  });

  constructor() {
    this.load();
  }

  private load(): void {
    this.categoryService
      .list()
      .pipe(takeUntilDestroyed())
      .subscribe({
        next: (list) => this.categories.set(list),
        error: (err) => this.error.set(apiErrorMessage(err))
      });
  }

  protected edit(category: Category): void {
    this.editingId.set(category.id);
    this.name.setValue(category.name);
    this.error.set(null);
  }

  protected cancel(): void {
    this.editingId.set(null);
    this.name.reset('');
  }

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

  protected remove(category: Category): void {
    if (!confirm(`Supprimer la catégorie "${category.name}" ?`)) return;
    this.error.set(null);
    this.categoryService.delete(category.id).subscribe({
      next: () => this.load(),
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }
}
