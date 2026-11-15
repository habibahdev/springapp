import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {CheckoutResponse, PaymentRequest} from '../models/payment.model';

/**
 * Service responsable de la communication avec les points d'accès de
 * l'api dédiés aux paiements.
 * Il permet notamment de demander au backend la création d'une session
 * stripe checkout afin de rediriger l'utilisateur vers la page de paiement
 * sécurisée.
 */
@Injectable({providedIn: 'root'})
export class PaymentService {
  /** client http utilisé pour envoyer les requêtes à l'api */
  private readonly http = inject(HttpClient);

  /** url de base des endpoints de l'api de paiement */
  private readonly apiUrl = `${environment.apiUrl}/payments`;

  /**
   * Demande la création d'une session stripe. Transmet au backend les infos
   * nécessaires à la création d'une commande et à l'initialisation du
   * paiement.
   * @param request adresse de livraison et liste des articles commandés
   */
  createCheckout(request: PaymentRequest): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.apiUrl}/checkout`, request);
  }
}
