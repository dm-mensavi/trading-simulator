package com.trading.orderservice.client;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioResponse {
    private Long id;
    private BigDecimal cashBalance;
    private List<StockHoldingResponse> holdings;

    @Data
    public static class StockHoldingResponse {
        private String ticker;
        private BigDecimal quantity;
    }
}
