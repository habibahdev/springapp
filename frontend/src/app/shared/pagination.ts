import {Component, input, output} from '@angular/core';
import {Page} from '../core/models/product.model';

@Component({
  selector: 'app-pagination',
  template: `
    @if (page().totalPages > 1) {
      <nav class="d-flex justify-content-center align-items-center gap-3 mt-4" aria-label="Pagination">
        <button
          type="button" class="btn btn-outline-secondary btn-sm"
          [disabled]="page().first"
          (click)="pageChange.emit(page().number - 1)">
          <i class="bi bi-chevron-left"></i> Précédent
        </button>
        <span class="text-body-secondary small">Page {{ page().number + 1 }} sur {{ page().totalPages }}</span>
        <button
          type="button" class="btn btn-outline-secondary btn-sm"
          [disabled]="page().last"
          (click)="pageChange.emit(page().number - 1)">
          Suivant <i class="bi bi-chevron-right"></i>
        </button>
      </nav>
    }
  `
})

export class PaginationComponent {
  readonly page = input.required<Page<unknown>>();
  readonly pageChange = output<number>();
}
