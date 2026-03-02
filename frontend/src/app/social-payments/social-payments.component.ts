import { Component, OnInit } from '@angular/core';
import { SocialPaymentService } from '../services/social-payment.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-social-payments',
  templateUrl: './social-payments.component.html',
  styleUrls: ['./social-payments.component.css']
})
export class SocialPaymentsComponent implements OnInit {
  payments: any[] = [];
  
  // Form properties
  newAmount: number | null = null;
  newPlatform: string = '';
  newDescription: string = '';
  newExternalReference: string = '';

  constructor(
    private socialPaymentService: SocialPaymentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(): void {
    this.socialPaymentService.getUserPayments().subscribe({
      next: (data) => this.payments = data,
      error: (err) => console.error('Error loading payments:', err)
    });
  }

  createPayment(): void {
    if (!this.newAmount || !this.newPlatform || !this.newDescription) {
      alert('Please fill in all required fields');
      return;
    }

    this.socialPaymentService.createPayment(
      this.newAmount,
      this.newPlatform,
      this.newDescription,
      this.newExternalReference
    ).subscribe({
      next: () => {
        this.loadPayments();
        this.resetForm();
        alert('Social payment created successfully!');
      },
      error: (err) => {
        console.error('Error creating payment:', err);
        alert('Failed to create social payment');
      }
    });
  }

  completePayment(paymentId: number): void {
    const payment = this.payments.find(p => p.id === paymentId);
    const amount = payment ? payment.amount : '0';
    
    if (confirm(`Complete social payment for $${amount}?`)) {
      this.socialPaymentService.completePayment(paymentId).subscribe({
        next: () => {
          this.loadPayments();
          alert('Payment completed successfully!');
        },
        error: (err) => {
          console.error('Error completing payment:', err);
          alert('Failed to complete payment');
        }
      });
    }
  }

  failPayment(paymentId: number): void {
    const payment = this.payments.find(p => p.id === paymentId);
    const amount = payment ? payment.amount : '0';
    
    if (confirm(`Fail social payment for $${amount}?`)) {
      this.socialPaymentService.failPayment(paymentId).subscribe({
        next: () => {
          this.loadPayments();
          alert('Payment marked as failed!');
        },
        error: (err) => {
          console.error('Error failing payment:', err);
          alert('Failed to mark payment as failed');
        }
      });
    }
  }

  viewPaymentDetails(paymentId: number): void {
    this.router.navigate(['/social-payments', paymentId]);
  }

  private resetForm(): void {
    this.newAmount = null;
    this.newPlatform = '';
    this.newDescription = '';
    this.newExternalReference = '';
  }

  // Helper method to get amount for confirmation dialogs
  private getAmountForPayment(paymentId: number): string {
    const payment = this.payments.find(p => p.id === paymentId);
    return payment ? payment.amount : '0';
  }
}