package com.portfoliopro.model;

import java.io.Serializable;

/**
 * Fundamental financial analysis metrics for an equity (Valuation, Earnings, Risk).
 */
public class FundamentalData implements Serializable {
    private String symbol;
    private String companyName;
    private String sector;
    private String industry;
    private String marketCap;         // Formatted: e.g. "$2.95 Trillion"
    private double peRatio;           // Price to Earnings
    private double forwardPe;         // Forward P/E
    private double eps;               // Earnings per share
    private double dividendYield;     // e.g. 0.55%
    private double fiftyTwoWeekHigh;
    private double fiftyTwoWeekLow;
    private double beta;
    private double targetPrice;
    private String analystRating;     // BUY, STRONG_BUY, HOLD, UNDERPERFORM
    private String description;

    public FundamentalData() {}

    public FundamentalData(String symbol, String companyName, String sector, String industry,
                           String marketCap, double peRatio, double forwardPe, double eps,
                           double dividendYield, double fiftyTwoWeekHigh, double fiftyTwoWeekLow,
                           double beta, double targetPrice, String analystRating, String description) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.industry = industry;
        this.marketCap = marketCap;
        this.peRatio = peRatio;
        this.forwardPe = forwardPe;
        this.eps = eps;
        this.dividendYield = dividendYield;
        this.fiftyTwoWeekHigh = fiftyTwoWeekHigh;
        this.fiftyTwoWeekLow = fiftyTwoWeekLow;
        this.beta = beta;
        this.targetPrice = targetPrice;
        this.analystRating = analystRating;
        this.description = description;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getMarketCap() { return marketCap; }
    public void setMarketCap(String marketCap) { this.marketCap = marketCap; }

    public double getPeRatio() { return peRatio; }
    public void setPeRatio(double peRatio) { this.peRatio = peRatio; }

    public double getForwardPe() { return forwardPe; }
    public void setForwardPe(double forwardPe) { this.forwardPe = forwardPe; }

    public double getEps() { return eps; }
    public void setEps(double eps) { this.eps = eps; }

    public double getDividendYield() { return dividendYield; }
    public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

    public double getFiftyTwoWeekHigh() { return fiftyTwoWeekHigh; }
    public void setFiftyTwoWeekHigh(double fiftyTwoWeekHigh) { this.fiftyTwoWeekHigh = fiftyTwoWeekHigh; }

    public double getFiftyTwoWeekLow() { return fiftyTwoWeekLow; }
    public void setFiftyTwoWeekLow(double fiftyTwoWeekLow) { this.fiftyTwoWeekLow = fiftyTwoWeekLow; }

    public double getBeta() { return beta; }
    public void setBeta(double beta) { this.beta = beta; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public String getAnalystRating() { return analystRating; }
    public void setAnalystRating(String analystRating) { this.analystRating = analystRating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

