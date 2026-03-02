import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TransactionService } from '../services/transaction.service';
import { ProfileService } from '../services/profile.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: any = null;
  userProfile: any = null;
  walletBalance: number = 0;
  accountNumber: string = '';
  isLoading: boolean = true;

  constructor(private authService: AuthService, private transactionService: TransactionService, private profileService: ProfileService, private router: Router) {}

  testConnection(): void {
    console.log('Testing connection...');
    this.transactionService.testConnection().subscribe({
      next: (response) => {
        console.log('Connection test successful:', response);
        alert('Connection test successful!');
      },
      error: (error) => {
        console.error('Connection test failed:', error);
        alert('Connection test failed: ' + error.message);
      }
    });
  }

  testRequest(): void {
    console.log('Testing request parsing...');
    this.transactionService.testRequest().subscribe({
      next: (response) => {
        console.log('Request test successful:', response);
        alert('Request test successful! Check backend logs.');
      },
      error: (error) => {
        console.error('Request test failed:', error);
        alert('Request test failed: ' + error.message);
      }
    });
  }

  debugRequest(): void {
    console.log('Testing raw request...');
    this.transactionService.debugRequest().subscribe({
      next: (response) => {
        console.log('Debug request successful:', response);
        alert('Debug request successful! Check backend logs.');
      },
      error: (error) => {
        console.error('Debug request failed:', error);
        alert('Debug request failed: ' + error.message);
      }
    });
  }

  testSimpleFunds(): void {
    console.log('Testing simple funds...');
    this.transactionService.testSimpleFunds().subscribe({
      next: (response) => {
        console.log('Simple funds test successful:', response);
        alert('Simple funds test successful! Check backend logs.');
      },
      error: (error) => {
        console.error('Simple funds test failed:', error);
        alert('Simple funds test failed: ' + error.message);
      }
    });
  }

  testAmount(): void {
    console.log('Testing amount parsing...');
    this.transactionService.testAmount().subscribe({
      next: (response) => {
        console.log('Amount test successful:', response);
        alert('Amount test successful! Check backend logs.');
      },
      error: (error) => {
        console.error('Amount test failed:', error);
        alert('Amount test failed: ' + error.message);
      }
    });
  }

  testTransaction(): void {
    console.log('Testing transaction creation...');
    this.transactionService.testTransaction().subscribe({
      next: (response) => {
        console.log('Transaction test successful:', response);
        alert(`Transaction test successful! ID: ${response.transactionId}, Type: ${response.transactionType}`);
      },
      error: (error) => {
        console.error('Transaction test failed:', error);
        alert('Transaction test failed: ' + error.message);
      }
    });
  }

  testEnum(): void {
    console.log('Testing enum conversion...');
    this.transactionService.testEnum().subscribe({
      next: (response) => {
        console.log('Enum test successful:', response);
        alert(`Enum test successful! Value: ${response.enumValue}, Name: ${response.enumName}`);
      },
      error: (error) => {
        console.error('Enum test failed:', error);
        alert('Enum test failed: ' + error.message);
      }
    });
  }

  testDatabase(): void {
    console.log('Testing database connection...');
    this.transactionService.testDatabase().subscribe({
      next: (response) => {
        console.log('Database test successful:', response);
        alert(`Database test successful! Users: ${response.users}, Transactions: ${response.transactions}, Wallets: ${response.wallets}`);
      },
      error: (error) => {
        console.error('Database test failed:', error);
        alert('Database test failed: ' + error.message);
      }
    });
  }

  fixDatabase(): void {
    console.log('Fixing database schema...');
    this.transactionService.fixDatabase().subscribe({
      next: (response) => {
        console.log('Database fix instructions:', response);
        alert(`Database schema fix required! Please run the SQL script: ${response.action}`);
      },
      error: (error) => {
        console.error('Database fix failed:', error);
        alert('Database fix failed: ' + error.message);
      }
    });
  }

  addInitialFunds(): void {
    const amount = prompt('Enter amount to add to your account:');
    if (amount && !isNaN(parseFloat(amount)) && parseFloat(amount) > 0) {
      this.transactionService.addInitialFunds(parseFloat(amount)).subscribe({
        next: (response) => {
          alert('Initial funds added successfully!');
          this.loadDashboardData();
        },
        error: (error) => {
          console.error('Error adding initial funds:', error);
          let errorMessage = 'Failed to add initial funds. Please try again.';
          if (error.error && error.error.error) {
            errorMessage = `Error: ${error.error.error}`;
          } else if (error.message) {
            errorMessage = `Error: ${error.message}`;
          }
          alert(errorMessage);
        }
      });
    }
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadDashboardData();
      }
    });
  }

  loadDashboardData(): void {
    this.profileService.getWalletProfile().subscribe({
      next: (walletProfile) => {
        this.walletBalance = walletProfile.wallet.balance;
        this.accountNumber = walletProfile.wallet.accountNumber || 'Not available';
        this.userProfile = walletProfile.user;
        // Update currentUser with phone and other details
        this.currentUser = {
          ...this.currentUser,
          phone: walletProfile.user.phone || 'Not provided',
          isVerified: walletProfile.user.isVerified || false,
          createdAt: walletProfile.user.createdAt
        };
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading wallet balance:', error);
        this.walletBalance = 0;
        this.accountNumber = 'Not available';
        this.isLoading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
