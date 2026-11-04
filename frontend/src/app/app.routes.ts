import { Routes } from '@angular/router';
import {adminGuard} from './core/guards/admin.guard';

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
  {path: '**', redirectTo: ''}
];
