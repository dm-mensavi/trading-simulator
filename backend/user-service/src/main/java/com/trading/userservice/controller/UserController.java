package com.trading.userservice.controller;

import com.trading.userservice.model.Portfolio;
import com.trading.userservice.model.User;
import com.trading.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users  body: { "username": "alice" }
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        User created = userService.createUser(request.username());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users/{id}/balance
    @GetMapping("/{id}/balance")
    public ResponseEntity<Portfolio> getBalance(@PathVariable Long id) {
        return userService.getUser(id)
                .map(u -> ResponseEntity.ok(u.getPortfolio()))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/users/{id}/portfolio/buy  — called internally by order-service
    @PostMapping("/{id}/portfolio/buy")
    public ResponseEntity<Portfolio> executeBuy(
            @PathVariable Long id,
            @RequestBody BuyRequest request) {
        Portfolio portfolio = userService.executeBuy(
                id, request.ticker(), request.quantity(), request.totalCost());
        return ResponseEntity.ok(portfolio);
    }

    public record UserCreateRequest(String username) {}

    public record BuyRequest(String ticker, BigDecimal quantity, BigDecimal totalCost) {}
}
