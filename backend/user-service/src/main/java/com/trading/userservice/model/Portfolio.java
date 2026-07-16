package com.trading.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "portfolios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal cashBalance;

    @ElementCollection
    @CollectionTable(name = "stock_holdings", joinColumns = @JoinColumn(name = "portfolio_id"))
    private Set<StockHolding> holdings = new HashSet<>();

    public Portfolio(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }
}
