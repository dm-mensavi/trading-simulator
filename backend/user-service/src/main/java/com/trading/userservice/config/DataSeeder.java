package com.trading.userservice.config;

import com.trading.userservice.model.Portfolio;
import com.trading.userservice.model.User;
import com.trading.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a demo account on first startup.
 * Email: demo@tradesim.io  /  Password: demo1234
 * Skips if the email already exists.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("demo@tradesim.io").isEmpty()) {
            Portfolio portfolio = new Portfolio(new BigDecimal("10000.00"));
            User demo = new User();
            demo.setFullName("Demo User");
            demo.setEmail("demo@tradesim.io");
            demo.setUsername("demo");
            demo.setPasswordHash(passwordEncoder.encode("demo1234"));
            demo.setPortfolio(portfolio);
            userRepository.save(demo);
            System.out.println("✅ Demo account seeded: demo@tradesim.io / demo1234");
        }
    }
}
