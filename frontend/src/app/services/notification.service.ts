import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface Notification {
  id: number;
  type: 'LOW_BALANCE' | 'PAYMENT_RECEIVED' | 'TRANSACTION' | 'MONEY_REQUEST' | 'SECURITY_CHANGE';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  userId: number;
}

export interface NotificationPreferences {
  lowBalanceAlerts: boolean;
  moneyReceivedAlerts: boolean;
  moneyRequestAlerts: boolean;
  securityChangeAlerts: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = environment.apiUrl;
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService) {}

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

  // Get all notifications for current user
  getNotifications(): Observable<Notification[]> {
    return this.http.get<any>(`${this.apiUrl}/notifications`, { headers: this.getAuthHeaders() });
  }

  // Mark notification as read
  markAsRead(notificationId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/notifications/mark-read/${notificationId}`, {}, { headers: this.getAuthHeaders() });
  }

  // Get notification preferences
  getNotificationPreferences(): Observable<NotificationPreferences> {
    return this.http.get<any>(`${this.apiUrl}/notifications/preferences`, { headers: this.getAuthHeaders() });
  }

  // Update notification preferences
  updateNotificationPreferences(preferences: NotificationPreferences): Observable<any> {
    return this.http.put(`${this.apiUrl}/notifications/preferences`, preferences, { headers: this.getAuthHeaders() });
  }

  // Update local notifications (for real-time updates)
  updateNotifications(notifications: Notification[]): void {
    this.notificationsSubject.next(notifications);
  }

  // Add new notification (for real-time updates)
  addNotification(notification: Notification): void {
    const currentNotifications = this.notificationsSubject.value;
    this.notificationsSubject.next([notification, ...currentNotifications]);
  }

  // Clear all notifications
  clearNotifications(): void {
    this.notificationsSubject.next([]);
  }
}