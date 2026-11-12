import {Component, inject, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {CurrencyPipe} from '@angular/common';
import {AuthService} from '../../core/services/auth.service';
import {PaymentService} from '../../core/services/payment.service';
import {CartService} from '../../core/services/cart.service';
import {apiErrorMessage} from '../../shared/api_error';

type FieldName = 'customerName' | 'customerEmail' | 'shippingAddress';

@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.html',
  standalone: true
})
export class CheckoutComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly paymentService = inject(PaymentService);
  protected readonly cart = inject(CartService);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

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

  protected invalid(name: FieldName): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

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
