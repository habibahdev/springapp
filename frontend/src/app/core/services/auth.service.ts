import {HttpClient} from '@angular/common/http';
import {Injectable, computed, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, tap} from 'rxjs';
import {environment} from '../../../environments/environment';
import {AuthResponse, LoginRequest, RegisterRequest} from '../models/auth.model';

/**
 * clé utilisée pour enregistrer la session utilisateur dans le localStorage.
 * il permet de :
 * <uL>
 *   <li>connecter un utilisateur</li>
 *   <li>créer un nouveau compte</li>
 *   <li>déconnecter un utilisateur</li>
 *   <li>vérification de la validité du token</li>
 * </ul>
 */
const STORAGE_KEY = 'springapp_session';

/**
 * Service responsable de la gestion de l'authentification des utilisateurs.
 */
@Injectable({providedIn: 'root'})
export class AuthService {
  /** client http utilisé pour communiquer avec l'api d'authentification */
  private readonly http = inject(HttpClient);

  /** routeur pour rediriger l'utilisateur après sa déconnexion */
  private readonly router = inject(Router);

  /** url de base des endpoints d'authentification */
  private readonly url = `${environment.apiUrl}/auth`;

  /** signal contenant la session utilisateur ; session initialisée à partir du localStorage */
  private readonly _session = signal<AuthResponse | null>(this.load());

  /** signal en lecture seul contenant la session utilisateur */
  readonly session = this._session.asReadonly();

  /** est-ce que l'utilisateur est connecté */
  readonly isLoggedIn = computed(() => this._session() !== null);

  /** indique si l'utilisateur connecté est admin ou non */
  readonly isAdmin = computed(() => this._session()?.role === 'ADMIN');

  /** retourne le nom complet de l'utilisateur connecté ou chaine vide */
  readonly fullName = computed(() => this._session()?.fullName ?? '');

  /**
   * retourne le token jwt de la session actuelle.
   */
  get token(): string | null {
    return this._session()?.token ?? null;
  }

  /**
   * authentifie un utilisateur auprès de l'api.
   * @param request les identifiants de connexion
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.url}/login`, request)
      .pipe(tap((res) => this.setSession(res)));
  }

  /**
   * Déconnecte l'utilisateur actuellement connecté.
   */
  logout(): void {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {}
    this._session.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Crée un nouveau compte utilisateur auprès de l'api.
   * @param request infos nécessaires à la création du compte.
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.url}/register`, request)
      .pipe(tap((res) => this.setSession(res)));
  }

  /**
   * Récupère la session enregistrée dans le localStorage
   * @private
   */
  private load(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const session = JSON.parse(raw) as AuthResponse;
      if (this.isExpired(session.token)) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return session;
    } catch {
      return null;
    }
  }

  /**
   * Vérifie si un token est expiré.
   * @param token token jwt à vérifier
   * @private
   */
  private isExpired(token: string): boolean {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64));
      return payload.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }

  /**
   * enregistre une nouvelle session utilisateur. La session est sauvegardée dans le localStorage
   * et mise à jour dans le signal pour notifier les composants qui l'utilisent.
   * @param session session reçue depuis l'api
   * @private
   */
  private setSession(session : AuthResponse): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } catch {}
    this._session.set(session);
  }
}
