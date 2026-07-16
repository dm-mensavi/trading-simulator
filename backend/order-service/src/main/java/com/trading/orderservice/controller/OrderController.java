package com.trading.orderservice.controller;

import com.trading.orderservice.model.Order;
import com.trading.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")

public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders/buy
     * body: { "userId": 1, "ticker": "AAPL", "quantity": 5 }
     */
    @PostMapping("/buy")
    public ResponseEntity<?> placeBuyOrder(@RequestBody BuyOrderRequest request) {
        try {
            Order order = orderService.placeBuyOrder(
                    request.userId(), request.ticker(), request.quantity());
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/orders/sell
     * body: { "userId": 1, "ticker": "AAPL", "quantity": 5 }
     */
    @PostMapping("/sell")
    public ResponseEntity<?> placeSellOrder(@RequestBody SellOrderRequest request) {
        try {
            Order order = orderService.placeSellOrder(
                    request.userId(), request.ticker(), request.quantity());
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public record SellOrderRequest(Long userId, String ticker, BigDecimal quantity) {}

    /**
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam com.trading.orderservice.model.OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

    public record BuyOrderRequest(Long userId, String ticker, BigDecimal quantity) {}
}
