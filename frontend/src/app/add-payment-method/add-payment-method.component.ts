import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PaymentMethodService } from '../services/payment-method.service';

@Component({
  selector: 'app-add-payment-method',
  templateUrl: './add-payment-method.component.html',
  styleUrls: ['./add-payment-method.component.css']
})
export class AddPaymentMethodComponent {
  paymentMethod = {
    cardType: 'CREDIT',
    cardNumber: '',
    expiryMonth: null,
    expiryYear: null,
    cardholderName: '',
    billingAddress: '',
    isDefault: false
  };

  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private paymentMethodService: PaymentMethodService, private router: Router) {}

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Basic validation
    if (!this.paymentMethod.cardNumber || this.paymentMethod.cardNumber.length < 13) {
      this.errorMessage = 'Please enter a valid card number';
      this.isLoading = false;
      return;
    }

    if (!this.paymentMethod.expiryMonth || !this.paymentMethod.expiryYear) {
      this.errorMessage = 'Please enter expiry date';
      this.isLoading = false;
      return;
    }

    if (!this.paymentMethod.cardholderName) {
      this.errorMessage = 'Please enter cardholder name';
      this.isLoading = false;
      return;
    }

    this.paymentMethodService.addPaymentMethod(this.paymentMethod).subscribe({
      next: (response) => {
        this.successMessage = 'Payment method added successfully!';
        this.isLoading = false;
        
        setTimeout(() => {
          this.router.navigate(['/payment-methods']);
        }, 2000);
      },
      error: (error) => {
        console.error('Error adding payment method:', error);
        this.errorMessage = error.error?.error || 'Failed to add payment method';
        this.isLoading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/payment-methods']);
  }
}
