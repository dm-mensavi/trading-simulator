import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;
  showPassword = false;

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.error = 'Please enter your email and password.';
      return;
    }
    this.loading = true;
    this.error = '';

    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Invalid email or password. Please try again.';
        this.loading = false;
      }
    });
  }
}
