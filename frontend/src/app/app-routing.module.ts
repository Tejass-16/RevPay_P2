import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Components
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { SendMoneyComponent } from './send-money/send-money.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { AddFundsComponent } from './add-funds/add-funds.component';
import { PaymentMethodsComponent } from './payment-methods/payment-methods.component';
import { AddPaymentMethodComponent } from './add-payment-method/add-payment-method.component';
import { InvoicesComponent } from './invoices/invoices.component';
import { BusinessVerificationComponent } from './business-verification/business-verification.component';
import { MoneyRequestsComponent } from './money-requests/money-requests.component';
import { SocialPaymentsComponent } from './social-payments/social-payments.component';
import { DatePipe } from './pipes/date.pipe';
import { NotificationsComponent } from './notifications/notifications.component';
import { ProfileComponent } from './profile/profile.component';
import { AnalyticsComponent } from './analytics/analytics.component';
import { LandingComponent } from './landing/landing.component';
import { NotFoundComponent } from './not-found/not-found.component';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { BusinessGuard } from './guards/business.guard';
import { LandingGuard } from './guards/landing.guard';

const routes: Routes = [
  { path: '', component: LandingComponent, canActivate: [LandingGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'send-money', component: SendMoneyComponent, canActivate: [AuthGuard] },
  { path: 'transactions', component: TransactionsComponent, canActivate: [AuthGuard] },
  { path: 'add-funds', component: AddFundsComponent, canActivate: [AuthGuard] },
  { path: 'payment-methods', component: PaymentMethodsComponent, canActivate: [AuthGuard] },
  { path: 'invoices', component: InvoicesComponent, canActivate: [AuthGuard, BusinessGuard] },
  { path: 'business-verification', component: BusinessVerificationComponent, canActivate: [AuthGuard, BusinessGuard] },
  { path: 'add-payment-method', component: AddPaymentMethodComponent, canActivate: [AuthGuard, BusinessGuard] },
  { path: 'money-requests', component: MoneyRequestsComponent, canActivate: [AuthGuard] },
  { path: 'social-payments', component: SocialPaymentsComponent, canActivate: [AuthGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'analytics', component: AnalyticsComponent, canActivate: [AuthGuard] },
  { path: 'not-found', component: NotFoundComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }