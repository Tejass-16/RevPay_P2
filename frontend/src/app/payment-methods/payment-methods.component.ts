import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PaymentMethodService } from '../services/payment-method.service';

@Component({
  selector: 'app-payment-methods',
  templateUrl: './payment-methods.component.html',
  styleUrls: ['./payment-methods.component.css']
})
export class PaymentMethodsComponent implements OnInit {
  paymentMethods: any[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private paymentMethodService: PaymentMethodService, private router: Router) {}

  ngOnInit(): void {
    this.loadPaymentMethods();
  }

  loadPaymentMethods(): void {
    console.log('=== PAYMENT METHODS DEBUG ===');
    console.log('Loading payment methods...');
    
    this.paymentMethodService.getPaymentMethods().subscribe({
      next: (response) => {
        console.log('Payment methods response:', response);
        console.log('Response type:', typeof response);
        console.log('Response length:', response ? response.length : 'null');
        this.paymentMethods = response;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading payment methods:', error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        this.errorMessage = 'Failed to load payment methods';
        this.isLoading = false;
      }
    });
  }

  addPaymentMethod(): void {
    this.router.navigate(['/payment-methods/add']);
  }

  editPaymentMethod(id: number): void {
    this.router.navigate(['/payment-methods/edit', id]);
  }

  deletePaymentMethod(id: number): void {
    if (confirm('Are you sure you want to delete this payment method?')) {
      this.paymentMethodService.deletePaymentMethod(id).subscribe({
        next: () => {
          this.successMessage = 'Payment method deleted successfully';
          this.loadPaymentMethods();
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error deleting payment method:', error);
          this.errorMessage = error.error?.error || 'Failed to delete payment method';
        }
      });
    }
  }

  setAsDefault(id: number): void {
    this.paymentMethodService.setDefaultPaymentMethod(id).subscribe({
      next: () => {
        this.successMessage = 'Payment method set as default';
        this.loadPaymentMethods();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        console.error('Error setting default payment method:', error);
        this.errorMessage = error.error?.error || 'Failed to set default payment method';
      }
    });
  }
}
