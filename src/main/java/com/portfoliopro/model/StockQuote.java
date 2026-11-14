package com.portfoliopro.model;

import java.io.Serializable;

/**
 * Real-time stock quote containing current market price and intraday stats.
 */
public class StockQuote implements Serializable {
    private String symbol;
    private String companyName;
    private double currentPrice;
    private double change;
    private double changePercent;
    private double dayHigh;
    private double dayLow;
    private long volume;
    private double previousClose;
    private String sector;

    public StockQuote() {}

    public StockQuote(String symbol, String companyName, double currentPrice, double change,
                      double changePercent, double dayHigh, double dayLow, long volume,
                      double previousClose, String sector) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.change = change;
        this.changePercent = changePercent;
        this.dayHigh = dayHigh;
        this.dayLow = dayLow;
        this.volume = volume;
        this.previousClose = previousClose;
        this.sector = sector;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public double getDayHigh() { return dayHigh; }
    public void setDayHigh(double dayHigh) { this.dayHigh = dayHigh; }

    public double getDayLow() { return dayLow; }
    public void setDayLow(double dayLow) { this.dayLow = dayLow; }

    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }

    public double getPreviousClose() { return previousClose; }
    public void setPreviousClose(double previousClose) { this.previousClose = previousClose; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
}

