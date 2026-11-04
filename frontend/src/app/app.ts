import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLinkActive, RouterLink } from '@angular/router';
import {AuthService} from './core/services/auth.service';
import {CartService} from './core/services/cart.service';

@Component({
  imports: [RouterOutlet, RouterLinkActive, RouterLink],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  //protected readonly title = signal('frontend');
  protected readonly auth = inject(AuthService);
  protected readonly cart = inject(CartService);
  protected readonly menuOpen = signal(false);

  protected logout(): void {
    this.auth.logout()
  }
}
