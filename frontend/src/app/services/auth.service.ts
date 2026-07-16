import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface AuthUser {
  userId: number;
  email: string;
  username: string;
  fullName: string;
  createdAt: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = '/api/auth';
  private readonly STORAGE_KEY = 'tradesim_user';

  private _currentUser = new BehaviorSubject<AuthUser | null>(this.loadFromStorage());
  currentUser$ = this._currentUser.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  register(fullName: string, email: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.API}/register`, { fullName, email, password }).pipe(
      tap(user => this.setUser(user))
    );
  }

  login(email: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.API}/login`, { email, password }).pipe(
      tap(user => this.setUser(user))
    );
  }

  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this._currentUser.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    const user = this._currentUser.getValue();
    if (!user) return false;
    // Check token expiry via JWT payload
    try {
      const payload = JSON.parse(atob(user.token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getCurrentUser(): AuthUser | null {
    return this._currentUser.getValue();
  }

  getToken(): string | null {
    return this._currentUser.getValue()?.token ?? null;
  }

  private setUser(user: AuthUser): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
    this._currentUser.next(user);
  }

  private loadFromStorage(): AuthUser | null {
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
