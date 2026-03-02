import { Component, OnInit } from '@angular/core';
import { TransactionService } from '../services/transaction.service';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.css']
})
export class TransactionsComponent implements OnInit {
  transactions: any[] = [];
  isLoading: boolean = true;
  currentPage: number = 1;
  hasMorePages: boolean = false;
  pageSize: number = 10;

  constructor(private transactionService: TransactionService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  // Helper methods for enhanced table display
  getUserRole(transaction: any): string {
    if (transaction.isCurrentUserSender) {
      return 'Sender';
    } else if (transaction.isCurrentUserReceiver) {
      return 'Receiver';
    } else {
      return 'System';
    }
  }

  getTransactionTypeLabel(type: string): string {
    // Normalize transaction type to handle both old and new values
    const normalizedType = this.normalizeTransactionType(type);
    
    switch (normalizedType) {
      case 'SEND':
        return 'Money Sent';
      case 'RECEIVE':
        return 'Money Received';
      case 'ADD_FUNDS':
        return 'Add Funds';
      case 'WITHDRAW':
        return 'Withdraw';
      case 'INITIAL':
        return 'Initial Deposit';
      default:
        return type?.replace('_', ' ') || 'Unknown';
    }
  }

  normalizeTransactionType(type: string): string {
    if (!type) {
      return 'UNKNOWN';
    }
    
    switch (type.toUpperCase()) {
      case 'S':
      case 'SEND':
        return 'SEND';
      case 'R':
      case 'RECEIVE':
        return 'RECEIVE';
      case 'A':
      case 'ADD_FUNDS':
        return 'ADD_FUNDS';
      case 'W':
      case 'WITHDRAW':
        return 'WITHDRAW';
      case 'I':
      case 'INITIAL':
        return 'INITIAL';
      default:
        return type.toUpperCase();
    }
  }

  getTypeClass(type: string): { [key: string]: boolean } {
    const normalizedType = this.normalizeTransactionType(type);
    
    return {
      'type-send': normalizedType === 'SEND',
      'type-receive': normalizedType === 'RECEIVE',
      'type-add-funds': normalizedType === 'ADD_FUNDS',
      'type-withdraw': normalizedType === 'WITHDRAW',
      'type-initial': normalizedType === 'INITIAL',
      'type-default': !normalizedType || !['SEND', 'RECEIVE', 'ADD_FUNDS', 'WITHDRAW', 'INITIAL'].includes(normalizedType)
    };
  }

  getDebitCreditDescription(transaction: any): string {
    if (transaction.transactionNature === 'DEBIT') {
      return 'Money deducted';
    } else if (transaction.transactionNature === 'CREDIT') {
      return 'Money added';
    } else {
      return 'Unknown';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return '✓';
      case 'PENDING':
        return '⏳';
      case 'FAILED':
        return '✗';
      default:
        return '?';
    }
  }

  loadTransactions(): void {
    this.isLoading = true;
    this.transactionService.getTransactions().subscribe({
      next: (response: any) => {
        console.log('Transactions response:', response);
        // Handle the new enhanced API response format
        if (response && response.success && response.transactions) {
          this.transactions = response.transactions;
        } else if (Array.isArray(response)) {
          // Handle legacy response format (direct array)
          this.transactions = response;
        } else {
          console.error('Unexpected response format:', response);
          this.transactions = [];
        }
        this.hasMorePages = this.transactions.length >= this.pageSize;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        // Fallback to sample data for demo
        this.transactions = [
          { 
            id: 1, 
            amount: 100.00, 
            transactionType: 'SEND', 
            transactionNature: 'DEBIT',
            description: 'Payment to John', 
            createdAt: '2024-01-15T10:30:00',
            status: 'COMPLETED',
            counterpartyName: 'John Doe',
            counterpartyEmail: 'john@example.com',
            isCurrentUserSender: true,
            isCurrentUserReceiver: false
          },
          { 
            id: 2, 
            amount: 250.00, 
            transactionType: 'RECEIVE',
            transactionNature: 'CREDIT',
            description: 'Payment from Sarah', 
            createdAt: '2024-01-14T14:20:00',
            status: 'COMPLETED',
            counterpartyName: 'Sarah Smith',
            counterpartyEmail: 'sarah@example.com',
            isCurrentUserSender: false,
            isCurrentUserReceiver: true
          },
          { 
            id: 3, 
            amount: 50.00, 
            transactionType: 'SEND',
            transactionNature: 'DEBIT',
            description: 'Coffee shop', 
            createdAt: '2024-01-13T08:15:00',
            status: 'COMPLETED',
            counterpartyName: 'Coffee Shop',
            counterpartyEmail: 'coffee@shop.com',
            isCurrentUserSender: true,
            isCurrentUserReceiver: false
          }
        ];
        this.hasMorePages = false;
        this.isLoading = false;
      }
    });
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadTransactions();
    }
  }

  nextPage(): void {
    if (this.hasMorePages) {
      this.currentPage++;
      this.loadTransactions();
    }
  }
}
