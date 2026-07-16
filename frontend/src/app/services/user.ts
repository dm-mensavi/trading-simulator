import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, Portfolio } from '../models/models';

const API_BASE = '';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) { }

  /** POST /api/users — create a new user */
  createUser(username: string): Observable<User> {
    return this.http.post<User>(`${API_BASE}/api/users`, { username });
  }

  /** GET /api/users/{id} — get user by id */
  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${API_BASE}/api/users/${id}`);
  }

  /** GET /api/users/{id}/balance — get portfolio (cash + holdings) */
  getPortfolio(id: number): Observable<Portfolio> {
    return this.http.get<Portfolio>(`${API_BASE}/api/users/${id}/balance`);
  }
}
