import { Component, OnInit } from '@angular/core';
import { InvoiceService } from '../services/invoice.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-customer-invoices',
  templateUrl: './customer-invoices.component.html',
  styleUrls: ['./customer-invoices.component.css']
})
export class CustomerInvoicesComponent implements OnInit {
  invoices: any[] = [];
  filteredInvoices: any[] = [];
  selectedInvoice: any = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  
  // Filter options
  selectedStatus = 'ALL';
  statusOptions = ['ALL', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED'];
  
  // Payment processing
  isProcessingPayment = false;

  constructor(
    private invoiceService: InvoiceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCustomerInvoices();
  }

  loadCustomerInvoices(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.invoiceService.getCustomerInvoices().subscribe({
      next: (data: any[]) => {
        this.invoices = data;
        this.filterInvoices();
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading customer invoices:', error);
        this.errorMessage = error.error?.error || 'Failed to load invoices';
        this.isLoading = false;
      }
    });
  }

  filterInvoices(): void {
    if (this.selectedStatus === 'ALL') {
      this.filteredInvoices = this.invoices;
    } else {
      this.filteredInvoices = this.invoices.filter(invoice => 
        invoice.status === this.selectedStatus
      );
    }
  }

  onStatusFilterChange(): void {
    this.filterInvoices();
  }

  viewInvoiceDetails(invoice: any): void {
    this.selectedInvoice = invoice;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeInvoiceDetails(): void {
    this.selectedInvoice = null;
  }

  canPayInvoice(invoice: any): boolean {
    return invoice.status === 'SENT';
  }

  isOverdue(invoice: any): boolean {
    return invoice.status === 'SENT' && new Date(invoice.dueDate) < new Date();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'SENT':
        return 'status-pending';
      case 'PAID':
        return 'status-paid';
      case 'OVERDUE':
        return 'status-overdue';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return 'status-draft';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  payInvoice(invoiceId: number): void {
    if (confirm('Are you sure you want to pay this invoice? The amount will be deducted from your wallet.')) {
      this.isProcessingPayment = true;
      this.errorMessage = '';
      this.successMessage = '';
      
      this.invoiceService.payInvoice(invoiceId).subscribe({
        next: (response: any) => {
          this.successMessage = 'Invoice paid successfully!';
          this.isProcessingPayment = false;
          this.selectedInvoice = null;
          this.loadCustomerInvoices(); // Refresh the list
        },
        error: (error: any) => {
          console.error('Error paying invoice:', error);
          this.errorMessage = error.error?.error || 'Failed to pay invoice';
          this.isProcessingPayment = false;
        }
      });
    }
  }

  checkPaymentAbility(invoiceId: number): void {
    this.invoiceService.canPayInvoice(invoiceId).subscribe({
      next: (canPay: boolean) => {
        if (!canPay) {
          this.errorMessage = 'This invoice cannot be paid at this time.';
        }
      },
      error: (error: any) => {
        console.error('Error checking payment ability:', error);
        this.errorMessage = 'Unable to verify payment status';
      }
    });
  }

  getTotalAmount(): number {
    return this.filteredInvoices.reduce((total, invoice) => total + invoice.totalAmount, 0);
  }

  getUnpaidAmount(): number {
    return this.filteredInvoices
      .filter(invoice => invoice.status === 'SENT')
      .reduce((total, invoice) => total + invoice.totalAmount, 0);
  }

  getOverdueAmount(): number {
    return this.filteredInvoices
      .filter(invoice => this.isOverdue(invoice))
      .reduce((total, invoice) => total + invoice.totalAmount, 0);
  }
}
