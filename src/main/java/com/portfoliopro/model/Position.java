package com.portfoliopro.model;

import java.io.Serializable;

/**
 * An asset position currently held in the user's investment portfolio.
 */
public class Position implements Serializable {
    private String symbol;
    private String companyName;
    private int shares;
    private double averageBuyPrice;
    private double currentPrice;
    private double marketValue;
    private double costBasis;
    private double unrealizedPnL;
    private double unrealizedPnLPercent;
    private double portfolioWeight; // e.g. 25.5 for 25.5%

    public Position() {}

    public Position(String symbol, String companyName, int shares, double averageBuyPrice,
                    double currentPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.shares = shares;
        this.averageBuyPrice = averageBuyPrice;
        this.currentPrice = currentPrice;
        recalculate();
    }

    public void recalculate() {
        this.costBasis = this.shares * this.averageBuyPrice;
        this.marketValue = this.shares * this.currentPrice;
        this.unrealizedPnL = this.marketValue - this.costBasis;
        this.unrealizedPnLPercent = (this.costBasis > 0) ? (this.unrealizedPnL / this.costBasis) * 100.0 : 0.0;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; recalculate(); }

    public double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; recalculate(); }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; recalculate(); }

    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }

    public double getCostBasis() { return costBasis; }
    public void setCostBasis(double costBasis) { this.costBasis = costBasis; }

    public double getUnrealizedPnL() { return unrealizedPnL; }
    public void setUnrealizedPnL(double unrealizedPnL) { this.unrealizedPnL = unrealizedPnL; }

    public double getUnrealizedPnLPercent() { return unrealizedPnLPercent; }
    public void setUnrealizedPnLPercent(double unrealizedPnLPercent) { this.unrealizedPnLPercent = unrealizedPnLPercent; }

    public double getPortfolioWeight() { return portfolioWeight; }
    public void setPortfolioWeight(double portfolioWeight) { this.portfolioWeight = portfolioWeight; }
}

