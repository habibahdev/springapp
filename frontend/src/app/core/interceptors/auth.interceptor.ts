import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../services/auth.service';
import {environment} from '../../../environments/environment';
import {catchError, throwError} from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const isApiCall = req.url.startsWith(environment.apiUrl);
  const isAuthCall = req.url.startsWith(`${environment.apiUrl}/auth/`)
  const request = token && isApiCall ? req.clone({setHeaders: {Authorization: `Bearer ${token}`}}): req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && token && isApiCall && !isAuthCall) {
        auth.logout();
      }
      return throwError(() => error);
    })
  );
}
