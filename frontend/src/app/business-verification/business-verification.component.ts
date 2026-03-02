import { Component, OnInit } from '@angular/core';
import { BusinessVerificationService } from '../services/business-verification.service';

@Component({
  selector: 'app-business-verification',
  templateUrl: './business-verification.component.html',
  styleUrls: ['./business-verification.component.css']
})
export class BusinessVerificationComponent implements OnInit {
  verificationStatus: any = {};
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  showUploadForm: boolean = false;
  showResubmitForm: boolean = false;
  
  // File upload
  selectedFiles: any = {
    businessRegistration: null,
    taxDocument: null,
    addressProof: null,
    identityProof: null
  };

  // Resubmit form
  resubmitForm: any = {
    businessName: '',
    businessType: '',
    taxId: '',
    businessAddress: ''
  };

  constructor(private businessVerificationService: BusinessVerificationService) {}

  ngOnInit(): void {
    this.loadVerificationStatus();
  }

  loadVerificationStatus(): void {
    this.isLoading = true;
    this.businessVerificationService.getVerificationStatus().subscribe({
      next: (response: any) => {
        this.verificationStatus = response;
        this.resubmitForm = {
          businessName: response.businessName || '',
          businessType: response.businessType || '',
          taxId: response.taxId || '',
          businessAddress: response.businessAddress || ''
        };
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading verification status:', error);
        this.errorMessage = 'Failed to load verification status';
        this.isLoading = false;
      }
    });
  }

  onFileSelected(event: any, documentType: string): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFiles[documentType] = file;
    }
  }

  uploadDocuments(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Validate required files
    if (!this.selectedFiles.businessRegistration || !this.selectedFiles.taxDocument || !this.selectedFiles.addressProof) {
      this.errorMessage = 'Please upload all required documents (Business Registration, Tax Document, Address Proof)';
      this.isLoading = false;
      return;
    }

    const formData = new FormData();
    formData.append('businessRegistration', this.selectedFiles.businessRegistration);
    formData.append('taxDocument', this.selectedFiles.taxDocument);
    formData.append('addressProof', this.selectedFiles.addressProof);
    
    if (this.selectedFiles.identityProof) {
      formData.append('identityProof', this.selectedFiles.identityProof);
    }

    this.businessVerificationService.uploadVerificationDocuments(formData).subscribe({
      next: (response: any) => {
        this.successMessage = response.message;
        this.isLoading = false;
        this.showUploadForm = false;
        this.selectedFiles = {
          businessRegistration: null,
          taxDocument: null,
          addressProof: null,
          identityProof: null
        };
        this.loadVerificationStatus();
      },
      error: (error: any) => {
        console.error('Error uploading documents:', error);
        this.errorMessage = error.error?.error || 'Failed to upload documents';
        this.isLoading = false;
      }
    });
  }

  resubmitVerification(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Validate form
    if (!this.resubmitForm.businessName || !this.resubmitForm.businessType || !this.resubmitForm.taxId || !this.resubmitForm.businessAddress) {
      this.errorMessage = 'Please fill in all required fields';
      this.isLoading = false;
      return;
    }

    this.businessVerificationService.resubmitVerification(this.resubmitForm).subscribe({
      next: (response: any) => {
        this.successMessage = response.message;
        this.isLoading = false;
        this.showResubmitForm = false;
        this.loadVerificationStatus();
      },
      error: (error: any) => {
        console.error('Error resubmitting verification:', error);
        this.errorMessage = error.error?.error || 'Failed to resubmit verification';
        this.isLoading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED': return 'status-rejected';
      default: return '';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'PENDING': return 'fas fa-clock';
      case 'APPROVED': return 'fas fa-check-circle';
      case 'REJECTED': return 'fas fa-times-circle';
      default: return 'fas fa-question-circle';
    }
  }

  getFileName(file: File): string {
    return file ? file.name : 'No file selected';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  resetFileSelection(documentType: string): void {
    this.selectedFiles[documentType] = null;
  }
}
