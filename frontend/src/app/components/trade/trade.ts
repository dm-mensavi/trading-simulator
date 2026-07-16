import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MarketService } from '../../services/market';
import { OrderService } from '../../services/order';
import { AuthService } from '../../services/auth.service';
import { StockPrice, Order } from '../../models/models';

export interface WatchlistItem {
  ticker: string;
  name: string;
  sector: string;
}

@Component({
  selector: 'app-trade',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trade.html',
  styleUrl: './trade.css'
})
export class TradeComponent implements OnInit {
  // Buy / Sell Tabs
  isBuy = true;

  // Price Lookup
  tickerQuery = '';
  currentPrice: StockPrice | null = null;
  priceLoading = false;
  priceError = '';

  // Order Form
  orderTicker = '';
  orderQty: number = 1;
  orderLoading = false;
  orderError = '';
  successMsg = '';

  // Order History
  orders: Order[] = [];

  // Curated watchlist — top S&P 500 picks by weekly volume & momentum
  watchlist: WatchlistItem[] = [
    { ticker: 'NVDA',  name: 'NVIDIA Corporation',  sector: 'Technology' },
    { ticker: 'AAPL',  name: 'Apple Inc',            sector: 'Technology' },
    { ticker: 'MSFT',  name: 'Microsoft',            sector: 'Technology' },
    { ticker: 'GOOGL', name: 'Alphabet (Google)',    sector: 'Technology' },
    { ticker: 'AMZN',  name: 'Amazon.com',           sector: 'Consumer' },
    { ticker: 'META',  name: 'Meta Platforms',       sector: 'Social Media' },
    { ticker: 'TSLA',  name: 'Tesla Inc',            sector: 'Automotive' },
    { ticker: 'JPM',   name: 'JPMorgan Chase',       sector: 'Finance' },
    { ticker: 'V',     name: 'Visa Inc',             sector: 'Finance' },
    { ticker: 'BRK.B', name: 'Berkshire Hathaway',  sector: 'Finance' },
  ];

  constructor(
    private marketService: MarketService,
    private orderService: OrderService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const userId = this.auth.getCurrentUser()?.userId ?? 1;
    this.orderService.getUserOrders(userId).subscribe({
      next: (o) => {
        this.orders = o.sort((a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.cdr.detectChanges();
      },
      error: () => { this.orders = []; this.cdr.detectChanges(); }
    });

    // Listen to query parameters to handle routing from portfolio
    this.route.queryParams.subscribe(params => {
      if (params['ticker']) {
        const t = params['ticker'].toUpperCase();
        this.tickerQuery = t;
        this.orderTicker = t;
        this.lookupPrice();
      }
      if (params['action'] === 'sell') {
        this.isBuy = false;
      }
      this.cdr.detectChanges();
    });
  }

  setTab(buy: boolean): void {
    this.isBuy = buy;
    this.orderError = '';
    this.successMsg = '';
    this.cdr.detectChanges();
  }

  /** Clicking a watchlist card pre-fills ticker fields and fires a live price lookup */
  selectStock(item: WatchlistItem): void {
    this.tickerQuery = item.ticker;
    this.orderTicker = item.ticker;
    this.lookupPrice();
  }

  lookupPrice(): void {
    if (!this.tickerQuery.trim()) return;
    this.priceLoading = true;
    this.priceError = '';
    this.currentPrice = null;
    this.cdr.detectChanges();

    this.marketService.getStockPrice(this.tickerQuery.trim()).subscribe({
      next: (p) => {
        this.currentPrice = p;
        this.orderTicker = p.ticker;
        this.priceLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.priceError = 'Could not fetch price. Check the ticker symbol or your Alpha Vantage API key.';
        this.priceLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get estimatedCostOrProceeds(): number {
    if (!this.currentPrice || !this.orderQty) return 0;
    return this.currentPrice.price * this.orderQty;
  }

  executeOrder(): void {
    if (this.isBuy) {
      this.placeBuy();
    } else {
      this.placeSell();
    }
  }

  placeBuy(): void {
    if (!this.orderTicker || !this.orderQty || this.orderQty < 1) return;
    this.orderLoading = true;
    this.orderError = '';
    this.successMsg = '';
    this.cdr.detectChanges();

    const userId = this.auth.getCurrentUser()?.userId ?? 1;
    this.orderService.placeBuyOrder(userId, this.orderTicker, this.orderQty).subscribe({
      next: (order) => {
        const fmt = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
        this.successMsg = `✓ Bought ${order.quantity} shares of ${order.ticker} for ${fmt.format(order.totalCost)}`;
        this.orders.unshift(order);
        this.orderLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.orderError = typeof err.error === 'string' ? err.error : 'Order failed. Check your balance or try again.';
        this.orderLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  placeSell(): void {
    if (!this.orderTicker || !this.orderQty || this.orderQty < 1) return;
    this.orderLoading = true;
    this.orderError = '';
    this.successMsg = '';
    this.cdr.detectChanges();

    const userId = this.auth.getCurrentUser()?.userId ?? 1;
    this.orderService.placeSellOrder(userId, this.orderTicker, this.orderQty).subscribe({
      next: (order) => {
        const fmt = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
        this.successMsg = `✓ Sold ${order.quantity} shares of ${order.ticker} for ${fmt.format(order.totalCost)}`;
        this.orders.unshift(order);
        this.orderLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.orderError = typeof err.error === 'string' ? err.error : 'Sell order failed. Verify that you own enough shares.';
        this.orderLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /** Parqet logo CDN — purpose-built free stock logo service by ticker symbol */
  logoUrl(ticker: string): string {
    return `https://assets.parqet.com/logos/symbol/${ticker}?format=jpg`;
  }

  /** First letter of ticker for fallback avatar */
  tickerInitial(ticker: string): string {
    return ticker.charAt(0);
  }
}
