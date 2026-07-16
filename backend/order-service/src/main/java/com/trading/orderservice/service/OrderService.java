package com.trading.orderservice.service;

import com.trading.orderservice.client.PortfolioResponse;
import com.trading.orderservice.client.StockPriceResponse;
import com.trading.orderservice.model.Order;
import com.trading.orderservice.model.OrderRepository;
import com.trading.orderservice.model.OrderStatus;
import com.trading.orderservice.model.OrderType;
import com.trading.orderservice.event.TradeExecutedEvent;
import com.trading.orderservice.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${services.market-service.url}")
    private String marketServiceUrl;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * BUY order flow:
     *  1. Fetch current price from market-service (Redis-cached)
     *  2. Verify user has sufficient cash via user-service
     *  3. Save the order record as PENDING
     *  4. Publish TradeExecutedEvent to RabbitMQ
     */
    @Transactional
    public Order placeBuyOrder(Long userId, String ticker, BigDecimal quantity) {
        // 1. Get current stock price
        String priceUrl = marketServiceUrl + "/api/market/price/" + ticker.toUpperCase();
        log.info("Fetching price from market-service: {}", priceUrl);
        StockPriceResponse priceResponse = restTemplate.getForObject(priceUrl, StockPriceResponse.class);

        if (priceResponse == null) {
            throw new RuntimeException("Unable to retrieve price for ticker: " + ticker);
        }

        BigDecimal pricePerShare = priceResponse.getPrice();
        BigDecimal totalCost = pricePerShare.multiply(quantity);
        log.info("Price for {}: {} | Total cost: {}", ticker, pricePerShare, totalCost);

        // 2. Check balance via user-service
        String balanceUrl = userServiceUrl + "/api/users/" + userId + "/balance";
        PortfolioResponse portfolio = restTemplate.getForObject(balanceUrl, PortfolioResponse.class);

        if (portfolio == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        if (portfolio.getCashBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException(
                    "Insufficient funds. Required: $" + totalCost + ", Available: $" + portfolio.getCashBalance());
        }

        // 3. Save the order record as PENDING
        Order order = new Order();
        order.setUserId(userId);
        order.setTicker(ticker.toUpperCase());
        order.setQuantity(quantity);
        order.setPricePerShare(pricePerShare);
        order.setTotalCost(totalCost);
        order.setOrderType(OrderType.BUY);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);
        
        log.info("Saved pending order. Publishing event to RabbitMQ...");

        // 4. Publish event to RabbitMQ
        TradeExecutedEvent event = new TradeExecutedEvent(
                order.getId(),
                userId,
                ticker.toUpperCase(),
                quantity,
                pricePerShare,
                totalCost,
                "BUY"
        );
        
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
        log.info("Published TradeExecutedEvent for buy order ID: {}", order.getId());

        return order;
    }

    /**
     * SELL order flow:
     *  1. Fetch current price from market-service (Redis-cached)
     *  2. Verify user has enough shares via user-service portfolio holdings
     *  3. Save the order record as PENDING SELL
     *  4. Publish TradeExecutedEvent to RabbitMQ with "SELL" type
     */
    @Transactional
    public Order placeSellOrder(Long userId, String ticker, BigDecimal quantity) {
        // 1. Get current stock price
        String priceUrl = marketServiceUrl + "/api/market/price/" + ticker.toUpperCase();
        log.info("Fetching price from market-service: {}", priceUrl);
        StockPriceResponse priceResponse = restTemplate.getForObject(priceUrl, StockPriceResponse.class);

        if (priceResponse == null) {
            throw new RuntimeException("Unable to retrieve price for ticker: " + ticker);
        }

        BigDecimal pricePerShare = priceResponse.getPrice();
        BigDecimal totalProceeds = pricePerShare.multiply(quantity);
        log.info("Price for sell {}: {} | Total proceeds: {}", ticker, pricePerShare, totalProceeds);

        // 2. Check holdings via user-service
        String balanceUrl = userServiceUrl + "/api/users/" + userId + "/balance";
        PortfolioResponse portfolio = restTemplate.getForObject(balanceUrl, PortfolioResponse.class);

        if (portfolio == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        // Validate the user has enough shares of the ticker
        boolean hasSufficientShares = false;
        if (portfolio.getHoldings() != null) {
            hasSufficientShares = portfolio.getHoldings().stream()
                    .filter(h -> h.getTicker().equalsIgnoreCase(ticker))
                    .anyMatch(h -> h.getQuantity().compareTo(quantity) >= 0);
        }

        if (!hasSufficientShares) {
            throw new RuntimeException("Insufficient shares. You do not own enough shares of " + ticker.toUpperCase() + " to execute this sell.");
        }

        // 3. Save the order record as PENDING
        Order order = new Order();
        order.setUserId(userId);
        order.setTicker(ticker.toUpperCase());
        order.setQuantity(quantity);
        order.setPricePerShare(pricePerShare);
        order.setTotalCost(totalProceeds);
        order.setOrderType(OrderType.SELL);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);
        
        log.info("Saved pending sell order. Publishing event to RabbitMQ...");

        // 4. Publish event to RabbitMQ
        TradeExecutedEvent event = new TradeExecutedEvent(
                order.getId(),
                userId,
                ticker.toUpperCase(),
                quantity,
                pricePerShare,
                totalProceeds,
                "SELL"
        );
        
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
        log.info("Published TradeExecutedEvent for sell order ID: {}", order.getId());

        return order;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, status);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
