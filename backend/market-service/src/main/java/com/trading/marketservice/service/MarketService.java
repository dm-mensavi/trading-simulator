package com.trading.marketservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.marketservice.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    public MarketService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the current stock price from Alpha Vantage.
     * Result is cached in Redis for 5 minutes (configured in RedisConfig).
     *
     * @param ticker the stock symbol e.g. "AAPL"
     * @return StockPrice containing current price and OHLC data
     */
    @Cacheable(value = "stockPrices", key = "#ticker.toUpperCase()")
    public StockPrice getStockPrice(String ticker) {
        String url = String.format(
                "%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                BASE_URL, ticker.toUpperCase(), apiKey
        );

        log.info("Fetching live price for {} from Alpha Vantage", ticker.toUpperCase());

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode quote = root.path("Global Quote");

            if (quote.isMissingNode() || quote.isEmpty()) {
                throw new RuntimeException("No data found for ticker: " + ticker.toUpperCase());
            }

            return new StockPrice(
                    quote.path("01. symbol").asText(),
                    new BigDecimal(quote.path("05. price").asText()),
                    new BigDecimal(quote.path("02. open").asText()),
                    new BigDecimal(quote.path("03. high").asText()),
                    new BigDecimal(quote.path("04. low").asText()),
                    new BigDecimal(quote.path("08. previous close").asText()),
                    quote.path("07. latest trading day").asText()
            );
        } catch (Exception e) {
            log.error("Failed to fetch price for {}: {}", ticker, e.getMessage());
            throw new RuntimeException("Could not fetch stock price for " + ticker.toUpperCase(), e);
        }
    }
}
