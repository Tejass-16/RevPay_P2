import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
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

  testConnection(): Observable<any> {
    return this.http.get(`${this.apiUrl}/transactions/test`, { headers: this.getAuthHeaders() });
  }

  testRequest(): Observable<any> {
    const body = { amount: "1000" };
    console.log('Testing request with body:', body);
    return this.http.post(`${this.apiUrl}/transactions/test-request`, body, { headers: this.getAuthHeaders() });
  }

  debugRequest(): Observable<any> {
    const body = JSON.stringify({ amount: "1000" });
    console.log('Debug request with raw body:', body);
    return this.http.post(`${this.apiUrl}/transactions/debug-request`, body, { headers: this.getAuthHeaders() });
  }

  testSimpleFunds(): Observable<any> {
    const body = { amount: "1000" };
    console.log('Testing simple funds with body:', body);
    return this.http.post(`${this.apiUrl}/transactions/add-funds-simple`, body, { headers: this.getAuthHeaders() });
  }

  testAmount(): Observable<any> {
    const body = { amount: "1000" };
    console.log('Testing amount parsing with body:', body);
    return this.http.post(`${this.apiUrl}/transactions/test-amount`, body, { headers: this.getAuthHeaders() });
  }

  testTransaction(): Observable<any> {
    const body = { amount: "1000" };
    console.log('Testing transaction creation with body:', body);
    return this.http.post(`${this.apiUrl}/transactions/test-transaction`, body, { headers: this.getAuthHeaders() });
  }

  testEnum(): Observable<any> {
    const body = { test: "enum" };
    console.log('Testing enum conversion with body:', body);
    return this.http.post(`${this.apiUrl}/transactions/test-enum`, body, { headers: this.getAuthHeaders() });
  }

  testDatabase(): Observable<any> {
    console.log('Testing database connection...');
    return this.http.post(`${this.apiUrl}/transactions/test-database`, {}, { headers: this.getAuthHeaders() });
  }

  fixDatabase(): Observable<any> {
    console.log('Fixing database schema...');
    return this.http.post(`${this.apiUrl}/transactions/fix-database`, {}, { headers: this.getAuthHeaders() });
  }

  sendMoney(email: string, amount: number, transactionPin: string): Observable<any> {
    console.log('=== CLEAN SEND MONEY IMPLEMENTATION ===');
    
    // Simple validation
    if (!email || amount <= 0) {
      console.error('Invalid input:', { email, amount });
      return throwError(() => ({ error: 'Invalid email or amount' }));
    }

    const body = {
      recipientEmail: email,
      amount: Number(amount),
      description: 'Money transfer',
      transactionPin: transactionPin || ''
    };
    
    console.log('Clean send money request:', {
      url: `${this.apiUrl}/transactions/send`,
      body: body,
      hasAuth: !!this.authService.getToken()
    });
    
    const headers = this.getAuthHeaders();
    
    return this.http.post(`${this.apiUrl}/transactions/send`, body, { 
      headers: headers
    }).pipe(
      catchError((error: any) => {
        console.error('Clean send money error:', {
          status: error.status,
          message: error.message,
          error: error.error
        });
        return throwError(() => error);
      })
    );
  }

  getTransactions(): Observable<any[]> {
    return this.http.get<any>(`${this.apiUrl}/transactions/history/current`, { headers: this.getAuthHeaders() });
  }

  // Enhanced transaction history methods
  getTransactionHistory(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/transactions/history/current`, { headers: this.getAuthHeaders() });
  }

  getTransactionHistoryByType(type: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/transactions/history/current/type/${type}`, { headers: this.getAuthHeaders() });
  }

  getTransactionHistoryByDateRange(startDate: string, endDate: string): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<any>(`${this.apiUrl}/transactions/history/current/date-range`, { 
      headers: this.getAuthHeaders(),
      params: params 
    });
  }

  getWalletBalance(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/wallet/balance`, { headers: this.getAuthHeaders() });
  }

  addFunds(amount: number, paymentMethodId?: string, bankAccount?: string): Observable<any> {
    const body: any = { 
      amount: amount
    };
    
    if (paymentMethodId) {
      body.paymentMethodId = paymentMethodId;
    }
    
    if (bankAccount) {
      body.bankAccount = bankAccount;
    }
    
    return this.http.post(`${this.apiUrl}/transactions/add-funds`, body, { headers: this.getAuthHeaders() });
  }

  addInitialFunds(amount: number): Observable<any> {
    const body = { amount: amount.toString() };
    const token = this.authService.getToken();
    
    console.log('Adding initial funds request:', {
      url: `${this.apiUrl}/transactions/add-funds-initial`,
      body: body,
      token: token
    });

    return this.http.post(`${this.apiUrl}/transactions/add-funds-initial`, body, { headers: this.getAuthHeaders() });
  }

  withdraw(amount: number, bankAccount?: string): Observable<any> {
    const body: any = {
      amount: amount
    };
    
    if (bankAccount) {
      body.bankAccount = bankAccount;
    }
    
    return this.http.post(`${this.apiUrl}/transactions/withdraw`, body, { headers: this.getAuthHeaders() });
  }
}