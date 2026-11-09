import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';

/**
 * Composant racine de l'espace d'administration de SpringApp
 * <p>ce composant sert de conteneur aux différentes interfaces de
 * gestion accessibles à l'administrateur, notamment les produits
 * les catégories et les commandes</p>
 * <p>la navigation entre ces interfaces est assurée par les liens
 * angular router. le composant {@code RouterOutlet} permet d'afficher
 * dynamiquement le composant correspondant à la route enfant active</p>
 */
@Component({
  selector: 'app-admin',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css',
  standalone: true,
})
export class AdminComponent {}
