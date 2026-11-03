import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Category, CategoryRequest} from '../models/category.model';
import {Observable} from 'rxjs';

/**
 * Service responsable de la gestion des catégories de produits.
 * il permet de communiquer avec l'api pour :
 * <ul>
 *   <li>récupère la liste des catégories</li>
 *   <li>créer une nouvelle catégorie</li>
 *   <li>modifier une catégorie existante</li>
 *   <li>supprime une catégorie</li>
 * </ul>
 */
@Injectable({providedIn: 'root'})
export class CategoryService {
  /** client http utilisé pour communiquer avec l'api */
  private readonly http = inject(HttpClient);

  /** url de base des endpoints de gestion des catégories */
  private readonly url = `${environment.apiUrl}/categories`;

  /**
   * récupère la liste de toutes les catégories
   */
  list(): Observable<Category[]> {
    return this.http.get<Category[]>(this.url);
  }

  /**
   * créer une nouvelle catégorie
   * @param request infos nécessaires à la création de la catégorie
   */
  create(request: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.url, request);
  }

  /**
   * modifie une catégorie existante
   * @param id identifiant de la catégorie à modifier
   * @param request nouvelles infos de la catégorie
   */
  update(id: number, request: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.url}/${id}`, request);
  }

  /**
   * supprime une catégorie à partir de son identifiant
   * @param id identifiant de la catégorie à supprimer
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
