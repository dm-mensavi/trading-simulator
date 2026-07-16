package com.trading.marketservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice implements Serializable {

    private String ticker;
    private BigDecimal price;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal previousClose;
    private String timestamp;
}
