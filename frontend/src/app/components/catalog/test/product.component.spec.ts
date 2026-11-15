import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {provideRouter} from '@angular/router';
import {of, throwError} from 'rxjs';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';

import {ProductsComponent} from '../products/products.component';
import {PaginationComponent} from '../../../shared/pagination';

import {ProductService} from '../../../core/services/product.service';
import {CategoryService} from '../../../core/services/category.service';
import {CartService} from '../../../core/services/cart.service';

import {Category} from '../../../core/models/category.model';
import {Page, Product} from '../../../core/models/product.model';

describe('ProductsComponent', () => {
  let fixture: ComponentFixture<ProductsComponent>;

  let productService: {
    search: ReturnType<typeof vi.fn>;
  };

  let categoryService: {
    list: ReturnType<typeof vi.fn>;
  };

  let cartService: {
    add: ReturnType<typeof vi.fn>;
    quantityOf: ReturnType<typeof vi.fn>;
  };

  let initialCartQuantity = 0;

  const mockProducts: Product[] = [
    {
      id: 1,
      name: 'Ordinateur portable',
      description: 'Ordinateur portable de test',
      price: 899.99,
      stock: 10,
      imageUrl: 'https://example.com/laptop.jpg',
      categoryId: 1,
      categoryName: 'Informatique'
    },
    {
      id: 2,
      name: 'Casque audio',
      description: 'Casque audio sans fil',
      price: 79.99,
      stock: 0,
      imageUrl: null,
      categoryId: 2,
      categoryName: 'Audio'
    }
  ];

  const mockCategories: Category[] = [
    {id: 1, name: 'Informatique'},
    {id: 2, name: 'Audio'}
  ];

  const mockPage: Page<Product> = {
    content: mockProducts,
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 12,
    first: true,
    last: true
  };

  beforeEach(async () => {
    initialCartQuantity = 0;
    productService = {
      search: vi.fn().mockReturnValue(of(mockPage))
    };

    categoryService = {
      list: vi.fn().mockReturnValue(of(mockCategories))
    };

    cartService = {
      add: vi.fn(),
      quantityOf: vi.fn((productId: number) =>
        productId === 1 ? initialCartQuantity : 0
      )
    };

    await TestBed.configureTestingModule({
      imports: [ProductsComponent],
      providers: [
        provideRouter([]),
        {
          provide: ProductService,
          useValue: productService
        },
        {
          provide: CategoryService,
          useValue: categoryService
        },
        {
          provide: CartService,
          useValue: cartService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductsComponent);
    fixture.detectChanges();

    // Attend la fin du debounceTime(250).
    await vi.waitFor(() => {
      expect(productService.search).toHaveBeenCalled();
    });

    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });


  describe('initialization', () => {
    it('should create the component', () => {
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should retrieve categories on initialization', () => {
      expect(categoryService.list).toHaveBeenCalledOnce();

      const categorySelect: HTMLSelectElement =
        fixture.nativeElement.querySelector(
          'select[aria-label="Filtrer par catégorie"]'
        );

      expect(categorySelect).not.toBeNull();
      expect(categorySelect.options.length).toBe(3);
      expect(categorySelect.options[1].textContent?.trim())
        .toBe('Informatique');
    });

    it('should retrieve products on initialization', () => {
      expect(productService.search).toHaveBeenCalledWith({
        q: undefined,
        categoryId: undefined,
        sort: 'name',
        page: 0,
        size: 12
      });
    });

    it('should display the products returned by the service', () => {
      const productCards =
        fixture.nativeElement.querySelectorAll('.product-card');

      expect(productCards.length).toBe(2);

      expect(fixture.nativeElement.textContent)
        .toContain('Ordinateur portable');

      expect(fixture.nativeElement.textContent)
        .toContain('Casque audio');
    });

    it('should display the total number of products', () => {
      const pageHeader = fixture.nativeElement.textContent;

      expect(pageHeader).toContain('2 produits');
    });
  });


  describe('search', () => {
    it('should search products using the entered text', async () => {
      const searchInput: HTMLInputElement =
        fixture.nativeElement.querySelector(
          'input[aria-label="Rechercher un produit"]'
        );

      searchInput.value = 'ordinateur';
      searchInput.dispatchEvent(new Event('input'));

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith({
          q: 'ordinateur',
          categoryId: undefined,
          sort: 'name',
          page: 0,
          size: 12
        });
      });

      expect(searchInput.value).toBe('ordinateur');
    });

    it('should reset the page index when searching', async () => {
      const pagination = fixture.debugElement.query(
        By.directive(PaginationComponent)
      );

      // Simule le passage à la deuxième page.
      pagination.componentInstance.pageChange.emit(1);
      fixture.detectChanges();

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({page: 1})
        );
      });

      const searchInput: HTMLInputElement =
        fixture.nativeElement.querySelector(
          'input[aria-label="Rechercher un produit"]'
        );

      searchInput.value = 'ordinateur';
      searchInput.dispatchEvent(new Event('input'));

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({
            q: 'ordinateur',
            page: 0
          })
        );
      });
    });
  });


  describe('category filter', () => {
    it('should filter products by category', async () => {
      const categorySelect: HTMLSelectElement =
        fixture.nativeElement.querySelector(
          'select[aria-label="Filtrer par catégorie"]'
        );

      categorySelect.value = '1';
      categorySelect.dispatchEvent(new Event('change'));

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({
            categoryId: 1,
            page: 0
          })
        );
      });
    });

    it('should remove the category filter when selecting all categories', async () => {
      const categorySelect: HTMLSelectElement =
        fixture.nativeElement.querySelector(
          'select[aria-label="Filtrer par catégorie"]'
        );

      categorySelect.value = '1';
      categorySelect.dispatchEvent(new Event('change'));

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({categoryId: 1})
        );
      });

      categorySelect.value = '';
      categorySelect.dispatchEvent(new Event('change'));

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({
            categoryId: undefined,
            page: 0
          })
        );
      });
    });
  });


  describe('sorting', () => {
    it('should sort products by price', async () => {
      const sortSelect: HTMLSelectElement =
        fixture.nativeElement.querySelector(
          'select[aria-label="Trier"]'
        );

      sortSelect.value = 'price';
      sortSelect.dispatchEvent(new Event('change'));

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({
            sort: 'price',
            page: 0
          })
        );
      });
    });

    it('should sort products by name by default', () => {
      const sortSelect: HTMLSelectElement =
        fixture.nativeElement.querySelector(
          'select[aria-label="Trier"]'
        );

      expect(sortSelect.value).toBe('name');
    });
  });


  describe('pagination', () => {
    it('should retrieve the selected page', async () => {
      const scrollSpy = vi.spyOn(window, 'scrollTo')
        .mockImplementation(() => {});

      const pagination = fixture.debugElement.query(
        By.directive(PaginationComponent)
      );

      pagination.componentInstance.pageChange.emit(1);
      fixture.detectChanges();

      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenLastCalledWith(
          expect.objectContaining({page: 1})
        );
      });

      expect(scrollSpy).toHaveBeenCalledWith({
        top: 0,
        behavior: 'smooth'
      });
    });
  });


  describe('cart', () => {
    it('should add a product to the cart', () => {
      const addButton: HTMLButtonElement =
        fixture.nativeElement.querySelector('.product-card button');

      addButton.click();

      expect(cartService.add).toHaveBeenCalledWith(mockProducts[0]);
    });

    it('should disable the add button when the product is out of stock', () => {
      const buttons: HTMLButtonElement[] = Array.from(
        fixture.nativeElement.querySelectorAll('.product-card button')
      );

      expect(buttons[1].disabled).toBe(true);
      expect(buttons[1].textContent).toContain('Ruputure de stock');
    });

    it('should disable the add button when the maximum stock quantity is reached', async () => {
      initialCartQuantity = 10;

      fixture.destroy();
      fixture = TestBed.createComponent(ProductsComponent);
      fixture.detectChanges();

      // Attend le chargement des produits après le debounceTime.
      await vi.waitFor(() => {
        expect(productService.search).toHaveBeenCalledTimes(2);
      });

      fixture.detectChanges();

      const buttons: HTMLButtonElement[] = Array.from(
        fixture.nativeElement.querySelectorAll('.product-card button')
      );

      expect(buttons.length).toBe(2);
      expect(buttons[0].disabled).toBe(true);
      expect(buttons[0].textContent).toContain('Quantité max atteinte');
    });
  });


  describe('empty results', () => {
    it('should display a message when no product matches the search', async () => {
      const emptyPage: Page<Product> = {
        ...mockPage,
        content: [],
        totalElements: 0,
        totalPages: 0,
        last: true
      };

      productService.search.mockReturnValue(of(emptyPage));

      const searchInput: HTMLInputElement =
        fixture.nativeElement.querySelector(
          'input[aria-label="Rechercher un produit"]'
        );

      searchInput.value = 'produit inexistant';
      searchInput.dispatchEvent(new Event('input'));

      await vi.waitFor(() => {
        expect(
          fixture.nativeElement.textContent
        ).toContain('Aucun produit ne correspond à votre recherche.');
      });

      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelectorAll('.product-card').length
      ).toBe(0);
    });
  });


  describe('error handling', () => {
    it('should display an error message when product loading fails', async () => {
      productService.search.mockReturnValue(
        throwError(() => ({
          status: 500,
          error: {message: 'Erreur serveur'}
        }))
      );

      const searchInput: HTMLInputElement =
        fixture.nativeElement.querySelector(
          'input[aria-label="Rechercher un produit"]'
        );

      searchInput.value = 'test erreur';
      searchInput.dispatchEvent(new Event('input'));

      await vi.waitFor(() => {
        expect(
          fixture.nativeElement.querySelector('[role="alert"]')
        ).not.toBeNull();
      });

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        'Impossible de charger les produits'
      );
    });
  });
});
