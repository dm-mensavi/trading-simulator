package com.trading.userservice.service;

import com.trading.userservice.dto.AuthRequest;
import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.model.Portfolio;
import com.trading.userservice.model.StockHolding;
import com.trading.userservice.model.User;
import com.trading.userservice.repository.UserRepository;
import com.trading.userservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ── Auth ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already registered: " + request.email());
        }

        Portfolio portfolio = new Portfolio(new BigDecimal("10000.00"));
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Derive username from email prefix, ensure unique by appending id later if needed
        user.setUsername(request.email().split("@")[0]);
        user.setPortfolio(portfolio);
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUsername(), user.getFullName());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getUsername(), user.getFullName(), user.getCreatedAt());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUsername(), user.getFullName());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getUsername(), user.getFullName(), user.getCreatedAt());
    }

    // ── Queries ───────────────────────────────────────────────────────────

    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ── Legacy: keep createUser for backward-compat (e.g. data seeders) ──

    @Transactional
    public User createUser(String username) {
        Portfolio portfolio = new Portfolio(new BigDecimal("10000.00"));
        User user = new User();
        user.setUsername(username);
        user.setFullName(username);
        user.setEmail(username + "@tradesim.local");
        user.setPasswordHash(passwordEncoder.encode("changeme"));
        user.setPortfolio(portfolio);
        return userRepository.save(user);
    }

    // ── Portfolio mutations ───────────────────────────────────────────────

    /**
     * Deducts totalCost from cash balance and adds the stock holding.
     * Called by order-service after a BUY order is validated.
     */
    @Transactional
    public Portfolio executeBuy(Long userId, String ticker, BigDecimal quantity, BigDecimal totalCost) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio.getCashBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient balance. Required: " + totalCost
                    + ", Available: " + portfolio.getCashBalance());
        }

        // Deduct cash
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalCost));

        // Add or update holding
        Set<StockHolding> holdings = portfolio.getHoldings();
        Optional<StockHolding> existing = holdings.stream()
                .filter(h -> h.getTicker().equalsIgnoreCase(ticker))
                .findFirst();

        BigDecimal pricePerShare = totalCost.divide(quantity, 4, java.math.RoundingMode.HALF_UP);

        if (existing.isPresent()) {
            StockHolding holding = existing.get();
            BigDecimal oldQty = holding.getQuantity();
            BigDecimal newQty = oldQty.add(quantity);
            
            BigDecimal oldAvg = holding.getAveragePurchasePrice();
            if (oldAvg == null) {
                oldAvg = pricePerShare;
            }
            
            BigDecimal oldTotalCost = oldQty.multiply(oldAvg);
            BigDecimal newTotalCost = oldTotalCost.add(totalCost);
            BigDecimal newAvg = newTotalCost.divide(newQty, 4, java.math.RoundingMode.HALF_UP);
            
            holding.setQuantity(newQty);
            holding.setAveragePurchasePrice(newAvg);
        } else {
            holdings.add(new StockHolding(ticker.toUpperCase(), quantity, pricePerShare));
        }

        userRepository.save(user);
        return portfolio;
    }

    /**
     * Adds saleProceeds to cash balance and deducts the stock holding.
     * Called by order-service after a SELL order is processed.
     */
    @Transactional
    public Portfolio executeSell(Long userId, String ticker, BigDecimal quantity, BigDecimal saleProceeds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Portfolio portfolio = user.getPortfolio();
        Set<StockHolding> holdings = portfolio.getHoldings();

        StockHolding holding = holdings.stream()
                .filter(h -> h.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Holding not found for ticker: " + ticker));

        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient quantity to sell. Holding: " 
                    + holding.getQuantity() + ", Selling: " + quantity);
        }

        // Add proceeds to cash balance
        portfolio.setCashBalance(portfolio.getCashBalance().add(saleProceeds));

        // Deduct quantity
        BigDecimal newQty = holding.getQuantity().subtract(quantity);
        if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            holdings.remove(holding);
        } else {
            holding.setQuantity(newQty);
        }

        userRepository.save(user);
        return portfolio;
    }
}
