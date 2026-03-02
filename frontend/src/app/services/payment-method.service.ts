import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PaymentMethodService {
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

  getPaymentMethods(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/payment-methods`, { headers: this.getAuthHeaders() });
  }

  addPaymentMethod(paymentMethod: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/payment-methods`, paymentMethod, { headers: this.getAuthHeaders() });
  }

  updatePaymentMethod(id: number, paymentMethod: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/payment-methods/${id}`, paymentMethod, { headers: this.getAuthHeaders() });
  }

  deletePaymentMethod(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/payment-methods/${id}`, { headers: this.getAuthHeaders() });
  }

  setDefaultPaymentMethod(id: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/payment-methods/${id}/set-default`, {}, { headers: this.getAuthHeaders() });
  }
}