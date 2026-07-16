import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StockPrice } from '../models/models';

const API_BASE = '';

@Injectable({ providedIn: 'root' })
export class MarketService {
  constructor(private http: HttpClient) { }

  /** GET /api/market/price/{ticker} — fetch live stock price (5-min Redis cache) */
  getStockPrice(ticker: string): Observable<StockPrice> {
    return this.http.get<StockPrice>(`${API_BASE}/api/market/price/${ticker.toUpperCase()}`);
  }
}
