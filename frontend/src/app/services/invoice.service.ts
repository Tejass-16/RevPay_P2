import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
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

  createInvoice(invoice: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/invoices/create`, invoice, { headers: this.getAuthHeaders() });
  }

  updateInvoice(id: number, invoice: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/invoices/update/${id}`, invoice, { headers: this.getAuthHeaders() });
  }

  deleteInvoice(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/invoices/delete/${id}`, { headers: this.getAuthHeaders() });
  }

  getMyInvoices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/my-invoices`, { headers: this.getAuthHeaders() });
  }

  getInvoicesByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/my-invoices/${status}`, { headers: this.getAuthHeaders() });
  }

  getInvoice(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/invoices/${id}`, { headers: this.getAuthHeaders() });
  }

  sendInvoice(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/invoices/send/${id}`, {}, { headers: this.getAuthHeaders() });
  }

  markInvoiceAsPaid(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/invoices/mark-paid/${id}`, {}, { headers: this.getAuthHeaders() });
  }

  cancelInvoice(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/invoices/cancel/${id}`, {}, { headers: this.getAuthHeaders() });
  }

  getOverdueInvoices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/overdue`, { headers: this.getAuthHeaders() });
  }

  generatePaymentLink(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/invoices/payment-link/${id}`, { headers: this.getAuthHeaders() });
  }

  addItemToInvoice(invoiceId: number, item: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/invoices/add-item/${invoiceId}`, item, { headers: this.getAuthHeaders() });
  }

  removeItemFromInvoice(invoiceId: number, itemId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/invoices/remove-item/${invoiceId}/${itemId}`, { headers: this.getAuthHeaders() });
  }

  // Customer-facing methods
  getCustomerInvoices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/customer/my-invoices`, { headers: this.getAuthHeaders() });
  }

  getCustomerInvoicesByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/customer/my-invoices/${status}`, { headers: this.getAuthHeaders() });
  }

  getCustomerInvoiceByNumber(invoiceNumber: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/invoices/customer/invoice/${invoiceNumber}`, { headers: this.getAuthHeaders() });
  }

  getCustomerOverdueInvoices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/customer/overdue`, { headers: this.getAuthHeaders() });
  }

  payInvoice(invoiceId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/invoices/customer/pay/${invoiceId}`, {}, { headers: this.getAuthHeaders() });
  }

  canPayInvoice(invoiceId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/invoices/customer/can-pay/${invoiceId}`, { headers: this.getAuthHeaders() });
  }
}