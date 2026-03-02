import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { InvoiceService } from '../services/invoice.service';

@Component({
  selector: 'app-invoices',
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {
  invoices: any[] = [];
  filteredInvoices: any[] = [];
  selectedInvoice: any = null;
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  showCreateForm: boolean = false;
  showEditForm: boolean = false;
  selectedStatus: string = 'all';
  
  // Form data
  invoiceForm: any = {
    customerName: '',
    customerEmail: '',
    customerPhone: '',
    customerAddress: '',
    dueDate: '',
    paymentTerms: '',
    notes: '',
    items: [],
    subtotal: 0,
    taxAmount: 0,
    totalAmount: 0
  };

  // New item form
  newItemForm: any = {
    description: '',
    quantity: 1,
    unitPrice: 0
  };

  constructor(private invoiceService: InvoiceService, private router: Router) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.isLoading = true;
    this.invoiceService.getMyInvoices().subscribe({
      next: (response: any) => {
        this.invoices = response;
        this.filteredInvoices = response;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading invoices:', error);
        this.errorMessage = 'Failed to load invoices';
        this.isLoading = false;
      }
    });
  }

  filterInvoices(): void {
    if (this.selectedStatus === 'all') {
      this.filteredInvoices = this.invoices;
    } else {
      this.filteredInvoices = this.invoices.filter(invoice => invoice.status === this.selectedStatus);
    }
  }

  createInvoice(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Validate form
    if (!this.invoiceForm.customerName || !this.invoiceForm.customerEmail || !this.invoiceForm.dueDate) {
      this.errorMessage = 'Please fill in all required fields';
      this.isLoading = false;
      return;
    }

    if (this.invoiceForm.items.length === 0) {
      this.errorMessage = 'Please add at least one item to the invoice';
      this.isLoading = false;
      return;
    }

    // Calculate totals before sending
    this.calculateTotals();

    this.invoiceService.createInvoice(this.invoiceForm).subscribe({
      next: (response: any) => {
        this.successMessage = 'Invoice created successfully!';
        this.isLoading = false;
        this.showCreateForm = false;
        this.resetForm();
        this.loadInvoices();
      },
      error: (error: any) => {
        console.error('Error creating invoice:', error);
        this.errorMessage = error.error?.error || 'Failed to create invoice';
        this.isLoading = false;
      }
    });
  }

  updateInvoice(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.selectedInvoice) {
      this.errorMessage = 'No invoice selected';
      this.isLoading = false;
      return;
    }

    this.invoiceService.updateInvoice(this.selectedInvoice.id, this.invoiceForm).subscribe({
      next: (response: any) => {
        this.successMessage = 'Invoice updated successfully!';
        this.isLoading = false;
        this.showEditForm = false;
        this.selectedInvoice = null;
        this.resetForm();
        this.loadInvoices();
      },
      error: (error: any) => {
        console.error('Error updating invoice:', error);
        this.errorMessage = error.error?.error || 'Failed to update invoice';
        this.isLoading = false;
      }
    });
  }

  deleteInvoice(id: number): void {
    if (confirm('Are you sure you want to delete this invoice?')) {
      this.isLoading = true;
      this.invoiceService.deleteInvoice(id).subscribe({
        next: () => {
          this.successMessage = 'Invoice deleted successfully!';
          this.isLoading = false;
          this.loadInvoices();
        },
        error: (error: any) => {
          console.error('Error deleting invoice:', error);
          this.errorMessage = error.error?.error || 'Failed to delete invoice';
          this.isLoading = false;
        }
      });
    }
  }

  sendInvoice(id: number): void {
    this.isLoading = true;
    this.invoiceService.sendInvoice(id).subscribe({
      next: (response: any) => {
        this.successMessage = 'Invoice sent successfully!';
        this.isLoading = false;
        this.loadInvoices();
      },
      error: (error: any) => {
        console.error('Error sending invoice:', error);
        this.errorMessage = error.error?.error || 'Failed to send invoice';
        this.isLoading = false;
      }
    });
  }

  markAsPaid(id: number): void {
    if (confirm('Mark this invoice as paid? This action cannot be undone.')) {
      this.isLoading = true;
      this.invoiceService.markInvoiceAsPaid(id).subscribe({
        next: (response: any) => {
          this.successMessage = 'Invoice marked as paid!';
          this.isLoading = false;
          this.loadInvoices();
        },
        error: (error: any) => {
          console.error('Error marking invoice as paid:', error);
          this.errorMessage = error.error?.error || 'Failed to mark invoice as paid';
          this.isLoading = false;
        }
      });
    }
  }

  cancelInvoice(id: number): void {
    if (confirm('Cancel this invoice? This action cannot be undone.')) {
      this.isLoading = true;
      this.invoiceService.cancelInvoice(id).subscribe({
        next: (response: any) => {
          this.successMessage = 'Invoice cancelled!';
          this.isLoading = false;
          this.loadInvoices();
        },
        error: (error: any) => {
          console.error('Error cancelling invoice:', error);
          this.errorMessage = error.error?.error || 'Failed to cancel invoice';
          this.isLoading = false;
        }
      });
    }
  }

  generatePaymentLink(id: number): void {
    this.isLoading = true;
    this.invoiceService.generatePaymentLink(id).subscribe({
      next: (response: any) => {
        this.successMessage = `Payment link generated: ${response}`;
        this.isLoading = false;
        // Copy to clipboard
        navigator.clipboard.writeText(response);
      },
      error: (error: any) => {
        console.error('Error generating payment link:', error);
        this.errorMessage = error.error?.error || 'Failed to generate payment link';
        this.isLoading = false;
      }
    });
  }

  addItem(): void {
    if (!this.newItemForm.description || this.newItemForm.quantity <= 0 || this.newItemForm.unitPrice <= 0) {
      this.errorMessage = 'Please fill in all item fields correctly';
      return;
    }

    const item = {
      description: this.newItemForm.description,
      quantity: this.newItemForm.quantity,
      unitPrice: this.newItemForm.unitPrice,
      lineTotal: this.newItemForm.quantity * this.newItemForm.unitPrice
    };

    this.invoiceForm.items.push(item);
    this.calculateTotals(); // Calculate totals after adding item
    this.newItemForm = {
      description: '',
      quantity: 1,
      unitPrice: 0
    };
  }

  removeItem(index: number): void {
    this.invoiceForm.items.splice(index, 1);
    this.calculateTotals();
  }

  calculateSubtotal(): number {
    return this.invoiceForm.items.reduce((total: number, item: any) => total + item.lineTotal, 0);
  }

  calculateTotals(): void {
    this.invoiceForm.subtotal = this.calculateSubtotal();
    this.invoiceForm.taxAmount = this.invoiceForm.subtotal * 0.18; // 18% tax
    this.invoiceForm.totalAmount = this.invoiceForm.subtotal + this.invoiceForm.taxAmount;
  }

  calculateTax(): number {
    return this.calculateSubtotal() * 0.18; // 18% GST
  }

  calculateTotal(): number {
    return this.calculateSubtotal() + this.calculateTax();
  }

  editInvoice(invoice: any): void {
    this.selectedInvoice = invoice;
    this.invoiceForm = {
      customerName: invoice.customerName,
      customerEmail: invoice.customerEmail,
      customerPhone: invoice.customerPhone || '',
      customerAddress: invoice.customerAddress || '',
      dueDate: invoice.dueDate,
      paymentTerms: invoice.paymentTerms || '',
      notes: invoice.notes || '',
      items: invoice.items || []
    };
    this.showEditForm = true;
  }

  resetForm(): void {
    this.invoiceForm = {
      customerName: '',
      customerEmail: '',
      customerPhone: '',
      customerAddress: '',
      dueDate: '',
      paymentTerms: '',
      notes: '',
      items: []
    };
    this.newItemForm = {
      description: '',
      quantity: 1,
      unitPrice: 0
    };
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'DRAFT': return 'status-draft';
      case 'SENT': return 'status-sent';
      case 'PAID': return 'status-paid';
      case 'OVERDUE': return 'status-overdue';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  }

  isOverdue(invoice: any): boolean {
    return invoice.status === 'SENT' && new Date(invoice.dueDate) < new Date();
  }
}
