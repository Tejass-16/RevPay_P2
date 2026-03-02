import { Component, OnInit } from '@angular/core';
import { MoneyRequestService } from '../services/money-request.service';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-money-requests',
  templateUrl: './money-requests.component.html',
  styleUrls: ['./money-requests.component.css']
})
export class MoneyRequestsComponent implements OnInit {
  activeTab: string = 'pending';
  pendingRequests: any[] = [];
  sentRequests: any[] = [];
  receivedRequests: any[] = [];
  
  // Form properties
  newReceiverId: number | null = null;
  newAmount: number | null = null;
  newDescription: string = '';

  constructor(
    private moneyRequestService: MoneyRequestService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  showTab(tab: string): void {
    this.activeTab = tab;
  }

  getCurrentUserEmail(): string {
    const currentUser = this.authService.currentUserValue;
    return currentUser ? currentUser.email : '';
  }

  loadRequests(): void {
    this.moneyRequestService.getPendingRequests().subscribe({
      next: (data) => this.pendingRequests = data,
      error: (err) => console.error('Error loading pending requests:', err)
    });

    this.moneyRequestService.getSentRequests().subscribe({
      next: (data) => this.sentRequests = data,
      error: (err) => console.error('Error loading sent requests:', err)
    });

    this.moneyRequestService.getReceivedRequests().subscribe({
      next: (data) => this.receivedRequests = data,
      error: (err) => console.error('Error loading received requests:', err)
    });
  }

  createRequest(): void {
    if (!this.newReceiverId || !this.newAmount || !this.newDescription) {
      alert('Please fill in all fields');
      return;
    }

    this.moneyRequestService.createRequest(
      this.newReceiverId, 
      this.newAmount, 
      this.newDescription
    ).subscribe({
      next: () => {
        this.loadRequests();
        this.resetForm();
        alert('Money request created successfully!');
      },
      error: (err) => {
        console.error('Error creating request:', err);
        alert('Failed to create money request');
      }
    });
  }

  acceptRequest(requestId: number): void {
    if (confirm('Are you sure you want to accept this request?')) {
      this.moneyRequestService.acceptRequest(requestId).subscribe({
        next: () => {
          this.loadRequests();
          alert('Request accepted successfully!');
        },
        error: (err) => {
          console.error('Error accepting request:', err);
          alert('Failed to accept request');
        }
      });
    }
  }

  declineRequest(requestId: number): void {
    if (confirm('Are you sure you want to decline this request?')) {
      this.moneyRequestService.declineRequest(requestId).subscribe({
        next: () => {
          this.loadRequests();
          alert('Request declined successfully!');
        },
        error: (err) => {
          console.error('Error declining request:', err);
          alert('Failed to decline request');
        }
      });
    }
  }

  cancelRequest(requestId: number): void {
    if (confirm('Are you sure you want to cancel this request?')) {
      this.moneyRequestService.cancelRequest(requestId).subscribe({
        next: () => {
          this.loadRequests();
          alert('Request cancelled successfully!');
        },
        error: (err) => {
          console.error('Error cancelling request:', err);
          alert('Failed to cancel request');
        }
      });
    }
  }

  viewRequestDetails(requestId: number): void {
    this.router.navigate(['/money-requests', requestId]);
  }

  private resetForm(): void {
    this.newReceiverId = null;
    this.newAmount = null;
    this.newDescription = '';
  }

  // Helper method to get amount for confirmation dialogs
  private getAmountForRequest(requestId: number): string {
    const request = this.pendingRequests.find(req => req.id === requestId);
    return request ? request.amount : '0';
  }
}