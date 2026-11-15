import {Component, inject, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {CurrencyPipe} from '@angular/common';
import {AuthService} from '../../core/services/auth.service';
import {PaymentService} from '../../core/services/payment.service';
import {CartService} from '../../core/services/cart.service';
import {apiErrorMessage} from '../../shared/api_error';

/**
 * Représente les champs du formulaire de validation de commande.
 * Ce type permet de limiter les noms de champs acceptés par la méthode invalid().
 */
type FieldName = 'customerName' | 'customerEmail' | 'shippingAddress';

/**
 * Composant responsable de la validation du panier et du lancement
 * du processus de paiement d'une commande. Il permet à l'utilisateur
 * de vérifier ses informations de livraison, de consulter le contenu de son
 * panier et de créer une session de paiement stripe checkout.
 * Une fois la session créée par le backend, l'utilisateur est redirigé vers
 * la page de paiement hébergée par stripe.
 */
@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css',
  standalone: true
})
export class CheckoutComponent {
  /** service permettant de construire et de gérer le formulaire réactif */
  private readonly formBuilder = inject(FormBuilder);

  /** service donnant accès aux infos de la session utilisateur */
  private readonly auth = inject(AuthService);

  /** service chargé de communiquer avec l'api de paiement */
  private readonly paymentService = inject(PaymentService);

  /** service permettant d'accéder au panier et à son contenu */
  protected readonly cart = inject(CartService);

  /** indique si une demande de création de session de paiement est en cours */
  protected readonly submitting = signal(false);

  /** contient le message d'erreur à afficher en cas d'échec du paiement */
  protected readonly error = signal<string | null>(null);

  /**
   * Formulaire réactif de validation de commande.
   * Les champs du nom et de l'adresse e-mail sont initialisés à partir de la session
   * utilisateur. L'adresse de livraison doit être renseignée par l'utilisateur.
   * Les validateurs garantissent que les champs obligatoires sont remplis et que leur
   * contenu respecte les contraintes définies.
   * @protected
   */
  protected readonly form = this.formBuilder.nonNullable.group({
    customerName: [
      this.auth.session()?.fullName ?? '',
      [Validators.required, Validators.maxLength(120)]
    ],
    customerEmail: [
      this.auth.session()?.email ?? '',
      [Validators.required, Validators.email]
    ],
    shippingAddress: [
      '',
      [Validators.required, Validators.maxLength(300)]
    ]
  });

  /**
   * Détermine si un champ du formulaire doit afficher son état invalide.
   * Un champ est considéré comme invalide lorsqu'il ne respecte pas ses
   * validateurs et que l'utilisateur l'a déjà modifié ou quitté.
   * @param name nom du champ à vérifier
   * @protected
   */
  protected invalid(name: FieldName): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

  /**
   * Valide le formulaire et déclenche la création d'une session stripe checkout.
   * la méthode vérifie d'abord que le panier contient des articles et que le
   * formulaire est valide. Elle transmet ensuite au backend l'adresse de livraison
   * ainsi que les produits et leurs quantités. En cas de succès, l'identifiant de la
   * commande est conservé dans le sessionStorage, puis l'utilisateur est redirigé vers stripe.
   * En cas d'erreur, un message adapté est affiché et l'état de chargement est réinitialisé.
   * @protected
   */
  protected submit(): void {
    if (this.cart.isEmpty()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const request = {
      shippingAddress: this.form.controls.shippingAddress.value,
      items: this.cart.toDetail()
    };

    this.paymentService.createCheckout(request).subscribe({
      next: (response) => {
        sessionStorage.setItem('springapp_pending_purchase_id', String(response.purchaseId));
        window.location.assign(response.checkoutUrl);
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.submitting.set(false);
      }
    });
  }
}
