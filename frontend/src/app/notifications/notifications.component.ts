import { Component, OnInit, OnDestroy } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { NotificationService, Notification, NotificationPreferences } from '../services/notification.service';

import { Subscription } from 'rxjs';

import { CommonModule } from '@angular/common';



@Component({

  selector: 'app-notifications',

  templateUrl: './notifications.component.html',

  styleUrls: ['./notifications.component.css'],

  standalone: true,

  imports: [FormsModule,CommonModule]

})

export class NotificationsComponent implements OnInit, OnDestroy {

  notifications: Notification[] = [];

  preferences: NotificationPreferences = {

    lowBalanceAlerts: true,

    moneyReceivedAlerts: true,

    moneyRequestAlerts: true,

    securityChangeAlerts: true

  };

  isLoading: boolean = false;

  private notificationSubscription: Subscription | null = null;



  constructor(private notificationService: NotificationService) {}



  ngOnInit(): void {

    this.loadNotifications();

    this.loadPreferences();

    this.notificationSubscription = this.notificationService.notifications$.subscribe(

      notifications => {

        this.notifications = notifications;

      }

    );

  }



  ngOnDestroy(): void {

    if (this.notificationSubscription) {

      this.notificationSubscription.unsubscribe();

    }

  }



  loadNotifications(): void {

    this.isLoading = true;

    this.notificationService.getNotifications().subscribe({

      next: (response: any) => {

        console.log('Notifications response:', response);

        // Handle the new API response format

        if (response && response.success && response.notifications) {

          this.notifications = response.notifications;

        } else if (Array.isArray(response)) {

          // Handle legacy response format (direct array)

          this.notifications = response;

        } else {

          console.error('Unexpected response format:', response);

          this.notifications = [];

        }

        this.isLoading = false;

      },

      error: (error) => {

        console.error('Error loading notifications:', error);

        this.isLoading = false;

      }

    });

  }



  loadPreferences(): void {

    this.notificationService.getNotificationPreferences().subscribe({

      next: (response: any) => {

        console.log('Preferences response:', response);

        // Handle the new API response format

        if (response && response.success && response.preferences) {

          this.preferences = response.preferences;

        } else if (response && typeof response === 'object') {

          // Handle legacy response format (direct object)

          this.preferences = response;

        } else {

          console.error('Unexpected preferences response format:', response);

        }

      },

      error: (error) => {

        console.error('Error loading preferences:', error);

      }

    });

  }



  markAsRead(notificationId: number): void {

    this.notificationService.markAsRead(notificationId).subscribe({

      next: () => {

        const notification = this.notifications.find(n => n.id === notificationId);

        if (notification) {

          notification.isRead = true;

        }

      },

      error: (error) => {

        console.error('Error marking notification as read:', error);

      }

    });

  }



  updatePreferences(): void {

    this.notificationService.updateNotificationPreferences(this.preferences).subscribe({

      next: () => {

        alert('Notification preferences updated successfully!');

      },

      error: (error) => {

        console.error('Error updating preferences:', error);

        alert('Failed to update notification preferences');

      }

    });

  }



  clearAll(): void {

    this.notificationService.clearNotifications();

  }



  getUnreadCount(): number {

    return this.notifications.filter(n => !n.isRead).length;

  }



  getNotificationIcon(type: string): string {

    switch (type) {

      case 'LOW_BALANCE':

        return '💰';

      case 'MONEY_RECEIVED':

        return '💵';

      case 'MONEY_REQUEST':

        return '📋';

      case 'SECURITY_CHANGE':

        return '🔒';

      default:

        return '📢';

    }

  }

}