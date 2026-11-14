package com.portfoliopro.model;

import java.io.Serializable;
import java.util.List;

/**
 * Top-level portfolio balance, P&L, and asset breakdown summary.
 */
public class PortfolioSummary implements Serializable {
    private double totalPortfolioValue; // Cash + Holdings
    private double cashBalance;
    private double investedValue;
    private double totalUnrealizedPnL;
    private double totalUnrealizedPnLPercent;
    private double totalRealizedPnL;
    private double dailyPnL;
    private double dailyPnLPercent;
    private int totalPositions;
    private List<Position> positions;

    public PortfolioSummary() {}

    public double getTotalPortfolioValue() { return totalPortfolioValue; }
    public void setTotalPortfolioValue(double totalPortfolioValue) { this.totalPortfolioValue = totalPortfolioValue; }

    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }

    public double getInvestedValue() { return investedValue; }
    public void setInvestedValue(double investedValue) { this.investedValue = investedValue; }

    public double getTotalUnrealizedPnL() { return totalUnrealizedPnL; }
    public void setTotalUnrealizedPnL(double totalUnrealizedPnL) { this.totalUnrealizedPnL = totalUnrealizedPnL; }

    public double getTotalUnrealizedPnLPercent() { return totalUnrealizedPnLPercent; }
    public void setTotalUnrealizedPnLPercent(double totalUnrealizedPnLPercent) { this.totalUnrealizedPnLPercent = totalUnrealizedPnLPercent; }

    public double getTotalRealizedPnL() { return totalRealizedPnL; }
    public void setTotalRealizedPnL(double totalRealizedPnL) { this.totalRealizedPnL = totalRealizedPnL; }

    public double getDailyPnL() { return dailyPnL; }
    public void setDailyPnL(double dailyPnL) { this.dailyPnL = dailyPnL; }

    public double getDailyPnLPercent() { return dailyPnLPercent; }
    public void setDailyPnLPercent(double dailyPnLPercent) { this.dailyPnLPercent = dailyPnLPercent; }

    public int getTotalPositions() { return totalPositions; }
    public void setTotalPositions(int totalPositions) { this.totalPositions = totalPositions; }

    public List<Position> getPositions() { return positions; }
    public void setPositions(List<Position> positions) { this.positions = positions; }
}

