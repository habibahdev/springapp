import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient} from '@angular/common/http';
import {afterEach, beforeEach, describe, expect, it} from 'vitest';

import {ProductService} from '../product.service';
import {environment} from '../../../../environments/environment';
import {Page, Product, ProductRequest} from '../../models/product.model';

describe('ProductService', () => {
  let service: ProductService;
  let httpTestingController: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/products`;

  const mockProduct: Product = {
    id: 1,
    name: 'Ordinateur portable',
    description: 'Ordinateur portable de test',
    price: 899.99,
    stock: 10,
    imageUrl: 'https://example.com/laptop.jpg',
    categoryId: 1,
    categoryName: 'Informatique'
  };

  const mockRequest: ProductRequest = {
    name: 'Ordinateur portable',
    description: 'Ordinateur portable de test',
    price: 899.99,
    stock: 10,
    imageUrl: 'https://example.com/laptop.jpg',
    categoryId: 1,
    categoryName: 'Informatique'
  };

  const mockPage: Page<Product> = {
    content: [mockProduct],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 12,
    first: true,
    last: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ProductService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });


  describe('search', () => {
    it('should retrieve products without search criteria', () => {
      service.search().subscribe((result) => {
        expect(result).toEqual(mockPage);
      });

      const request = httpTestingController.expectOne(apiUrl);

      expect(request.request.method).toBe('GET');

      request.flush(mockPage);
    });

    it('should retrieve products with search criteria', () => {
      service.search({
        q: 'ordinateur',
        categoryId: 1,
        page: 0,
        size: 12,
        sort: 'price'
      }).subscribe((result) => {
        expect(result).toEqual(mockPage);
      });

      const request = httpTestingController.expectOne(
        (req) => req.url === apiUrl
      );

      expect(request.request.method).toBe('GET');
      expect(request.request.params.get('q')).toBe('ordinateur');
      expect(request.request.params.get('categoryId')).toBe('1');
      expect(request.request.params.get('page')).toBe('0');
      expect(request.request.params.get('size')).toBe('12');
      expect(request.request.params.get('sort')).toBe('price');

      request.flush(mockPage);
    });

    it('should ignore undefined, null and empty parameters', () => {
      service.search({
        q: '',
        categoryId: undefined,
        page: 0,
        size: 12,
        sort: 'name'
      }).subscribe();

      const request = httpTestingController.expectOne(
        (req) => req.url === apiUrl
      );

      expect(request.request.params.has('q')).toBe(false);
      expect(request.request.params.has('categoryId')).toBe(false);

      // La valeur 0 doit être conservée.
      expect(request.request.params.get('page')).toBe('0');

      request.flush(mockPage);
    });
  });


  describe('get', () => {
    it('should retrieve a product by its id', () => {
      service.get(1).subscribe((product) => {
        expect(product).toEqual(mockProduct);
      });

      const request = httpTestingController.expectOne(`${apiUrl}/1`);

      expect(request.request.method).toBe('GET');

      request.flush(mockProduct);
    });
  });

  describe('create', () => {
    it('should create a new product', () => {
      service.create(mockRequest).subscribe((product) => {
        expect(product).toEqual(mockProduct);
      });

      const request = httpTestingController.expectOne(apiUrl);

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(mockRequest);

      request.flush(mockProduct);
    });
  });


  describe('update', () => {
    it('should update an existing product', () => {
      service.update(1, mockRequest).subscribe((product) => {
        expect(product).toEqual(mockProduct);
      });

      const request = httpTestingController.expectOne(`${apiUrl}/1`);

      expect(request.request.method).toBe('PUT');
      expect(request.request.body).toEqual(mockRequest);

      request.flush(mockProduct);
    });
  });


  describe('delete', () => {
    it('should delete a product by its id', () => {
      service.delete(1).subscribe();

      const request = httpTestingController.expectOne(`${apiUrl}/1`);

      expect(request.request.method).toBe('DELETE');

      request.flush(null);
    });
  });
});
