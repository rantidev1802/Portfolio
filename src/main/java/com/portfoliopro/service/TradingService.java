package com.portfoliopro.service;

import com.portfoliopro.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Trade Execution Engine handling atomic BUY/SELL order processing,
 * position updates, cash ledger accounting, and order audit trail.
 */
@Service
public class TradingService {

    private final MarketDataService marketDataService;
    private final Map<String, Position> holdings = new ConcurrentHashMap<>();
    private final List<TradeOrder> transactionHistory = new CopyOnWriteArrayList<>();
    private final AtomicLong orderIdGenerator = new AtomicLong(1001);

    private double cashBalance = 100000.00; // $100,000 Initial Paper Trading Balance
    private double totalRealizedPnL = 0.00;

    public TradingService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
        initSampleHoldings();
    }

    private void initSampleHoldings() {
        // Seed with sample initial portfolio for immediate visualization
        executeOrder("AAPL", "BUY", 50);
        executeOrder("MSFT", "BUY", 30);
        executeOrder("NVDA", "BUY", 15);
    }

    /**
     * Atomically executes a BUY or SELL market order.
     */
    public synchronized TradeExecutionResponse executeOrder(String symbol, String type, int quantity) {
        String sym = symbol.toUpperCase().trim();
        String orderType = type.toUpperCase().trim();

        if (!marketDataService.symbolExists(sym)) {
            return new TradeExecutionResponse(false, "Invalid stock symbol: " + sym, null, this.cashBalance);
        }

        if (quantity <= 0) {
            return new TradeExecutionResponse(false, "Order quantity must be greater than zero.", null, this.cashBalance);
        }

        StockQuote quote = marketDataService.getQuote(sym);
        double executionPrice = quote.getCurrentPrice();
        double totalCost = round(executionPrice * quantity);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String orderId = "ORD-" + orderIdGenerator.getAndIncrement();

        if ("BUY".equals(orderType)) {
            if (this.cashBalance < totalCost) {
                TradeOrder rejected = new TradeOrder(orderId, sym, "BUY", quantity, executionPrice, totalCost, timestamp, "REJECTED_INSUFFICIENT_FUNDS");
                transactionHistory.add(0, rejected);
                return new TradeExecutionResponse(false, "Insufficient buying power. Required: $" + String.format("%.2f", totalCost) + ", Available: $" + String.format("%.2f", this.cashBalance), rejected, this.cashBalance);
            }

            // Deduct cash
            this.cashBalance = round(this.cashBalance - totalCost);

            // Update or add position
            Position existing = holdings.get(sym);
            if (existing != null) {
                int oldShares = existing.getShares();
                double oldTotal = oldShares * existing.getAverageBuyPrice();
                double newAvg = round((oldTotal + totalCost) / (oldShares + quantity));
                existing.setShares(oldShares + quantity);
                existing.setAverageBuyPrice(newAvg);
                existing.setCurrentPrice(executionPrice);
            } else {
                Position newPos = new Position(sym, quote.getCompanyName(), quantity, executionPrice, executionPrice);
                holdings.put(sym, newPos);
            }

            TradeOrder filled = new TradeOrder(orderId, sym, "BUY", quantity, executionPrice, totalCost, timestamp, "FILLED");
            transactionHistory.add(0, filled);
            return new TradeExecutionResponse(true, "Successfully bought " + quantity + " shares of " + sym + " at $" + executionPrice, filled, this.cashBalance);

        } else if ("SELL".equals(orderType)) {
            Position existing = holdings.get(sym);
            if (existing == null || existing.getShares() < quantity) {
                int owned = (existing != null) ? existing.getShares() : 0;
                TradeOrder rejected = new TradeOrder(orderId, sym, "SELL", quantity, executionPrice, totalCost, timestamp, "REJECTED_INSUFFICIENT_SHARES");
                transactionHistory.add(0, rejected);
                return new TradeExecutionResponse(false, "Cannot sell " + quantity + " shares. You only own " + owned + " shares of " + sym, rejected, this.cashBalance);
            }

            // Calculate realized PnL
            double costForSold = round(quantity * existing.getAverageBuyPrice());
            double realizedDelta = round(totalCost - costForSold);
            this.totalRealizedPnL = round(this.totalRealizedPnL + realizedDelta);

            // Credit cash
            this.cashBalance = round(this.cashBalance + totalCost);

            // Update position
            int remaining = existing.getShares() - quantity;
            if (remaining == 0) {
                holdings.remove(sym);
            } else {
                existing.setShares(remaining);
                existing.setCurrentPrice(executionPrice);
            }

            TradeOrder filled = new TradeOrder(orderId, sym, "SELL", quantity, executionPrice, totalCost, timestamp, "FILLED");
            transactionHistory.add(0, filled);
            return new TradeExecutionResponse(true, "Successfully sold " + quantity + " shares of " + sym + " at $" + executionPrice + " (Realized P&L: " + (realizedDelta >= 0 ? "+$" : "-$") + Math.abs(realizedDelta) + ")", filled, this.cashBalance);

        } else {
            return new TradeExecutionResponse(false, "Unsupported order type: " + type + ". Must be BUY or SELL.", null, this.cashBalance);
        }
    }

    public synchronized void resetPortfolio() {
        this.holdings.clear();
        this.transactionHistory.clear();
        this.cashBalance = 100000.00;
        this.totalRealizedPnL = 0.00;
        initSampleHoldings();
    }

    public synchronized void depositCash(double amount) {
        if (amount > 0) {
            this.cashBalance = round(this.cashBalance + amount);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            TradeOrder deposit = new TradeOrder("DEP-" + orderIdGenerator.getAndIncrement(), "CASH", "DEPOSIT", 1, amount, amount, timestamp, "COMPLETED");
            transactionHistory.add(0, deposit);
        }
    }

    public Map<String, Position> getHoldings() {
        // Refresh positions with latest market prices
        for (Position pos : holdings.values()) {
            StockQuote quote = marketDataService.getQuote(pos.getSymbol());
            if (quote != null) {
                pos.setCurrentPrice(quote.getCurrentPrice());
            }
        }
        return holdings;
    }

    public double getCashBalance() { return cashBalance; }
    public double getTotalRealizedPnL() { return totalRealizedPnL; }
    public List<TradeOrder> getTransactionHistory() { return new ArrayList<>(transactionHistory); }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

