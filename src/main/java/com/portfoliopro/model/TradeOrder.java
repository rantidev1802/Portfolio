package com.portfoliopro.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Order model for trade execution requests and audit log.
 */
public class TradeOrder implements Serializable {
    private String orderId;
    private String symbol;
    private String orderType; // BUY or SELL
    private int quantity;
    private double price;
    private double totalAmount;
    private String timestamp;
    private String status; // FILLED, REJECTED, PENDING

    public TradeOrder() {}

    public TradeOrder(String orderId, String symbol, String orderType, int quantity,
                      double price, double totalAmount, String timestamp, String status) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.orderType = orderType;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

