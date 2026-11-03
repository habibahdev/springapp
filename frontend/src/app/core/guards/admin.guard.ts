import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {AuthService} from '../services/auth.service';

/**
 * Guard chargé de protéger les routes accessibles uniquement aux admins.
 * vérifie que l'utilisateur est authentifié et possède le rôle admin
 *
 * <ul>
 *   <li>si l'utilisateur n'est pas connecté, il est redirigé vers la page de connexion
 *   l'url initialement demandée et conservée dans le paramètre returnUrl</li>
 *   <li>si l'utilisateur est connecté et admin, accès à la route autorisé</li>
 *   <li>si l'utilisateur est connecté, mais ne possède pas le rôle admin, il est
 *   redirigé vers la page d'accueil</li>
 * </ul>
 * @param _route infos relatives à la route à activer
 * @param state état de la route : url
 */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login'], {
      queryParams: {returnUrl: state.url}
    });
  }
  return auth.isAdmin() ? true : router.createUrlTree(['/']);
}
