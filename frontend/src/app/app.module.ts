import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AppRoutingModule } from './app-routing.module';


// Components
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { SendMoneyComponent } from './send-money/send-money.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { AddFundsComponent } from './add-funds/add-funds.component';
import { PaymentMethodsComponent } from './payment-methods/payment-methods.component';
import { InvoicesComponent } from './invoices/invoices.component';
import { BusinessVerificationComponent } from './business-verification/business-verification.component';
import { AddPaymentMethodComponent } from './add-payment-method/add-payment-method.component';
import { MoneyRequestsComponent } from './money-requests/money-requests.component';
import { SocialPaymentsComponent } from './social-payments/social-payments.component';
import { DatePipe } from './pipes/date.pipe';
import { NotificationsComponent } from './notifications/notifications.component';
import { ProfileComponent } from './profile/profile.component';
import { AnalyticsComponent } from './analytics/analytics.component';
import { LandingComponent } from './landing/landing.component';

// Services
import { AuthService } from './services/auth.service';
import { TransactionService } from './services/transaction.service';
import { MoneyRequestService } from './services/money-request.service';
import { SocialPaymentService } from './services/social-payment.service';
import { InvoiceService } from './services/invoice.service';
import { PaymentMethodService } from './services/payment-method.service';
import { BusinessVerificationService } from './services/business-verification.service';
import { NotificationService } from './services/notification.service';
import { ProfileService } from './services/profile.service';
import { AnalyticsService } from './services/analytics.service';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { BusinessGuard } from './guards/business.guard';
import { LandingGuard } from './guards/landing.guard';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    DashboardComponent,
    LoginComponent,
    RegisterComponent,
    SendMoneyComponent,
    TransactionsComponent,
    AddFundsComponent,
    PaymentMethodsComponent,
    AddPaymentMethodComponent,
    AddFundsComponent,
    InvoicesComponent,
    BusinessVerificationComponent,
    MoneyRequestsComponent,
    SocialPaymentsComponent,
    DatePipe,
    AnalyticsComponent,
    LandingComponent,
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
    CommonModule,
    NotificationsComponent,
    ProfileComponent,
    RouterModule
  ],
  providers: [
    AuthService,
    TransactionService,
    MoneyRequestService,
    SocialPaymentService,
    InvoiceService,
    PaymentMethodService,
    BusinessVerificationService,
    NotificationService,
    ProfileService,
    AnalyticsService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }