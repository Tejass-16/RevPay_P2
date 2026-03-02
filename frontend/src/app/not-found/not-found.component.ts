import { Component } from '@angular/core';

@Component({
  selector: 'app-not-found',
  template: `
    <div class="container text-center mt-5">
      <h1>404 - Page Not Found</h1>
      <p>The page you're looking for doesn't exist.</p>
      <a routerLink="/dashboard" class="btn btn-primary">Go to Dashboard</a>
    </div>
  `,
  styles: [`
    .container {
      max-width: 600px;
      margin: 0 auto;
      padding: 2rem;
    }
    h1 {
      color: #dc3545;
      margin-bottom: 1rem;
    }
    p {
      margin-bottom: 2rem;
      color: #666;
    }
  `]
})
export class NotFoundComponent {}