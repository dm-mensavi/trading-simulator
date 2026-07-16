package com.trading.userservice.listener;

import com.trading.userservice.config.RabbitConfig;
import com.trading.userservice.event.TradeExecutedEvent;
import com.trading.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TradeEventListener {

    private static final Logger log = LoggerFactory.getLogger(TradeEventListener.class);
    
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.order-service.url:http://localhost:8000}")
    private String gatewayOrOrderUrl;

    public TradeEventListener(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleTradeExecutedEvent(TradeExecutedEvent event) {
        log.info("Received TradeExecutedEvent for order ID: {} | Type: {}", event.getOrderId(), event.getOrderType());
        
        try {
            if ("SELL".equalsIgnoreCase(event.getOrderType())) {
                userService.executeSell(
                        event.getUserId(),
                        event.getTicker(),
                        event.getQuantity(),
                        event.getTotalCost() // totalCost field contains total proceeds for sells
                );
            } else {
                userService.executeBuy(
                        event.getUserId(),
                        event.getTicker(),
                        event.getQuantity(),
                        event.getTotalCost()
                );
            }
            log.info("Successfully executed trade in portfolio for user: {}", event.getUserId());
            
            // 2. Call back order-service to mark order as EXECUTED
            updateOrderStatus(event.getOrderId(), "EXECUTED");
            
        } catch (Exception e) {
            log.error("Failed to execute trade in portfolio for order {}: {}", event.getOrderId(), e.getMessage());
            
            // Failsafe: update order status to FAILED
            updateOrderStatus(event.getOrderId(), "FAILED");
        }
    }

    private void updateOrderStatus(Long orderId, String status) {
        String url = gatewayOrOrderUrl + "/api/orders/" + orderId + "/status?status=" + status;
        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("Updated order {} status to {}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to call order-service status update for order {}: {}", orderId, e.getMessage());
        }
    }
}
