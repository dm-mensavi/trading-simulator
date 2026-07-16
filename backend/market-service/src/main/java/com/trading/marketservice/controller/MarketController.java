package com.trading.marketservice.controller;

import com.trading.marketservice.model.StockPrice;
import com.trading.marketservice.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")

public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    /**
     * GET /api/market/price/{ticker}
     * Returns cached or live stock price for the given ticker.
     * Cache TTL: 5 minutes (configured in RedisConfig).
     */
    @GetMapping("/price/{ticker}")
    public ResponseEntity<StockPrice> getPrice(@PathVariable String ticker) {
        StockPrice price = marketService.getStockPrice(ticker);
        return ResponseEntity.ok(price);
    }
}
