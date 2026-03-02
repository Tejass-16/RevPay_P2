import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // Helper method for authentication headers
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (token) {
      return new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });
    } else {
      return new HttpHeaders({
        'Content-Type': 'application/json'
      });
    }
  }

  // Personal Analytics Methods
  getPersonalAnalytics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/personal/summary`, { 
      headers: this.getAuthHeaders() 
    });
  }

  getPersonalTransactionsWithFilters(filters?: {
    type?: string;
    startDate?: string;
    endDate?: string;
    minAmount?: number;
    maxAmount?: number;
  }): Observable<any> {
    let params = '';
    if (filters) {
      const queryParams = new URLSearchParams();
      if (filters.type) queryParams.append('type', filters.type);
      if (filters.startDate) queryParams.append('startDate', filters.startDate);
      if (filters.endDate) queryParams.append('endDate', filters.endDate);
      if (filters.minAmount) queryParams.append('minAmount', filters.minAmount.toString());
      if (filters.maxAmount) queryParams.append('maxAmount', filters.maxAmount.toString());
      params = '?' + queryParams.toString();
    }

    return this.http.get(`${this.apiUrl}/analytics/personal/transactions/filters${params}`, { 
      headers: this.getAuthHeaders() 
    });
  }

  exportPersonalTransactionsCSV(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/analytics/personal/export/csv`, { 
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  exportPersonalTransactionsPDF(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/analytics/personal/export/pdf`, { 
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  // Business Analytics Methods
  getDailyRevenue(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/business/revenue/daily`, { 
      headers: this.getAuthHeaders() 
    });
  }

  getWeeklyRevenue(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/business/revenue/weekly`, { 
      headers: this.getAuthHeaders() 
    });
  }

  getMonthlyRevenue(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/business/revenue/monthly`, { 
      headers: this.getAuthHeaders() 
    });
  }

  getTopCustomers(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/business/customers/top`, { 
      headers: this.getAuthHeaders() 
    });
  }

  getBusinessCharts(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/business/trends/charts`, { 
      headers: this.getAuthHeaders() 
    });
  }

  exportBusinessInvoicesCSV(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/analytics/business/export/csv`, { 
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  // Utility Methods
  downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  // Chart Data Preparation Methods
  prepareRevenueChartData(revenueTrends: any[]): any[] {
    return revenueTrends.map(trend => ({
      date: trend.label,
      revenue: trend.revenue,
      outstanding: trend.outstandingRevenue,
      transactions: trend.transactionCount
    }));
  }

  prepareCustomerChartData(topCustomers: any[]): any[] {
    return topCustomers.map(customer => ({
      name: customer.customerName,
      email: customer.customerEmail,
      totalSpent: customer.totalSpent,
      transactionCount: customer.transactionCount,
      invoiceCount: customer.invoiceCount
    }));
  }

  preparePersonalTransactionChartData(transactions: any[]): any[] {
    const groupedData = transactions.reduce((acc, transaction) => {
      const date = new Date(transaction.createdAt).toISOString().split('T')[0];
      if (!acc[date]) {
        acc[date] = { sent: 0, received: 0, withdrawn: 0 };
      }
      
      if (transaction.type === 'SENT') {
        acc[date].sent += transaction.amount;
      } else if (transaction.type === 'RECEIVED') {
        acc[date].received += transaction.amount;
      } else if (transaction.type === 'WITHDRAWN') {
        acc[date].withdrawn += transaction.amount;
      }
      
      return acc;
    }, {});

    return Object.keys(groupedData).map(date => ({
      date,
      sent: groupedData[date].sent,
      received: groupedData[date].received,
      withdrawn: groupedData[date].withdrawn
    }));
  }

  // Filter Methods
  filterTransactionsByType(transactions: any[], type: string): any[] {
    if (!type || type === 'ALL') return transactions;
    return transactions.filter(transaction => transaction.type === type);
  }

  filterTransactionsByDateRange(transactions: any[], startDate: string, endDate: string): any[] {
    if (!startDate && !endDate) return transactions;
    
    return transactions.filter(transaction => {
      const transactionDate = new Date(transaction.createdAt);
      const start = startDate ? new Date(startDate) : new Date('1900-01-01');
      const end = endDate ? new Date(endDate) : new Date('2100-12-31');
      
      return transactionDate >= start && transactionDate <= end;
    });
  }

  filterTransactionsByAmount(transactions: any[], minAmount?: number, maxAmount?: number): any[] {
    return transactions.filter(transaction => {
      const amount = parseFloat(transaction.amount);
      if (minAmount !== undefined && amount < minAmount) return false;
      if (maxAmount !== undefined && amount > maxAmount) return false;
      return true;
    });
  }

  // Calculation Methods
  calculateTotalAmount(transactions: any[], type?: string): number {
    let filteredTransactions = transactions;
    if (type) {
      filteredTransactions = this.filterTransactionsByType(transactions, type);
    }
    
    return filteredTransactions.reduce((total, transaction) => {
      return total + parseFloat(transaction.amount);
    }, 0);
  }

  calculateAverageTransactionAmount(transactions: any[]): number {
    if (transactions.length === 0) return 0;
    const total = this.calculateTotalAmount(transactions);
    return total / transactions.length;
  }

  getTransactionTypeDistribution(transactions: any[]): any {
    const distribution = {
      SENT: 0,
      RECEIVED: 0,
      WITHDRAWN: 0
    };

    transactions.forEach(transaction => {
      const transactionType = transaction.type as keyof typeof distribution;
      if (distribution.hasOwnProperty(transactionType)) {
        distribution[transactionType]++;
      }
    });

    return distribution;
  }
}
