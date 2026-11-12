import {Component, inject, OnInit} from '@angular/core';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-payment-cancel',
  imports: [RouterLink],
  templateUrl: './cancel.component.html'
})
export class PaymentCancelComponent implements OnInit {

  ngOnInit(): void {
    sessionStorage.removeItem('springapp_pending_purchase_id');
  }
}
