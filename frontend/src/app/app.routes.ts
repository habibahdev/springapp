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
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./components/admin/admin.component').then((m) => m.AdminComponent),
    children: [
      {path: '', pathMatch: 'full', redirectTo: 'products'},
      {
        path: 'products',
        title: 'Administration - Produits',
        loadComponent: () => import('./components/admin/product/product.component').then((m) => m.AdminProductComponent)
      },
      {
        path: 'categories',
        title: 'Administration - Catégories',
        loadComponent: () => import('./components/admin/category/category.component').then((m) => m.AdminCategoriesComponent)
      },
      {
        path: 'purchases',
        title: 'Administration - Commandes',
        loadComponent: () => import('./components/admin/purchase/purchase.component').then((m) => m.AdminPurchasesComponent)
      },
    ]
  },
  {path: '**', redirectTo: ''}
];
