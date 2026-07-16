import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  fullName = '';
  email = '';
  password = '';
  confirmPassword = '';
  error = '';
  loading = false;
  showPassword = false;

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit(): void {
    if (!this.fullName || !this.email || !this.password) {
      this.error = 'Please fill in all fields.';
      return;
    }
    if (this.password.length < 6) {
      this.error = 'Password must be at least 6 characters.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.auth.register(this.fullName, this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error = err.error || 'Registration failed. Email may already be in use.';
        this.loading = false;
      }
    });
  }
}
