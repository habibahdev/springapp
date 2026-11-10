import {HttpClient} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {ChatRequest, ChatResponse} from '../models/assistant.model';

/**
 * Service responsable de la communication avec l'assistant conversationnel.
 * Il permet d'envoyer les messages des clients au backend et de récupérer
 * les réponses ainsi que les produits recommandés
 */
@Injectable({providedIn: 'root'})
export class AssistantService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/assistant`;

  /**
   * envoie un message à l'assistant conversationnel
   * @param message message formulé par le client
   */
  chat(message: string): Observable<ChatResponse> {
    const request: ChatRequest = {message};
    return this.http.post<ChatResponse>(`${this.url}/chat`, request);
  }
}
