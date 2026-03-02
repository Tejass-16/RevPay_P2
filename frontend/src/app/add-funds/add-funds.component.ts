import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TransactionService } from '../services/transaction.service';
import { PaymentMethodService } from '../services/payment-method.service';

@Component({
  selector: 'app-add-funds',
  templateUrl: './add-funds.component.html',
  styleUrls: ['./add-funds.component.css']
})
export class AddFundsComponent implements OnInit {
  paymentMethods: any[] = [];
  selectedPaymentMethod: string = '';
  amount: number = 0;
  bankAccount: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  paymentMethod: string = 'card';

  constructor(private transactionService: TransactionService, 
              private paymentMethodService: PaymentMethodService,
              private router: Router) {}

  ngOnInit(): void {
    this.loadPaymentMethods();
  }

  onPaymentMethodChange(): void {
    this.selectedPaymentMethod = '';
    this.errorMessage = '';
  }

  loadPaymentMethods(): void {
    this.paymentMethodService.getPaymentMethods().subscribe({
      next: (response: any) => {
        this.paymentMethods = response;
        // Set default payment method if available
        if (this.paymentMethods.length > 0 && !this.selectedPaymentMethod) {
          const defaultMethod = this.paymentMethods.find((method: any) => method.isDefault);
          if (defaultMethod) {
            this.selectedPaymentMethod = defaultMethod.id;
          }
        }
      },
      error: (error: any) => {
        console.error('Error loading payment methods:', error);
        this.errorMessage = 'Failed to load payment methods';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Basic validation
    if (this.amount <= 0) {
      this.errorMessage = 'Please enter a valid amount';
      this.isLoading = false;
      return;
    }

    if (this.paymentMethod === 'card' && !this.selectedPaymentMethod) {
      this.errorMessage = 'Please select a payment method';
      this.isLoading = false;
      return;
    }

    if (this.paymentMethod === 'bank' && !this.bankAccount.trim()) {
      this.errorMessage = 'Please enter bank account details';
      this.isLoading = false;
      return;
    }

    const fundData: any = {
      amount: this.amount
    };
    
    if (this.paymentMethod === 'card') {
      fundData.paymentMethodId = this.selectedPaymentMethod;
    } else if (this.paymentMethod === 'bank') {
      fundData.bankAccount = this.bankAccount;
    }

    this.transactionService.addFunds(this.amount, fundData).subscribe({
      next: (response: any) => {
        this.successMessage = 'Funds added successfully!';
        this.isLoading = false;
        
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 2000);
      },
      error: (error: any) => {
        console.error('Error adding funds:', error);
        this.errorMessage = error.error?.error || 'Failed to add funds';
        this.isLoading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}