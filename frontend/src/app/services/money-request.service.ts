import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class MoneyRequestService {
  private baseUrl = `${environment.apiUrl}/money-requests`;

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

  createRequest(receiverId: number, amount: number, description: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, {
      receiverId,
      amount,
      description
    }, { headers: this.getAuthHeaders() });
  }

  acceptRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${requestId}/accept`, {}, 
      { headers: this.getAuthHeaders() });
  }

  declineRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${requestId}/decline`, {}, 
      { headers: this.getAuthHeaders() });
  }

  cancelRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${requestId}/cancel`, {}, 
      { headers: this.getAuthHeaders() });
  }

  getPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/pending`, 
      { headers: this.getAuthHeaders() });
  }

  getSentRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/sent`, 
      { headers: this.getAuthHeaders() });
  }

  getReceivedRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/received`, 
      { headers: this.getAuthHeaders() });
  }

  getRequestById(requestId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${requestId}`, 
      { headers: this.getAuthHeaders() });
  }

  payRequest(requestId: number, transactionPin: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${requestId}/pay`, 
      { transactionPin }, 
      { headers: this.getAuthHeaders() });
  }
}