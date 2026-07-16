import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user';
import { OrderService } from '../../services/order';
import { AuthService } from '../../services/auth.service';
import { Portfolio, Order } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  portfolio: Portfolio | null = null;
  orders: Order[] = [];
  loading = true;
  error = '';

  get executedOrders(): number {
    return this.orders.filter(o => o.status === 'EXECUTED').length;
  }

  constructor(
    private userService: UserService,
    private orderService: OrderService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const userId = this.auth.getCurrentUser()?.userId ?? 1;

    this.userService.getPortfolio(userId).subscribe({
      next: (p) => {
        this.portfolio = p;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load portfolio. Is the user-service running?';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });

    this.orderService.getUserOrders(userId).subscribe({
      next: (o) => {
        // Sort newest first so slice(0,5) always shows the most recent orders
        this.orders = o.sort((a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.cdr.detectChanges();
      },
      error: () => { this.orders = []; this.cdr.detectChanges(); }
    });
  }
}
