import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TransactionService } from '../services/transaction.service';

@Component({
  selector: 'app-send-money',
  templateUrl: './send-money.component.html',
  styleUrls: ['./send-money.component.css']
})
export class SendMoneyComponent implements OnInit {
  email: string = '';
  amount: number | null = null;
  transactionPin: string = '';
  isLoading: boolean = false;
  walletBalance: number = 0;

  constructor(
    private authService: AuthService,
    private transactionService: TransactionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadWalletBalance();
    this.testBackendConnection();
  }

  testBackendConnection(): void {
    console.log('Testing backend connection...');
    this.transactionService.testConnection().subscribe({
      next: (response) => {
        console.log('Backend connection successful:', response);
        this.testSendMoneyEndpoint();
      },
      error: (error: any) => {
        console.error('Backend connection failed:', error);
      }
    });
  }

  testSendMoneyEndpoint(): void {
    console.log('Testing send money endpoint...');
    const testData = {
      recipientEmail: 'test@example.com',
      amount: 10,
      description: 'Test transfer',
      transactionPin: ''
    };
    
    this.transactionService.testRequest().subscribe({
      next: (response) => {
        console.log('Test send money endpoint successful:', response);
      },
      error: (error: any) => {
        console.error('Test send money endpoint failed:', error);
      }
    });
  }

  fixDatabase(): void {
    console.log('Fixing database schema...');
    this.transactionService.fixDatabase().subscribe({
      next: (response) => {
        console.log('Database fix successful:', response);
        alert('Database schema fixed! Please try sending money again.');
      },
      error: (error: any) => {
        console.error('Database fix failed:', error);
        alert('Database fix failed. Check console for details.');
      }
    });
  }

  loadWalletBalance(): void {
    this.transactionService.getWalletBalance().subscribe({
      next: (response: any) => {
        this.walletBalance = response.balance || 0;
      },
      error: (error: any) => {
        console.error('Error loading balance:', error);
        this.walletBalance = 0;
      }
    });
  }

  sendMoney(): void {
    if (!this.email || !this.amount) {
      alert('Please fill in email and amount fields');
      return;
    }

    if (this.amount <= 0) {
      alert('Amount must be greater than 0');
      return;
    }

    if (this.amount > this.walletBalance) {
      alert('Insufficient balance. Available balance: $' + this.walletBalance.toFixed(2));
      return;
    }

    this.isLoading = true;

    this.transactionService.sendMoney(this.email, this.amount, this.transactionPin || '').subscribe({
      next: (response: any) => {
        console.log('Send money response:', response);
        alert('Money sent successfully!');
        this.resetForm();
        this.loadWalletBalance(); // Refresh balance after successful transaction
        this.router.navigate(['/transactions']);
      },
      error: (error: any) => {
        console.error('Error sending money:', error);
        this.isLoading = false;
        
        let errorMessage = 'Failed to send money';
        if (error.error && error.error.error) {
          errorMessage = error.error.error;
        } else if (error.message) {
          errorMessage = error.message;
        } else if (error.status === 400) {
          errorMessage = 'Invalid request. Please check your input.';
        } else if (error.status === 401) {
          errorMessage = 'Authentication failed. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Insufficient permissions or invalid transaction PIN.';
        } else if (error.status === 404) {
          errorMessage = 'Recipient not found.';
        }
        
        alert(errorMessage);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private resetForm(): void {
    this.email = '';
    this.amount = null;
    this.transactionPin = '';
  }
}