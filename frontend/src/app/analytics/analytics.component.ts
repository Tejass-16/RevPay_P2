import { Component, OnInit } from '@angular/core';
import { AnalyticsService } from '../services/analytics.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent implements OnInit {
  userRole: string = '';
  activeTab: string = 'personal';
  
  // Personal Analytics Data
  personalAnalytics: any = {};
  personalTransactions: any[] = [];
  personalFilters = {
    type: '',
    startDate: '',
    endDate: '',
    minAmount: undefined,
    maxAmount: undefined
  };
  
  // Business Analytics Data
  businessAnalytics: any = {};
  topCustomers: any[] = [];
  
  // Loading states
  personalLoading: boolean = false;
  businessLoading: boolean = false;

  constructor(
    private analyticsService: AnalyticsService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log('AnalyticsComponent initialized');
    const currentUser = this.authService.currentUserValue;
    this.userRole = currentUser?.role || '';
    console.log('User role:', this.userRole);
    this.loadPersonalAnalytics();
    
    if (this.userRole === 'BUSINESS') {
      this.loadBusinessAnalytics();
    }
  }

  // Tab Management
  switchTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'personal' && Object.keys(this.personalAnalytics).length === 0) {
      this.loadPersonalAnalytics();
    } else if (tab === 'business' && Object.keys(this.businessAnalytics).length === 0) {
      this.loadBusinessAnalytics();
    }
  }

  // Personal Analytics Methods
  loadPersonalAnalytics(): void {
    console.log('Loading personal analytics...');
    this.personalLoading = true;
    this.analyticsService.getPersonalAnalytics().subscribe({
      next: (data) => {
        console.log('Personal analytics data received:', data);
        this.personalAnalytics = data;
        this.personalTransactions = data.recentTransactions || [];
        this.personalLoading = false;
      },
      error: (error) => {
        console.error('Error loading personal analytics:', error);
        this.personalLoading = false;
      }
    });
  }

  applyPersonalFilters(): void {
    this.personalLoading = true;
    this.analyticsService.getPersonalTransactionsWithFilters(this.personalFilters).subscribe({
      next: (data) => {
        this.personalTransactions = data.transactions || [];
        this.personalLoading = false;
      },
      error: (error) => {
        console.error('Error applying filters:', error);
        this.personalLoading = false;
      }
    });
  }

  clearPersonalFilters(): void {
    this.personalFilters = {
      type: '',
      startDate: '',
      endDate: '',
      minAmount: undefined,
      maxAmount: undefined
    };
    this.loadPersonalAnalytics();
  }

  exportPersonalTransactions(format: string): void {
    if (format === 'csv') {
      this.analyticsService.exportPersonalTransactionsCSV().subscribe({
        next: (blob) => {
          this.analyticsService.downloadFile(blob, 'transactions.csv');
        },
        error: (error) => {
          console.error('Error exporting CSV:', error);
        }
      });
    } else if (format === 'pdf') {
      this.analyticsService.exportPersonalTransactionsPDF().subscribe({
        next: (blob) => {
          this.analyticsService.downloadFile(blob, 'transactions.pdf');
        },
        error: (error) => {
          console.error('Error exporting PDF:', error);
        }
      });
    }
  }

  // Business Analytics Methods
  loadBusinessAnalytics(): void {
    console.log('Loading business analytics...');
    this.businessLoading = true;
    
    // Load business analytics data
    this.analyticsService.getTopCustomers().subscribe({
      next: (data) => {
        console.log('Top customers data received:', data);
        this.topCustomers = data.topCustomers || [];
      },
      error: (error) => {
        console.error('Error loading top customers:', error);
      }
    });

    this.analyticsService.getBusinessCharts().subscribe({
      next: (data) => {
        console.log('Business charts data received:', data);
        this.businessAnalytics = data;
        this.businessLoading = false;
      },
      error: (error) => {
        console.error('Error loading business charts:', error);
        this.businessLoading = false;
      }
    });
  }

  exportBusinessInvoices(): void {
    this.analyticsService.exportBusinessInvoicesCSV().subscribe({
      next: (blob) => {
        this.analyticsService.downloadFile(blob, 'invoices.csv');
      },
      error: (error) => {
        console.error('Error exporting invoices:', error);
      }
    });
  }

  // Utility Methods
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getTransactionTypeIcon(type: string): string {
    switch (type) {
      case 'SENT':
        return '↑';
      case 'RECEIVED':
        return '↓';
      case 'WITHDRAWN':
        return '↗';
      default:
        return '→';
    }
  }

  getTransactionTypeColor(type: string): string {
    switch (type) {
      case 'SENT':
        return '#ff6b6b';
      case 'RECEIVED':
        return '#51cf66';
      case 'WITHDRAWN':
        return '#ff922b';
      default:
        return '#868e96';
    }
  }
}
