package com.trading.userservice.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockHolding {
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal averagePurchasePrice;
}
