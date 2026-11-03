import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {AuthService} from '../services/auth.service';

/**
 * Guard chargé de protéger les routes accessibles uniquement aux utilisateurs connectés.
 * vérifie si une session utilisateur est active.
 * <ul>
 *   <li>si l'utilisateur est connecté, l'accès à la route est autorisé</li>
 *   <li>si l'utilisateur n'est pas connecté, redirection vers la page de connexion</li>
 *   <li>l'url initialement demandée est conservée dans le paramètre returnUrl pour
 *   permettre une éventuelle redirection après la connexion</li>
 * </ul>
 * @param _route infos relatives à la route à activer
 * @param state état de la route : url
 */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isLoggedIn()
    ? true : router.createUrlTree(['/login'], {queryParams: {returnUrl: state.url}});
}
