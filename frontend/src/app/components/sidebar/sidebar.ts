import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, AuthUser } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent implements OnInit {
  user: AuthUser | null = null;

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(u => this.user = u);
  }

  get initials(): string {
    if (!this.user?.fullName) return '?';
    return this.user.fullName.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  logout(): void {
    this.auth.logout();
  }
}
