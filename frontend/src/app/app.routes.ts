import { Routes, CanActivateFn, Router } from '@angular/router';
import {inject} from '@angular/core';
import {adminGuard} from './core/guards/admin.guard';
import {AuthService} from './core/services/auth.service';

const assistantGuard: CanActivateFn= () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAdmin() ? router.createUrlTree(['/admin']) : true;
};

export const routes: Routes = [
  {
    path: 'login',
    title: 'Connexion',
    loadComponent: () => import('./components/auth/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    title: 'Créer un compte',
    loadComponent: () => import('./components/auth/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: 'assistant',
    title: 'Assistant',
    canActivate: [assistantGuard],
    loadComponent: () => import ('./components/assistant/assistant.component').then((m) => m.AssistantComponent)
  },
  {path: '**', redirectTo: ''}
];
