import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Category, CategoryRequest} from '../models/category.model';
import {Observable} from 'rxjs';

/**
 * Service permettant de communiquer avec les points d'accès de l'api
 * dédiés à la gestion des achats. il permet de créer des achats, de consulter
 * l'historique des achats et de modifier leur statut
 */
@Injectable({providedIn: 'root'})
export class PurchaseService {
  /** client http utilisé pour communiquer avec l'api des achats */
  private readonly http = inject(HttpClient);

  /** url de base des points d'accès de l'api des achats */
  private readonly url = `${environment.apiUrl}/purchases`;

  /**
   * créer un nouvel achat. envoie les infos de l'achat à l'api
   * et retourne l'achat créé
   * @param request infos nécessaires à la création de l'achat
   */
  create(request: PurchaseRequest): Observable<Purchase> {
    return this.http.post<Purchase>(this.url, request);
  }

  /**
   * récupère l'ensemble des achats de l'utilisateur connecté.
   * utilise le point d'accès dédié à l'historique des achats
   * de l'utilisateur actuellement authentifié
   */
  myPurchases(): Observable<Purchase[]> {
    return this.http.get<Purchase[]>(`${this.url}/me`);
  }

  /**
   * récupère l'ensemble des achats enregistrés. cette méthode
   * est généralement destinées à une utilisation administrative,
   * selon les règles d'autorisation définies dans le backend
   */
  all(): Observable<Purchase[]> {
    return this.http.get<Purchase[]>(this.url);
  }

  /**
   * récupère un achat à partir de son identifiant.
   * @param id identifiant de l'achat à récupérer
   */
  get(id: number): Observable<Purchase> {
    return this.http.get<Purchase>(`${this.url}/${id}`);
  }

  /**
   * modifie le statut d'un achat existant.
   * @param id identifiant de l'achat à modifier
   * @param status nouveau statut à attribuer à l'achat
   */
  updateStatus(id: number, status: PurchaseStatus): Observable<Purchase> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<Purchase>(`${this.url}/${id}/status`, null, {params});
  }
}
