import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  
  // Forgot Password Properties
  showForgotPasswordModal: boolean = false;
  forgotUsername: string = '';
  isForgotLoading: boolean = false;
  forgotPasswordMessage: string = '';
  forgotPasswordSuccess: boolean = false;
  resetCodeGenerated: boolean = false;
  resetCode: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  isResetLoading: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  onLogin(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        this.router.navigate(['/dashboard']);
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Login failed. Please try again.';
        this.isLoading = false;
      }
    });
  }

  // Forgot Password Methods
  showForgotPassword(): void {
    console.log('Forgot password clicked - showing modal');
    this.showForgotPasswordModal = true;
    this.forgotPasswordMessage = '';
    this.forgotUsername = '';
    this.forgotPasswordSuccess = false;
    this.resetCodeGenerated = false;
    this.resetCode = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.isResetLoading = false;
  }

  closeForgotPassword(): void {
    this.showForgotPasswordModal = false;
    this.forgotPasswordMessage = '';
    this.forgotUsername = '';
    this.forgotPasswordSuccess = false;
    this.resetCodeGenerated = false;
    this.resetCode = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.isResetLoading = false;
  }

  generateResetCode(): void {
    if (!this.forgotUsername.trim()) {
      this.forgotPasswordMessage = 'Please enter your email address';
      this.forgotPasswordSuccess = false;
      return;
    }

    // Basic email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.forgotUsername)) {
      this.forgotPasswordMessage = 'Please enter a valid email address';
      this.forgotPasswordSuccess = false;
      return;
    }

    this.isForgotLoading = true;
    this.forgotPasswordMessage = '';

    this.authService.forgotPassword(this.forgotUsername).subscribe({
      next: (response) => {
        if (response.success) {
          this.forgotPasswordMessage = 'Reset code generated! Enter the code from console to reset your password.';
          this.forgotPasswordSuccess = true;
          this.resetCodeGenerated = true;
          
          // Log to console for development (as requested)
          console.log('=== PASSWORD RESET CODE ===');
          console.log('Email:', this.forgotUsername);
          console.log('Reset Code:', response.developmentCode);
          console.log('========================');
          
          // Store reset info temporarily
          sessionStorage.setItem('resetEmail', this.forgotUsername);
        } else {
          this.forgotPasswordMessage = response.message || 'Failed to generate reset code';
          this.forgotPasswordSuccess = false;
        }
        this.isForgotLoading = false;
      },
      error: (error) => {
        this.forgotPasswordMessage = error.error?.message || 'Failed to generate reset code';
        this.forgotPasswordSuccess = false;
        this.isForgotLoading = false;
      }
    });
  }

  showPasswordResetForm(): void {
    // This would show a form to enter the reset code
    // For simplicity, we'll just show the success message
    this.forgotPasswordMessage = 'Code generated successfully! Check browser console (F12) for the reset code.';
    this.forgotPasswordSuccess = true;
  }

  resetPassword(): void {
    // Validate inputs
    if (!this.resetCode.trim()) {
      this.forgotPasswordMessage = 'Please enter the reset code';
      this.forgotPasswordSuccess = false;
      return;
    }

    if (!this.newPassword.trim()) {
      this.forgotPasswordMessage = 'Please enter a new password';
      this.forgotPasswordSuccess = false;
      return;
    }

    if (this.newPassword.length < 6) {
      this.forgotPasswordMessage = 'Password must be at least 6 characters long';
      this.forgotPasswordSuccess = false;
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.forgotPasswordMessage = 'Passwords do not match';
      this.forgotPasswordSuccess = false;
      return;
    }

    this.isResetLoading = true;
    this.forgotPasswordMessage = '';

    this.authService.resetPassword(this.forgotUsername, this.resetCode, this.newPassword).subscribe({
      next: (response) => {
        if (response.success) {
          this.forgotPasswordMessage = 'Password reset successfully! You can now login with your new password.';
          this.forgotPasswordSuccess = true;
          
          // Close modal after 2 seconds
          setTimeout(() => {
            this.closeForgotPassword();
          }, 2000);
        } else {
          this.forgotPasswordMessage = response.message || 'Failed to reset password';
          this.forgotPasswordSuccess = false;
        }
        this.isResetLoading = false;
      },
      error: (error) => {
        this.forgotPasswordMessage = error.error?.message || 'Failed to reset password';
        this.forgotPasswordSuccess = false;
        this.isResetLoading = false;
      }
    });
  }
}
