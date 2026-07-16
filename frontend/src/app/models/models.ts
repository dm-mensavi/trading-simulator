export interface User {
  id: number;
  username: string;
  portfolio: Portfolio;
}

export interface Portfolio {
  id: number;
  cashBalance: number;
  holdings: StockHolding[];
}

export interface StockHolding {
  ticker: string;
  quantity: number;
  averagePurchasePrice?: number;
}

export interface StockPrice {
  ticker: string;
  price: number;
  open: number;
  high: number;
  low: number;
  previousClose: number;
  timestamp: string;
}

export interface Order {
  id: number;
  userId: number;
  ticker: string;
  quantity: number;
  pricePerShare: number;
  totalCost: number;
  orderType: "BUY" | "SELL";
  status: "EXECUTED" | "PENDING" | "FAILED";
  createdAt: string;
}
