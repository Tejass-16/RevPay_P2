import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  email: string = '';
  password: string = '';
  firstName: string = '';
  lastName: string = '';
  transactionPin: string = '';
  role: string = 'USER';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  register(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const userData = {
      email: this.email,
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
      transactionPin: this.transactionPin,
      role: this.role
    };

    this.authService.register(userData).subscribe({
      next: (response: any) => {
        if (response && response.token) {
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = 'Registration failed. Please try again.';
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Registration error:', error);
        this.errorMessage = 'Registration failed: ' + (error.message || 'Unknown error');
        this.isLoading = false;
      }
    });
  }
}