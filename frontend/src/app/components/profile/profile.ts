import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, AuthUser } from '../../services/auth.service';
import { UserService } from '../../services/user';
import { Portfolio } from '../../models/models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  user: AuthUser | null = null;
  portfolio: Portfolio | null = null;
  loading = true;

  constructor(
    private auth: AuthService,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.user = this.auth.getCurrentUser();
    if (!this.user) { this.router.navigate(['/login']); return; }

    this.userService.getPortfolio(this.user.userId).subscribe({
      next: (p: Portfolio) => {
        this.portfolio = p;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get initials(): string {
    if (!this.user?.fullName) return '?';
    return this.user.fullName.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  get memberSince(): string {
    if (!this.user?.createdAt) return '—';
    return new Date(this.user.createdAt).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  }

  get portfolioValue(): number {
    return this.portfolio?.cashBalance ?? 0;
  }

  get holdingsCount(): number {
    return this.portfolio?.holdings?.length ?? 0;
  }

  logout(): void {
    this.auth.logout();
  }
}
