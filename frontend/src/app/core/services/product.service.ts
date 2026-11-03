import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Page, Product, ProductQuery, ProductRequest} from '../models/product.model';
import {Observable} from 'rxjs';

/**
 * Service responsable de la gestion des produits. Il permet de communiquer avec l'API pour :
 * <ul>
 *   <li>rechercher et filtrer les produits</li>
 *   <li>récupérer un produit par son identifiant</li>
 *   <li>créer un nouveau produit</li>
 *   <li>modifier un produit existant</li>
 *   <li>supprimer un produit</li>
 * </ul>
 */
@Injectable({providedIn: 'root'})
export class ProductService {
  /** client http utilisé pour communiquer avec l'api */
  private readonly http = inject(HttpClient);

  /** url de base des endpoints de gestion des porduits */
  private readonly url = `${environment.apiUrl}/products`;

  /**
   * Recherche les produits en fonction des critères fournis.
   * Les critères sont convertis en paramètres http. les valeurs undefined,
   * null et les chaines vides sont ignorées.
   * @param query les critères de recherche, de filtrage et de pagination
   */
  search(query: ProductQuery = {}): Observable<Page<Product>> {
    let params = new HttpParams();
    for (const [k,v] of Object.entries(query)) {
      if (v !== undefined && v !== null && v !== '') {
        params = params.set(k, String(v));
      }
    }
    return this.http.get<Page<Product>>(this.url, {params});
  }

  /**
   * récupère un produit à partir de son identifiant.
   * @param id identifiant du produit recherché
   */
  get(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.url}/${id}`);
  }

  /**
   * crée un nouveau produit. les infos du produit sont envoyées à l'api
   * dans le corps de la requête http post
   * @param request infos nécessaires à la création du porduit
   */
  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.url, request);
  }

  /**
   * modifie un produit existant. les nouvelles infos sont envoyées à l'api
   * dans le corps de la requête http put
   * @param id identifiant du produit à modifier
   * @param request nouvelles infos du produit
   */
  update(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.url}/${id}`, request);
  }

  /**
   * supprimer un produit à partir de son identifiant
   * @param id identifiant du produit à supprimer
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
