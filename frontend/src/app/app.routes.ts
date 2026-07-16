import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // Public routes
  { path: 'login',    loadComponent: () => import('./components/login/login').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./components/register/register').then(m => m.RegisterComponent) },

  // Protected routes
  { path: 'dashboard', canActivate: [authGuard], loadComponent: () => import('./components/dashboard/dashboard').then(m => m.DashboardComponent) },
  { path: 'portfolio', canActivate: [authGuard], loadComponent: () => import('./components/portfolio/portfolio').then(m => m.PortfolioComponent) },
  { path: 'trade',     canActivate: [authGuard], loadComponent: () => import('./components/trade/trade').then(m => m.TradeComponent) },
  { path: 'profile',   canActivate: [authGuard], loadComponent: () => import('./components/profile/profile').then(m => m.ProfileComponent) },

  // Fallback
  { path: '**', redirectTo: 'dashboard' }
];
