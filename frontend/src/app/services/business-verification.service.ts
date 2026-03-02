import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class BusinessVerificationService {
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

  uploadVerificationDocuments(formData: FormData): Observable<any> {
    const token = this.authService.getToken();
    if (token) {
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`
      });
      return this.http.post(`${this.apiUrl}/business-verification/upload-documents`, formData, { headers });
    } else {
      return this.http.post(`${this.apiUrl}/business-verification/upload-documents`, formData);
    }
  }

  getVerificationStatus(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/business-verification/status`, { headers: this.getAuthHeaders() });
  }

  resubmitVerification(requestData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/business-verification/resubmit`, requestData, { headers: this.getAuthHeaders() });
  }
}