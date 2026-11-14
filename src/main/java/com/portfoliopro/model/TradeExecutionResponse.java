package com.portfoliopro.model;

import java.io.Serializable;

/**
 * Result response after an order execution attempt.
 */
public class TradeExecutionResponse implements Serializable {
    private boolean success;
    private String message;
    private TradeOrder order;
    private double newCashBalance;

    public TradeExecutionResponse() {}

    public TradeExecutionResponse(boolean success, String message, TradeOrder order, double newCashBalance) {
        this.success = success;
        this.message = message;
        this.order = order;
        this.newCashBalance = newCashBalance;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public TradeOrder getOrder() { return order; }
    public void setOrder(TradeOrder order) { this.order = order; }

    public double getNewCashBalance() { return newCashBalance; }
    public void setNewCashBalance(double newCashBalance) { this.newCashBalance = newCashBalance; }
}

