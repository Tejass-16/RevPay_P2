import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SocialPaymentService {
  private baseUrl = `${environment.apiUrl}/social-payments`;

  constructor(private http: HttpClient) {}

  // Helper method for authentication headers
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('currentUser');
    const user = token ? JSON.parse(token) : null;
    const authToken = user ? user.token : null;
    
    if (authToken) {
      return new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      });
    } else {
      return new HttpHeaders({
        'Content-Type': 'application/json'
      });
    }
  }

  createPayment(amount: number, platform: string, description: string, externalReference?: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, {
      amount,
      platform,
      description,
      externalReference
    }, { headers: this.getAuthHeaders() });
  }

  completePayment(paymentId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${paymentId}/complete`, {}, { headers: this.getAuthHeaders() });
  }

  failPayment(paymentId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${paymentId}/fail`, {}, { headers: this.getAuthHeaders() });
  }

  getUserPayments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/user`, { headers: this.getAuthHeaders() });
  }

  getPaymentsByPlatform(platform: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/platform/${platform}`, { headers: this.getAuthHeaders() });
  }

  getCompletedPayments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/completed`, { headers: this.getAuthHeaders() });
  }

  getPaymentById(paymentId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${paymentId}`, { headers: this.getAuthHeaders() });
  }
}