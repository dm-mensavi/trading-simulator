import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user';
import { AuthService } from '../../services/auth.service';
import { MarketService } from '../../services/market';
import { Portfolio } from '../../models/models';
import { forkJoin, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface HoldingDetail {
  ticker: string;
  quantity: number;
  averagePurchasePrice: number;
  currentPrice: number;
  totalCost: number;
  marketValue: number;
  profitLoss: number;
  profitLossPercent: number;
}

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './portfolio.html',
  styleUrl: './portfolio.css'
})
export class PortfolioComponent implements OnInit {
  portfolio: Portfolio | null = null;
  loading = true;
  error = '';

  holdingDetails: HoldingDetail[] = [];
  totalPortfolioValue = 0;
  totalProfitLoss = 0;
  totalProfitLossPercent = 0;
  totalCostBasis = 0;

  constructor(
    private userService: UserService,
    private auth: AuthService,
    private marketService: MarketService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const userId = this.auth.getCurrentUser()?.userId ?? 1;
    this.userService.getPortfolio(userId).subscribe({
      next: (p) => {
        this.portfolio = p;
        this.loading = false;
        this.loadCurrentPrices();
      },
      error: () => {
        this.error = 'Failed to load portfolio. Is the user-service running?';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadCurrentPrices(): void {
    if (!this.portfolio || !this.portfolio.holdings || this.portfolio.holdings.length === 0) {
      this.totalPortfolioValue = this.portfolio?.cashBalance ?? 0;
      this.holdingDetails = [];
      this.totalProfitLoss = 0;
      this.totalProfitLossPercent = 0;
      this.totalCostBasis = 0;
      this.cdr.detectChanges();
      return;
    }

    const requests = this.portfolio.holdings.map(h => {
      return this.marketService.getStockPrice(h.ticker).pipe(
        map(priceInfo => ({
          ticker: h.ticker,
          quantity: h.quantity,
          averagePurchasePrice: h.averagePurchasePrice ?? 0,
          currentPrice: priceInfo.price
        })),
        catchError(() => {
          // If price lookup fails, default to cost basis to show zero PnL
          return of({
            ticker: h.ticker,
            quantity: h.quantity,
            averagePurchasePrice: h.averagePurchasePrice ?? 0,
            currentPrice: h.averagePurchasePrice ?? 0
          });
        })
      );
    });

    forkJoin(requests).subscribe({
      next: (results) => {
        let totalCost = 0;
        let totalVal = 0;

        this.holdingDetails = results.map(r => {
          const cost = r.quantity * r.averagePurchasePrice;
          const marketValue = r.quantity * r.currentPrice;
          const profitLoss = marketValue - cost;
          const profitLossPercent = cost > 0 ? (profitLoss / cost) * 100 : 0;

          totalCost += cost;
          totalVal += marketValue;

          return {
            ticker: r.ticker,
            quantity: r.quantity,
            averagePurchasePrice: r.averagePurchasePrice,
            currentPrice: r.currentPrice,
            totalCost: cost,
            marketValue: marketValue,
            profitLoss: profitLoss,
            profitLossPercent: profitLossPercent
          };
        });

        this.totalCostBasis = totalCost;
        this.totalPortfolioValue = (this.portfolio?.cashBalance ?? 0) + totalVal;
        this.totalProfitLoss = totalVal - totalCost;
        this.totalProfitLossPercent = totalCost > 0 ? (this.totalProfitLoss / totalCost) * 100 : 0;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });
  }

  logoUrl(ticker: string): string {
    return `https://assets.parqet.com/logos/symbol/${ticker}?format=jpg`;
  }

  tickerInitial(ticker: string): string {
    return ticker.charAt(0);
  }
}
