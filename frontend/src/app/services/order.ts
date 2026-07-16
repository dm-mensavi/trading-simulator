import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/models';

const API_BASE = '';

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) { }

  /**
   * POST /api/orders/buy
   * body: { userId: number, ticker: string, quantity: number }
   */
  placeBuyOrder(userId: number, ticker: string, quantity: number): Observable<Order> {
    return this.http.post<Order>(`${API_BASE}/api/orders/buy`, { userId, ticker, quantity });
  }

  /**
   * POST /api/orders/sell
   * body: { userId: number, ticker: string, quantity: number }
   */
  placeSellOrder(userId: number, ticker: string, quantity: number): Observable<Order> {
    return this.http.post<Order>(`${API_BASE}/api/orders/sell`, { userId, ticker, quantity });
  }

  /** GET /api/orders/user/{userId} — fetch all orders for a user */
  getUserOrders(userId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_BASE}/api/orders/user/${userId}`);
  }
}
