package com.trading.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeExecutedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long userId;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalCost;
    private String orderType;
}
