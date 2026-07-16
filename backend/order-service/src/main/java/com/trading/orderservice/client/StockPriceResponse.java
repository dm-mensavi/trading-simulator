package com.trading.orderservice.client;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockPriceResponse {
    private String ticker;
    private BigDecimal price;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal previousClose;
    private String timestamp;
}
